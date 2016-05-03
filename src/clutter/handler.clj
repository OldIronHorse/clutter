(ns clutter.handler
  (:use compojure.core
        ring.middleware.json)
  (:require [compojure.handler :as handler]
            [ring.util.response :refer [response]]
            [compojure.route :as route]))

(defroutes app-routes
  (GET "/users" [] (response {}))
  (GET "/conversations" [] (response {}))
  (route/not-found (response {:message "Page not found"})))

(def app 
  (-> 
    app-routes
    wrap-json-response
    wrap-json-body))
