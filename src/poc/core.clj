(ns poc.core
  (:require
    [ring.adapter.jetty :as jetty]
    [poc.handler :refer [handler]]
    [poc.routes :refer [routes]]
    [poc.router :refer [router]]))





(defn -main
  [& _]
  (jetty/run-jetty (handler {:db {:datasource :nil}} routes) {:port 8080}))