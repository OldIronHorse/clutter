(ns clutter.handler
  (:use compojure.core
        ring.middleware.json)
  (:require [compojure.handler :as handler]
            [ring.util.response :refer [response]]
            [compojure.route :as route]
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

(defn conversations []
  (with-db mc/find-maps "conversations" {} {:_id false}))

(defn users []
  (with-db mc/find-maps "users" {} {:_id false}))

(defn create-user [name]
  (let
    [new-user (with-db mc/insert-and-return "users" {:name name, :_id (ObjectId.)})]
    (update new-user :_id str)))

(defroutes app-routes
  (GET "/users" [] (response {:users (users)}))
  ;;(POST "/users" request (response (create-user (request :params :name))))
  (POST "/users" request (response (create-user (get-in request [:body :name]))))
  (GET "/conversations" [] (response {:conversations (conversations)}))
  (route/not-found (response {:message "Page not found"})))

(def app
  (->
    (wrap-json-body app-routes {:keywords? true})
    wrap-json-response))
