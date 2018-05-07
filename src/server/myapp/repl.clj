(ns myapp.repl
  (:require
   [myapp.components.http-server]
   [myapp.components.hikari-cp :refer [query]]
   [myapp.components.logger]
   [myapp.components.queries :as q :refer [db]]
   [myapp.components.nrepl :as n]
   [myapp.components.handler :as h]
   [environ.core :refer [env]]
   [mount.core :as mount]))

(defn start! []
  (->
   (mount/except [#'n/nrepl])
   (mount/start)))

(defn stop! []
  (->
   (mount/except [#'n/nrepl])
   (mount/stop)))

(defn restart! []
  (stop!)
  (start!))

(comment
  (start!)
  (stop!)
  (restart!))
