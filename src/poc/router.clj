(ns poc.router
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

(defn- read-val
  [val]
  (edn/read-string (subs val 1)))

(defn- init [act val]
  (map (fn [a]
         (if (fn? a) (a (read-val val)) a)) act))

(defn router
  [routes uri method]
  (loop [routes routes uri-seq (re-seq #"\/[\-a-zA-Z0-9._~!$&'()*+,;=:@]*" uri) actions []]
    (let [parameter (when (seq routes) (re-find #"/:.*" (ffirst routes)))
          route (filter #(#{(or parameter (first uri-seq))} (first %)) routes)
          [[uri act & sub-routes]] route
          act (if (map? act) (get act method) act)]
      (cond
        (empty? uri-seq) actions
        (and uri act) (recur sub-routes (rest uri-seq) (into actions (init act (first uri-seq))))
        :else (throw (ex-info "Not found" {:status 404 :body "not found"}))))))