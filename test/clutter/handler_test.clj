(ns clutter.handler-test
  (:use clojure.test
        ring.mock.request
        clutter.handler)
  (:require [monger.core :as mg]
            [monger.conversion :as mconv]
            [monger.collection :as mc]
            [clojure.data.json :as json])
  (:import org.bson.types.ObjectId))

(deftest test-app-routes
  (with-redefs
    [mg/get-db (fn [conn db-name]
                  (is (= "clutter" db-name))
                  nil)]
    (testing "users endpoint, empty"
      (with-redefs
        [mc/find-maps (fn [db coll query]
                        (is (= "users" coll))
                        (is (= {} query))
                        '())]
        (let [response (app-routes (request :get "/users"))]
          (is (= 200 (:status response)))
          (is (= '() (:users response))))))
    (testing "users endpoint, multiple users"
      (with-redefs
        [mc/find-maps (fn [db coll query]
                        (is (= "users" coll))
                        (is (= {} query))
                        [{:name "user1"
                          :_id (ObjectId. "111111111111111111111111")}
                         {:name "user2"
                          :_id (ObjectId. "222222222222222222222222")}])]
        (let [response (app-routes (request :get "/users"))]
          (is (= 200 (:status response)))
          (is (= '({:name "user1", :_id "111111111111111111111111"}
                   {:name "user2", :_id "222222222222222222222222"})
                 (:users response))))))
    (testing "users endpoint, query by name"
      (with-redefs
        [mc/find-maps (fn [db coll query]
                        (is (= "users" coll))
                        (is (= {:name "Bill"} query))
                        [{:name "Bill"
                          :_id (ObjectId. "111111111111111111111111")}])]
      (let [response (app
                      (request :get "/users" {:name "Bill"}))]
        (is (= 200 (:status response)))
        (is (= '({:name "Bill", :_id "111111111111111111111111"})
              (:users response))))))
    (testing "users endpoint, add user"
      (with-redefs
        [mc/insert-and-return (fn [db coll fields]
                                (is (= "users" coll))
                                (is (= "Bill" (:name fields)))
                                (is (not (nil? (:_id fields))))
                                fields)]
        (let [response
                (app-routes
                  (-> (request :post "/users")
                      (content-type "application/json")
                      (assoc :body {:name "Bill"})))]
          (is (= 200 (:status response)))
          (is (not (nil? (-> response :body :_id))))
          (is (string? (-> response :body :_id)))
          (is (= "Bill" (-> response :body :name))))))
    (testing "users endpoint, get user by id"
      (with-redefs
        [mc/find-one-as-map (fn [db coll query]
                              (is (= "users" coll))
                              (is (= {:_id (ObjectId. "111111111111111111111111")}
                                     query))
                              {:name "user1"
                               :_id (ObjectId. "111111111111111111111111")})]
        (let [response (app-routes
                          (request :get "/users/111111111111111111111111"))]
          (is (= 200 (:status response)))
          (is (= "111111111111111111111111" (-> response :body :_id)))
          (is (= "user1" (-> response :body :name))))))
    (testing "conversations endpoint, empty"
      (with-redefs
        [mc/find-maps (fn [db coll query]
                        (is (= "conversations" coll))
                        (is (= {} query))
                        '())]
        (let [response (app-routes (request :get "/conversations"))]
          (is (= 200 (:status response)))
          (is (= '() (-> response :body :conversations))))))
    (testing "conversations endpoint, multiple empty conversations"
      (with-redefs
        [mc/find-maps (fn [db coll query]
                        (is (= "conversations" coll))
                        (is (= {} query))
                        [{:by "user1", :title "User 1's Words"
                          :_id (ObjectId. "111111111111111111111111")}
                         {:by "user2", :title "Some words from User2"
                          :_id (ObjectId. "222222222222222222222222")}])]
        (let [response (app-routes (request :get "/conversations"))]
          (is (= 200 (:status response)))
          (is (= '({:by "user1", :title "User 1's Words"
                    :_id "111111111111111111111111"}
                   {:by "user2", :title "Some words from User2"
                    :_id "222222222222222222222222"})
                 (-> response :body :conversations))))))
    (testing "non-existent endpoint"
      (let [response (app-routes (request :get "/theres_nothing_to_see_here"))]
        (is (= 404 (:status response)))))))
