(ns clutter.handler-test
  (:use clojure.test
        ring.mock.request
        clutter.handler)
  (:require [monger.core :as mg]
            [monger.conversion :as mconv]
            [monger.collection :as mc]))

(deftest test-app
  (with-redefs
    [mg/get-db (fn [conn db-name]
                  (is (= "clutter" db-name))
                  nil)]
    (testing "users endpoint, empty" 
      (with-redefs
        [mc/find-maps (fn [db coll query projection]
                        (is (= "users" coll))
                        (is (= {} query))
                        (is (= {:_id false} projection))
                        '())]
        (let [response (app-routes (request :get "/users"))]
          (is (= 200 (:status response)))
          (is (= '() (-> response :body :users))))))
    (testing "conversations endpoint, empty"
      (with-redefs
        [mc/find-maps (fn [db coll query projection] 
                        (is (= "conversations" coll))
                        (is (= {} query))
                        (is (= {:_id false} projection))
                        '())]
        (let [response (app-routes (request :get "/conversations"))]
          (is (= 200 (:status response)))
          (is (= '() (-> response :body :conversations))))))
    (testing "non-existent endpoint"
      (let [response (app-routes (request :get "/theres_nothing_to_see_here"))]
        (is (= 404 (:status response)))))))
