(ns poc.router
  (:require [clojure.edn :as edn]))

(defn- read-val
  [val]
  (edn/read-string (subs val 1)))

(defn- init [act val]
  (map (fn [a]
         (if (fn? a) (a (read-val val)) a)) act))

(defn router
  [routes uri method]
  (loop [routes routes uri-seq (re-seq #"\/[\-a-zA-Z0-9._~!$&'()*+,;=:@]*" uri) actions []]
    (if (empty? uri-seq)
      actions
      (let [parameter (re-find #"/:.*" (ffirst routes))
            route (filter #(#{(or parameter (first uri-seq))} (first %)) routes)
            [[uri act & sub-routes]] route
            act (if (map? act) (get act method) act)]
        (if (and uri act)
          (recur sub-routes (rest uri-seq) (into actions (init act (first uri-seq))))
          (throw (ex-info "Not found" {:status 404 :body "not found"})))))))
