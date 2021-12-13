(ns poc.actions.todos
  (:import (java.util UUID)))

(defonce todos-db (atom {}))

(def todos
  [:request
   (fn [_ _]
     {:response-data @todos-db})])

(def view-all
  [:response-data
   (fn [_ rd]
     {:response {:body rd}})])

(def view-one
  [:response-data
   (fn [_ rd]
     {:response {:body rd}})])

(def add-todo
  [:request
   (fn [_ req]
     (let [id (UUID/randomUUID)
           td (get (swap! todos-db assoc id (assoc (:params req) :id id)) id)]
       {:response-data td}))])

(defn get-one-todo
  [todo-id]
  [:request
   (fn [_ _]
     (if-let [td (get @todos-db todo-id)]
       {:response-data td}
       (throw (ex-info "Not found" {:status 404
                                    :body (format "Todo %s not found" todo-id)}))))])

(defn toggle-todo
  [todo-id]
  [:request
   (fn [_ _]
     {:response-data (-> (swap! todos-db update-in [todo-id :marked] not)
                         (get todo-id))})])

(defn set-todo
  [todo-id toggled]
  [:request
   (fn [_ _]
     {:response-data (-> (swap! todos-db assoc-in [todo-id :marked] toggled)
                         (get todo-id))})])