(ns poc.actions.todos
  "First element of the vector is referring to the key to where to subscribe on the state atom
  Second element is the function to execute when the key's value changing
  Actions are executed in order of definition, results are merged")

(def echo
  [:request
   (fn [_ res]
     {:response {:status 200
                 :body res}})])

(def view
  [:db-response
   (fn [_ rd]
     (if (seq rd) {:response {:body rd}}
         {:response {:status 404
                     :body "not found"}}))])

(defn ->todos [m]
  (select-keys m [:id :label :marked]))

(def todos
  [:request
   (fn [_ _]
     {:query {:select [:*]
              :from   [:todos]}})])

(def add-todo
  [:request
   (fn [_ {params :params}]
     {:query {:insert-into [:todos]
              :values      [(->todos params)]}})])

(defn select-todo
  "this kind of action is initialized on routing process from route parameters"
  [todo-id]
  [:request
   (fn [_ _]
     {:query {:where [:= :id todo-id]}})])

(defn toggle-todo
  "this kind of action is initialized on routing process from route parameters"
  [todo-id]
  [:request
   (fn [_ _]
     {:query {:update [:todos]
              :where  [:= :id todo-id]
              :set    {:marked [:not :marked]}}})])

(defn set-todo
  "this kind of action is initialized on routing process from route parameters"
  [todo-id toggled]
  [:request
   (fn [_ _]
     {:query {:update [:todos]
              :where  [:= :todos.id todo-id]
              :set    {:marked toggled}}})])
