(ns poc.router
  (:require
    [clojure.edn :as edn]
    [poc.util :as util]))

(defn init
  [actions parameters]
  (map (fn [[k & v]]
         (if (fn? k)
           (apply (util/unpack-var k) (map parameters v))
           (reduce conj [k] v)))
       actions))

(defn uri->seq
  [uri]
  (re-seq #"\/[\-a-zA-Z0-9._~!$&'()*+,;=:@]*" uri))

(defn extract
  [uri]
  (->> uri
       (re-find #"\/([\-a-zA-Z0-9._~!$&'()*+,;=:@]*)")
       second
       edn/read-string))

(defn uri->regex
  [uri]
  (re-pattern
    (apply str
           (concat
             (map
               (fn [v] (if (keyword? (extract v)) "/([-a-zA-Z0-9._~!$&'()*+,;=:@]*)" v))
               (uri->seq uri))
             ["$"]))))

(defn uri->keys
  [uri]
  (reduce
    (fn [acc v]
      (if (keyword? (extract v))
        (conj acc v)
        acc))
    [] (uri->seq uri)))

(defn uri-match
  [[route act] uri]
  (let [m (re-find (uri->regex route) uri)
        ks (seq (uri->keys route))]
    (cond
      (nil? m) nil
      (nil? ks) [act {}]
      :else [act (into {} (map (fn [k v]
                                 [(extract k) (edn/read-string v)])
                               ks (rest m)))])))

(defn ppr
  ([routes]
   (reduce
     (fn [acc route]
       (let [[uri act & sub-routes] route]
         (cond
           (empty? sub-routes) (conj acc [uri act sub-routes])
           :else (let [sr (mapv (fn [[u a & s]]
                                  (concat [(str uri u) (concat act a)] s)) sub-routes)]
                   (concat acc [[uri act]] (ppr sr))))))
     [] routes)))

(defn not-found
  []
  (throw (ex-info "Not found" {:status 404 :body "not found"})))

(defn router
  [routes]
  (let [length-grouped-routes (group-by #(-> % first uri->seq count) (ppr (util/unpack-var routes)))]
    (fn [uri method]
      (let [matching-routes (->> uri uri->seq count
                                 (get length-grouped-routes)
                                 (map #(uri-match % uri))
                                 (remove nil?))]
        (case (count matching-routes)
          0 (not-found)
          1 (let [[[act params]] matching-routes
                  acts (->> (mapcat (fn [[k v]]
                                      (if (and k (fn? v))
                                        [[k v]]
                                        (when (= k method) v))) act)
                            (remove nil?)
                            (map util/unpack-var)
                            seq)]
              (if acts (init acts params) (not-found)))
          (throw (ex-info "Invalid routing" {:status 500 :body "Invalid routing configuration"})))))))
