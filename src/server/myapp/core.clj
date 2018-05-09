(ns myapp.core
  (:gen-class)
  (:require [myapp.db :as db]
            [myapp.components.http-server :as s]
            [myapp.components.hikari-cp]
            [myapp.components.queries :as q]
            [myapp.components.nrepl :as n]
            [myapp.components.handler :as h]
            [myapp.components.logger]
            [myapp.repl]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [me.raynes.fs :as fs]
            [mount.core :as mount]
            [taoensso.timbre :as timbre]))

(defn -main [& args]
  (do (timbre/info "M O U N T I N G")
      (mount/start)))
