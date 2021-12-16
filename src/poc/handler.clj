(ns poc.handler
  (:require
    [poc.router :refer [router]]
    [poc.util :refer [deep-merge]]))

(defn watcher-fn
  "If key referenced value changed, then executes all functions subscribed to the key"
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
  "Delivers the response to a promise, when its appears."
  [response-promise]
  [:response
   (fn [_ new-state]
     (deliver response-promise new-state)
     nil)])

(defn subscribe
  "Collecting actions and watcher keywords."
  [subscribers [k f]]
  (let [subs (get subscribers k [])]
    (assoc subscribers k (conj subs f))))

(defn handler
  "Get actions from routers and subscribes those to an atom."
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
             (deref response-promise 6000 {:status 500
                                           :body   "request timed out"}))
           (catch Exception e (or (ex-data e)
                                  {:status 500
                                   :body (.getMessage e)}))))))
