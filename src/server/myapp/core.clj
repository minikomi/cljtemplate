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
  (cond
    (some #{"init"} args)
    (do
      (timbre/info "Initializing DB")
      (mount/start (mount/except [#'n/nrepl
                                  #'s/server
                                  #'h/wrapped-handler]))
      (db/migrate)
      (let [console (. System console)
            username (do (print "Admin Username: ") (flush) (read-line))
            pwd (String/valueOf (.readPassword (System/console) "Password:" nil))]
        (db/create-user! username pwd))
      (System/exit 0))
    (some #{"migrate" "rollback"} args)
    (do
      (mount/start)
      (db/rollback)
      (System/exit 0))
    :else
    (do (timbre/info "M O U N T I N G")
        (mount/start))))
