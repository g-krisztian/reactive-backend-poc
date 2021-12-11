(ns poc.routes
  (:require
    [ring.middleware.cookies :as cookies]
    [ring.middleware.params :as middleware.params]
    [clojure.walk :refer [keywordize-keys]]))

(def cookies
  [:request
   (fn [_ request]
     {:request
      (letfn [(move-cookies [{headers :headers :as req}]
                (cond-> req
                        (not (get headers "cookie")) (assoc-in
                                                       [:headers "cookie"]
                                                       (:cookie headers))))]
        (-> request move-cookies cookies/cookies-request))})])

(def params
  [:request
   (fn [_ request]
     {:request ((middleware.params/wrap-params identity) request)})])

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
   (fn [_ db-data]
     {:response {:status 200
                 :body   (str db-data)}})])

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

(def posts
  [:request
   (fn [_ _]
     {:response-data "see all posts"})])

(def view-all
  [:response-data
   (fn [_ rd]
     {:response {:body rd}})])

(def view-one
  [:response-data
   (fn [_ rd]
     {:response {:body rd}})])

(defn get-one-post
  [post-id]
  [:request
   (fn [_ req]
     {:response-data {:post post-id
                      :req  req}})])

(def routes
  [["/todo" {:get [posts view-all]}
    ["/:id" {:get [(partial get-one-post) view-one]}]]
   ["/api" api
    ["/message" message]
    ["/ctx" context]
    ["/image" [image]]]])
