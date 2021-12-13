(ns poc.handler-test
  (:require
    [clojure.test :refer :all]
    [poc.handler :refer [handler]]
    [poc.routes :refer [routes]]))

(def handler* (handler {} routes))

(deftest use-id
  (is (= {:status 404, :body "Todo 0 not found"}
         (handler*
           {:uri            "/todo/0"
            :request-method :get})))
  (is (boolean?
        (get-in (handler*
                  {:uri            "/todo/100/toggle-mark"
                   :request-method :post})
                [:body
                 :marked])))

  (is (= {:body {:marked true}}
         (handler*
           {:uri            "/todo/100/mark/true"
            :request-method :post})))

  (let [result (handler* {:uri            "/todo"
                          :request-method :put
                          :params         {:label "Example todo"}})]
    (is (= "Example todo"
           (get-in result [:body :label])))
    (is (uuid? (get-in result [:body :id])))))