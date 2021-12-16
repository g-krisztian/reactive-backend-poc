(ns poc.config.core
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [poc.util :as util]))

(def x-name (comp keyword name))
(def x-namespace (comp keyword namespace))

(def sys-env
  (->> (merge {} (System/getProperties) (System/getenv))
       (map (fn [[k v]]
              [(-> (str/lower-case k)
                   (str/replace "__" "/")
                   (str/replace "_" "-")
                   (str/replace "." "-")
                   keyword) v]))
       (into {})))

(def config-file
  (some-> (io/resource "config.edn") slurp edn/read-string))

(defn flat->deep
  [m]
  (let [qualified (filter #(qualified-keyword? (first %)) m)
        un-q (into {} (remove #(qualified-keyword? (first %)) m))
        gr (group-by (fn [[k _]] (x-namespace k)) qualified)]
    (into un-q (map (fn [[g vs]]
                      (let [ms (into {} (map (fn [[k v]] [(x-name k) v]) vs))]
                        [g ms]))
                    gr))))

(defn config*
  [& override]
  {:pre (even? (count override))}
  (let [deep-system (flat->deep sys-env)
        deep-file (flat->deep config-file)
        deep-override (flat->deep (apply hash-map override))]
    (util/deep-merge deep-file deep-system deep-override)))

(def config
  "Customized config resolver for support both name-spaced and deep configuration maps"
  (memoize config*))
