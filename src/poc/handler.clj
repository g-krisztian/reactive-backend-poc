(ns poc.handler
  (:require
    [poc.util :refer [deep-merge]]
    [poc.router :refer [router]]))

(defn watcher-fn
  [dependencies subscribers]
  (fn [key watched old-state new-state]
    (let [oldv (get old-state key)
          newv (get new-state key)]
      (when-not (= oldv newv)
        (let [actions (get subscribers key)
              results (map #(% dependencies newv) actions)
              rs (apply deep-merge new-state results)]
          (reset! watched rs))))))

(defn deliver-response
  [response-promise]
  [:response
   (fn [_ new-state]
     (deliver response-promise new-state)
     nil)])

(defn subscribe
  [subscribers [k f]]
  (let [subs (get subscribers k [])]
    (assoc subscribers k (conj subs f))))

(defn handler
  [dependencies routes]
  (let [router* (router routes)]
    (fn [request]
      (try (let [{:keys [:uri :request-method]} request
                 actions (router* uri request-method)
                 ctx (atom {})
                 response-promise (promise)
                 subscribers (reduce subscribe {} (conj actions (deliver-response response-promise)))]
             (doseq [[k _] subscribers]
               (add-watch ctx k (watcher-fn dependencies subscribers)))
             (reset! ctx {:request request})
             (deref response-promise)) ;1000 {:status 500
                                      ;      :body   "request timed out"))
           (catch Exception e (ex-data e))))))


