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

(defroutes app-routes
  (GET "/users" [] (response {:users (users)}))
  (POST "/users" request (response (create-user (get-in request [:body :name]))))
  (GET "/conversations" [] (response {:conversations (conversations)}))
  (route/not-found (response {:message "Page not found"})))

(def app
  (->
    app-routes
    (wrap-json-body {:keywords? true})
    wrap-json-response))
