(ns clutter.handler-test
  (:use clojure.test
        ring.mock.request
        clutter.handler))

(deftest test-app
  (testing "users endpoint"
    (let [response (app (request :get "/users"))]
      (is (= 200 (:status response)))
      (is (= 
            "application/json; charset=utf-8"
            (get-in response [:headers "Content-Type"])))))
  (testing "conversations endpoint"
    (let [response (app (request :get "/conversations"))]
      (is (= 200 (:status response)))
      (is (= 
            "application/json; charset=utf-8" 
            (get-in response [:headers "Content-Type"])))))
  (testing "non-existent endpoint"
    (let [response (app (request :get "/theres_nothing_to_see_here"))]
      (is (= 404 (:status response))))))
