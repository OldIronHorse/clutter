(defproject clutter "0.1.0-SNAPSHOT"
  :description "Chatroom webservice"
  :url "https://github.com/OldIronHorse/clutter.git"
  :license {:name "GNU Public License v3"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.5.0"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler clutter.handler/app
         :nrepl {:start? true
                 :port 9998}}
  :profiles 
    {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                          [ring-mock "0.1.5"]]}})