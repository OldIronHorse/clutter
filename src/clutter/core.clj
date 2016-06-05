(ns clutter.core
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [monger.core :as mg]
            [monger.conversion :as mconv]
            [monger.collection :as mc])
  (:import org.bson.types.ObjectId))

;;TODO clutter.core-test...
;; Ugly, but works for prod and test...
(def conn (atom nil))

(defn connect []
  (log/info "init. Connecting to mongodb...")
  (swap!
    conn
    (fn [c]
      (if (nil? c) (mg/connect) c))))

(defn disconnect []
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

