(ns poc.routes
  (:require
    [clojure.walk :refer [keywordize-keys]]
    [poc.actions.common :refer [cookies params]]
    [poc.actions.todos :refer :all]
    [poc.database.core :as db]))

(def routes
  [["/todo" {:get [#'todos #'db/execute #'view]
             :put [#'params #'add-todo #'db/execute #'view]}
    ["/:id" {:get [[select-todo :id] #'db/execute #'view]}
     ["/toggle-mark" {:post [[toggle-todo :id] #'db/execute #'view]}]
     ["/mark/:mark" {:post [[set-todo :id :mark] #'db/execute #'view]}]]]])
