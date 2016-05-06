(ns clutter.handler
  (:use compojure.core
        ring.middleware.json)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :refer [response]]
            [ring.middleware.logger :as logger]
            [monger.core :as mg]
            [monger.conversion :as mconv]
            [monger.collection :as mc])
  (:import org.bson.types.ObjectId))

(def conn (atom nil))

(defn init []
  ;; should probably do more checking here
  (swap! conn (fn [_] (mg/connect))))

(defn destroy []
  ;; should probably do more checking here
  (mg/disconnect @conn)
  (swap! conn (fn[_] nil)))

(defn with-db [op & args]
  (let
    [db (mg/get-db @conn "clutter")]
    (apply op db args)))

(defn ids-to-str [l]
  (map #(update %1 :_id str) l))

(defn conversations []
  (ids-to-str (with-db mc/find-maps "conversations" {})))

(defn users []
  (ids-to-str (with-db mc/find-maps "users" {})))

(defn create-user [username]
  (let
    [new-user (with-db mc/insert-and-return "users"
                {:name username, :_id (ObjectId.)})]
    (update new-user :_id str)))

(defn user-by-id [_id]
  (update (with-db mc/find-one-as-map "users" {:_id (ObjectId. _id)}) :_id str))

(defroutes app-routes
  (GET "/users" [] (response {:users (users)}))
  (GET "/users/:_id" [_id] (response  (user-by-id _id)))
  (POST "/users" request (response (create-user (get-in request [:body :name]))))
  (GET "/conversations" [] (response {:conversations (conversations)}))
  (route/not-found (response {:message "Page not found"})))

(defn wrap-exception-handling [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        {:status 404, :body "Item not found"}))))
  

(def app
  (->
    app-routes
    logger/wrap-with-logger
    (wrap-json-body {:keywords? true})
    wrap-json-response
    wrap-exception-handling))
