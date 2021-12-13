(ns poc.handler-test
  (:require
    [clojure.test :refer :all]
    [poc.handler :refer [handler]]
    [poc.routes :refer [routes]]))

(def handler* (handler {} routes))

(deftest use-id
  (is (= {:body {:post 0, :req {:uri "/todo/0", :request-method :get}}}
         (handler*
           {:uri            "/todo/0"
            :request-method :get})))

  (is (boolean?
        (get-in (handler*
                  {:uri            "/todo/100/toggle-mark"
                   :request-method :post})
                [:body
                 100
                 :marked])))
  (is (= {:body {100 {:marked true}}}
        (handler*
          {:uri            "/todo/100/mark/true"
           :request-method :post}))))
