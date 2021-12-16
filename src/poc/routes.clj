(ns poc.routes
  (:require
    [clojure.walk :refer [keywordize-keys]]
    [poc.actions.common :refer [cookies params]]
    [poc.actions.todos :refer :all]
    [poc.database.core :as db]
    [poc.router :as router]))

(def routes
  (router/def-routes
    [["/echo" {:get [echo]}]
     ["/todo" {:get [todos db/execute view]
               :put [params add-todo db/execute view]}
      ["/:id" {:get [[select-todo :id] db/execute view]}    ; :id is resolved by router
       ["/toggle-mark" {:post [[toggle-todo :id] db/execute view]}] ; :id is resolved by router
       ["/mark/:mark" {:post [[set-todo :id :mark] db/execute view]}]]]])) ; :id and :mark are resolved by router
