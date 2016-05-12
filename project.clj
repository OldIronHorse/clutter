(defproject clutter "0.1.0-SNAPSHOT"
  :description "Chatroom webservice"
  :url "https://github.com/OldIronHorse/clutter.git"
  :license {:name "GNU Public License v3"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.2.0"]
                 [ring.middleware.logger "0.5.0"]
                 [compojure "1.5.0"]
                 [com.novemberain/monger "3.0.2"]
                 [org.clojure/tools.logging "0.2.4"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:init clutter.handler/init
         :destroy clutter.handler/destroy
         :handler clutter.handler/app
         :nrepl {:start? true
                 :port 9998}}
  :profiles 
    {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                          [org.clojure/data.json "0.2.6"] 
                          [ring-mock "0.1.5"]]}})
