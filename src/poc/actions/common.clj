(ns poc.actions.common
  (:require [ring.middleware.cookies :as cookies]
            [ring.middleware.params :as middleware.params]))

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
