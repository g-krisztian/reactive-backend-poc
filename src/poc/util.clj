(ns poc.util)

(defn deep-merge [& maps]
  (apply merge-with
         (fn [& args]
           (if (every? map? args)
             (apply deep-merge args)
             (last args)))
         maps))

(defn unpack-var
  [var]
  (if (var? var) @var var))
