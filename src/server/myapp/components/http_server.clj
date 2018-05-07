(ns myapp.components.http-server
  (:require [myapp.components.handler :refer [wrapped-handler]]
            [mount.core :refer [defstate]]
            [environ.core :refer [env]]
            [org.httpkit.server :as ohs]
            [taoensso.timbre :as timbre]))

;; mounting

(defn- stop-server! [server]
  (server :timeout 100))

(defstate server
  :start (do
           (timbre/info "Server started on port " (env :http-port 3000))
           (ohs/run-server #'wrapped-handler {:port (Integer. (env :http-port "3000"))}))
  :stop (stop-server! server))
