(ns clutter.handler
  (:use compojure.core
        ring.middleware.json)
  (:require [compojure.route :as route]
            [ring.util.response :refer [response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.logger :as logger]
            [monger.core :as mg]
            [monger.conversion :as mconv]
            [monger.collection :as mc]
            [clojure.tools.logging :as log])
  (:import org.bson.types.ObjectId))

;; Ugly, but works for prod and test...
(def conn (atom nil))

(defn init []
  (log/info "init. Connecting to mongodb...")
  (swap!
    conn
    (fn [c]
      (if (nil? c) (mg/connect) c))))

(defn destroy []
  (swap!
    conn
    (fn[c]
      (if (nil? c) c (mg/disconnect @conn)))))

(defn with-db [op & args]
  (log/info "with-db:" (str op) (str args))
  (let
    [db (mg/get-db @conn "clutter")
     result (apply op db args)]
    (log/info "with-db: result:" (str (vec result)))
    result))

(defn ids-to-str [l]
  (map #(update %1 :_id str) l))

(defn conversations []
  (ids-to-str (with-db mc/find-maps "conversations" {})))

(defn users [params]
  (ids-to-str (with-db mc/find-maps "users" (if (nil? params) {} params))))

(defn create-user [username]
  (let
    [new-user (with-db mc/insert-and-return "users"
                {:name username, :_id (ObjectId.)})]
    (update new-user :_id str)))

(defn user-by-id [_id]
  (update (with-db mc/find-one-as-map "users" {:_id (ObjectId. _id)}) :_id str))

(defroutes app-routes
  (GET "/users" {params :params} (response {:users (users params)}))
  (GET "/users/:_id" [_id] (response  (user-by-id _id)))
  (POST "/users" request (response (create-user (get-in request [:body :name]))))
  (GET "/conversations" [] (response {:conversations (conversations)}))
  (route/not-found (response {:message "Page not found"})))

(defn wrap-exception-handling [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/info "wrap-exception-handling" (str e))
        {:status 404, :body "Item not found"}))))

(def app
  (-> app-routes
    logger/wrap-with-logger
    (wrap-json-body {:keywords? true})
    wrap-json-response
    wrap-exception-handling
    (wrap-defaults api-defaults)))
