(ns poc.router-test
  (:require [clojure.test :refer :all]
            [poc.router :refer [router]]
            [poc.routes :refer :all]))

(def router* (router routes))

(deftest router-test
  (is (= [posts view-all] (router* "/todo" :get)))
  (is (thrown-with-msg? Exception #"Not found" (router* "/todo" :post)))
  (is (= api (router* "/api" :get)))
  (is (= api (router* "/api" :post)))
  (is (= api (router* "/api" :delete)))
  (is (= (concat api message) (router* "/api/message" :get)))
  (is (= (concat api context) (router* "/api/ctx" :get)))
  (is (thrown-with-msg? Exception #"Not found" (router* "/api/ctx/something" :post)))
  (is (= (conj api image) (router* "/api/image" :get))))
