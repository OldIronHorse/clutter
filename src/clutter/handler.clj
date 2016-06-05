(ns clutter.handler
  (:use compojure.core
        ring.middleware.json)
  (:require [compojure.route :as route]
            [ring.util.response :refer [response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.logger :as logger]
            [clutter.core :refer :all]
            [clojure.tools.logging :as log]))

(defn init []
  (connect))

(defn destroy []
  (disconnect))

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
