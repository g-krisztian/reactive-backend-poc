(ns poc.database.core
  (:require
    [poc.config.core :as config]
    [migratus.core :as migratus]
    [honey.sql :as sql]
    [next.jdbc :as jdbc]))

(def datasource
  (jdbc/get-datasource (:database (config/config))))

(def execute
  [:query
   (fn [_ query]
     {:db-response (jdbc/execute! datasource (sql/format query) {:return-keys true})})])

(def transaction
  [:query
   (fn [_ query]
     {:db-response (jdbc/with-transaction [tx datasource]
                     (jdbc/execute! tx (sql/format query)))})])

(def mig-cfg
  (let [cfg (config/config)]
    (assoc (:migratus cfg) :db (assoc (:database cfg) :datasource datasource))))

(defn mig-create
  [table]
  (migratus/create mig-cfg table))

mig-cfg

(comment
  (migratus/migrate mig-cfg)
  (migratus/reset mig-cfg))