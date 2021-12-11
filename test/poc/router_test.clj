(ns poc.router-test
  (:require [clojure.test :refer :all]
            [poc.router :refer [router]]
            [poc.routes :refer :all]))

(deftest router-test
  (is (= [posts view-all] (router routes "/todo" :get)))
  (is (thrown? Exception (router routes "/todo" :post)))
  (is (= api (router routes "/api" :get)))
  (is (= api (router routes "/api" :post)))
  (is (= api (router routes "/api" :delete)))
  (is (= (concat api message) (router routes "/api/message" :get)))
  (is (= (concat api context) (router routes "/api/ctx" :get)))
  (is (thrown? Exception (router routes "/api/ctx/something" :post)))
  (is (= (conj api image) (router routes "/api/image" :get))))

