(ns poc.router-test
  (:require [clojure.test :refer :all]
            [poc.actions.todos :refer :all]
            [poc.router :refer [router]]))

(def routes
  [["/todo" {:get [todos view]}
    ["/:id" {:get [[#'select-todo :id] view]}
     ["/toggle-mark" {:post [[toggle-todo :id] view]}]
     ["/mark/:mark" {:post [[set-todo :id :mark] view]}]]]])

(def router* (router routes))

(deftest router-test
  (is (= [todos view] (router* "/todo" :get)))
  (is (= [todos view [#'poc.actions.todos/select-todo :id] view] (router* "/todo/100" :get)))
  (is (thrown-with-msg? Exception #"Not found" (router* "/todo" :post)))
  (is (thrown-with-msg? Exception #"Not found" (router* "/api/ctx/something" :post))))
