(ns poc.routes
  (:require
    [poc.actions.todos :refer :all]
    [poc.actions.common :refer [cookies params]]
    [clojure.walk :refer [keywordize-keys]]))

(def routes
  [["/todo" {:get [todos view]
             :put [params add-todo view]}
    ["/:id" {:get [[get-one-todo :id] view]}
     ["/toggle-mark" {:post [[toggle-todo :id] view]}]
     ["/mark/:mark" {:post [[set-todo :id :mark] view]}]]]])
