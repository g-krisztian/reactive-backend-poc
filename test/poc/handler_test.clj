(ns poc.handler-test
  (:require
    [clojure.test :refer :all]
    [poc.handler :refer [handler]]
    [poc.routes :refer [routes]]))

(def handler* (handler {} routes))
;;
;;(handler*
;; {:uri     "/api/message"
;;  :headers {:cookie "session-id=sseeessiioonn"}})
;;
;;(handler*
;;  {:uri     "/todo"
;;   :request-method :get})

(deftest use-id
  (is (= {:body {:post 0, :req {:uri "/todo/0", :request-method :get}}}
        (handler*
          {:uri     "/todo/0"
           :request-method :get}))))