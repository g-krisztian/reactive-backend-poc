(ns poc.routes
  (:require
    [poc.actions.todos :refer :all]
    [poc.actions.common :refer [cookies params]]
    [clojure.walk :refer [keywordize-keys]]))

(def routes
  [["/todo" {:get [todos view-all]
             :put [params add-todo view-one]}
    ["/:id" {:get [[get-one-todo :id] view-one]}
     ["/toggle-mark" {:post [[toggle-todo :id] view-one]}]
     ["/mark/:mark" {:post [[set-todo :id :mark] view-one]}]]]])
