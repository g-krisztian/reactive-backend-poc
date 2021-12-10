(ns poc.core
  (:require [ring.middleware.cookies :as cookies]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as middleware.params]
            [clojure.walk :refer [keywordize-keys]]))

(defn- sub-router
  ([routes uri-seq]
   (loop [routes routes uri-seq uri-seq actions []]
     (let [route (filter #(#{(first uri-seq)} (first %)) routes)
           [[uri act & sub-routes]] route]
       (cond
         (empty? uri-seq) actions
         uri (recur sub-routes (rest uri-seq) (into actions act))
         :else (throw (ex-info "Not found" {:status 404 :body "not found"})))))))

(defn router
  [routes uri]
  (let [seq-uri (re-seq #"/[a-zA-Z0-9]*" uri)]
    (sub-router routes seq-uri)))

(defn handler
  [dependencies routes]
  (fn [request]
    (prn "URI " (-> request :uri))
    (try (let [uri (-> request :uri)
               actions (router routes uri)
               ctx (atom {})
               response-promise (promise)
               subscribers (atom {})
               subscribe (fn [k f]
                           (let [subs (get @subscribers k [])]
                             (swap! subscribers assoc k (conj subs f))))
               watcher (fn [key watched old-state new-state]
                         (prn "watcher " key)
                         (let [oldv (get old-state key)
                               newv (get new-state key)]
                           (when-not (= oldv newv)
                             (let [actions (get @subscribers key)
                                   results (for [a actions]
                                             (a dependencies newv))
                                   rs (into {} (for [r results]
                                                 (reduce (fn [s [k v]]
                                                           (cond
                                                             (map? v) (update s k merge v)
                                                             :else (assoc s k v))) new-state r)))]
                               (reset! watched rs)))))
               deliver-response [:response
                                 (fn [_ new-state]
                                   (prn "response " new-state)
                                   (deliver response-promise new-state)
                                   nil)]]
           (doseq [[k f] (conj actions deliver-response)]
             (subscribe k f)
             (add-watch ctx k watcher))
           (swap! ctx assoc :request request)
           (deref response-promise 1000 {:status 500
                                         :body   "request timed out"}))
         (catch Exception e (ex-data e)))))


(def parse-request-cookies
  (fn [req]
    (letfn [(move-cookies [{headers :headers :as req}]
              (cond-> req
                      (not (get headers "cookie")) (assoc-in
                                                     [:headers "cookie"]
                                                     (:cookie headers))))]
      (-> req move-cookies cookies/cookies-request keywordize-keys))))

(def cookies
  [:request
   (fn [_ new-state]
     (prn "cookies" new-state)
     {:request (parse-request-cookies new-state)})])

(def params
  [:request
   (fn [_ new-state]
     (prn "params" new-state)
     (let [f #(keywordize-keys
                ((middleware.params/wrap-params identity) %))]
       {:request (f new-state)}))])

(def action
  [:request
   (fn [_ new-state]
     (prn "action" new-state)
     {:query {:select :* :from :users}})])

(def db
  [:query
   (fn [ctx new-state]
     (prn "db" new-state)
     {:db-data (str ctx)})])


(def view
  [:db-data
   (fn [_ new-state]
     (prn "view" new-state)
     {:response {:status 200
                 :body   (str new-state)}})])

(def api
  [cookies
   params])

(def message
  [action
   db
   view])

(def image
  [:request
   (fn [_ _]
     {:response {:body "Hello image"}})])

(def routes
  [["/" []]
   ["/api" api
    ["/message" message]
    ["/image" [image]]]])

(= [] (router routes "/"))
(= api (router routes "/api"))
(= (concat api message) (router routes "/api/message"))
(= api (router routes "/api/image"))

(defn -main
  [& _]
  (jetty/run-jetty (handler {:db {:datasource :nil}} routes) {:port 8080}))