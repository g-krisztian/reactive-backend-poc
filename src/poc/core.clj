(ns poc.core
  (:require
    [ring.adapter.jetty :as jetty]
    [ring.middleware.cookies :as cookies]
    [ring.middleware.params :as middleware.params]
    [clojure.walk :refer [keywordize-keys]]))

(defn router
  [routes uri]
  (loop [routes routes uri-seq (re-seq #"/[a-zA-Z0-9]*" uri) actions []]
    (let [route (filter #(#{(first uri-seq)} (first %)) routes)
          [[uri act & sub-routes]] route]
      (cond
        (empty? uri-seq) actions
        uri (recur sub-routes (rest uri-seq) (into actions act))
        :else (throw (ex-info "Not found" {:status 404 :body "not found"}))))))

(defn deep-merge [& maps]
  (apply merge-with
         (fn [& args]
           (if (every? map? args)
             (apply deep-merge args)
             (last args)))
         maps))

(defn watcher-fn
  [dependencies subscribers]
  (fn [key watched old-state new-state]
    (let [oldv (get old-state key)
          newv (get new-state key)]
      (when-not (= oldv newv)
        (let [actions (get subscribers key)
              results (map #(% dependencies newv) actions)
              rs (apply deep-merge new-state results)]
          (reset! watched rs))))))

(defn deliver-response
  [response-promise]
  [:response
   (fn [_ new-state]
     (deliver response-promise new-state)
     nil)])

(defn subscribe
  [subscribers [k f]]
  (let [subs (get subscribers k [])]
    (assoc subscribers k (conj subs f))))

(defn handler
  [dependencies routes]
  (fn [request]
    (try (let [uri (-> request :uri)
               actions (router routes uri)
               ctx (atom {})
               response-promise (promise)
               subscribers (reduce subscribe {} (conj actions (deliver-response response-promise)))]
           (doseq [[k _] subscribers]
             (add-watch ctx k (watcher-fn dependencies subscribers)))
           (reset! ctx {:request request})
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
     {:request (parse-request-cookies new-state)})])

(def params
  [:request
   (fn [_ new-state]
     (let [f #(keywordize-keys
                ((middleware.params/wrap-params identity) %))]
       {:request (f new-state)}))])

(def action
  [:request
   (fn [_ _]
     {:query {:select :* :from :users}})])

(def db
  [:query
   (fn [ctx _]
     {:db-data (str ctx)})])

(def view
  [:db-data
   (fn [_ new-state]
     {:response {:status 200
                 :body   (str new-state)}})])

(def cont
  [:request
   (fn [ctx _]
     {:response {:status 200
                 :body   (str ctx)}})])

(def api
  [cookies
   params])

(def message
  [action
   db
   view])

(def context
  [action
   db
   cont])

(def image
  [:request
   (fn [_ _]
     {:response {:body "Hello image"}})])

(def routes
  [["/" []]
   ["/api" api
    ["/message" message]
    ["/ctx" context]
    ["/image" [image]]]])

(comment
  (= [] (router routes "/"))
  (= api (router routes "/api"))
  (= (concat api message) (router routes "/api/message"))
  (= (concat api context) (router routes "/api/ctx"))
  (= (concat api context [cont]) (router routes "/api/ctx/something"))
  (= (conj api image) (router routes "/api/image")))

(defn -main
  [& _]
  (jetty/run-jetty (handler {:db {:datasource :nil}} routes) {:port 8080}))

(comment
  (time (dotimes [_ 50000]
          ((handler {} routes) {:uri     "/api/message"
                                :headers {:cookie "session-id=sseeessiioonn"}}))))