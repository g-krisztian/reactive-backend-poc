(ns poc.actions.todos)

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
     (let [id (rand-int Integer/MAX_VALUE)]
       {:query {:insert-into [:todos]
                :values      [(->todos (assoc params :id id))]}}))])

(defn select-todo
  [todo-id]
  [:request
   (fn [_ _]
     {:query {:where [:= :id todo-id]}})])

(defn toggle-todo
  [todo-id]
  [:request
   (fn [_ _]
     {:query {:update [:todos]
              :where  [:= :id todo-id]
              :set    {:marked [:not :marked]}}})])

(defn set-todo
  [todo-id toggled]
  [:request
   (fn [_ _]
     {:query {:update [:todos]
              :where  [:= :todos.id todo-id]
              :set    {:marked toggled}}})])
