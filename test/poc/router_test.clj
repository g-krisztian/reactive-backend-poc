(ns poc.router-test
  (:require [clojure.test :refer :all]
            [poc.actions.todos :refer :all]
            [poc.router :refer [router]]))

(def routes
  [["/todo" {:get [todos view-all]}
    ["/:id" {:get [[get-one-todo :id] view-one]}
     ["/toggle-mark" {:post [[toggle-todo :id] view-one]}]
     ["/mark/:mark" {:post [[set-todo :id :mark] view-one]}]]]])

(def router* (router routes))

(deftest router-test
  (is (= [todos view-all] (router* "/todo" :get)))
  (is (thrown-with-msg? Exception #"Not found" (router* "/todo" :post)))
  (is (thrown-with-msg? Exception #"Not found" (router* "/api/ctx/something" :post))))
