(ns poc.handler-test
  (:require
    [clojure.test :refer :all]
    [poc.handler :refer [handler]]
    [poc.routes :refer [routes]]))

(def handler* (handler {} #'routes))

(deftest todos
  (is (= {:status 404, :body "not found"}
         (handler*
           {:uri            (str "/todo/0")
            :request-method :get})))
  (let [result (handler* {:uri            "/todo"
                          :request-method :put
                          :params         {:label "Example todo"}})
        todo-id (get-in result [:body 0 :todos/id])]
    (is (= "Example todo"
           (get-in result [:body 0 :todos/label])))
    (is (number? todo-id))
    (is (false? (get-in result [:body 0 :todos/marked])))
    (testing "Mark todo item marked"
      (is (= true
             (-> (handler*
                   {:uri            (format "/todo/%d/mark/true" todo-id)
                    :request-method :post})
                 (get-in [:body 0 :todos/marked])))))
    (testing "Toggle todo item marked"
      (is (= false
             (get-in (handler*
                       {:uri            (format "/todo/%d/toggle-mark" todo-id)
                        :request-method :post})
                     [:body 0 :todos/marked]))))))

(deftest echo
  (is (= {:status 200, :body {:uri "/echo", :request-method :get}}
         (handler*
           {:uri            (str "/echo")
            :request-method :get}))))
