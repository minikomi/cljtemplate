(ns myapp.db
  (:require [myapp.components.queries :as q]
            [buddy.hashers :as hashers]
            [environ.core :refer [env]]
            [ragtime.jdbc :as r-jdbc]
            [ragtime.repl :as repl]
            [taoensso.timbre :as timbre]))

(defn- load-config []
  {:datastore (r-jdbc/sql-database (env :database-url))
   :migrations (r-jdbc/load-resources "migrations")})

(defn generate [generate-name]
  (let [curr (.format
              (java.text.SimpleDateFormat. "yyyyMMddHHmmss")
              (java.util.Date.))
        migration-root (str (:dir-migrations env) "/" curr "-" generate-name)]
    (spit (str migration-root ".up.sql")
          "-- migration to be applied\n\n")
    (spit (str migration-root ".down.sql")
          "-- rolling back recipe\n\n")
    (timbre/info "Creating" migration-root)))

(defn migrate []
  (repl/migrate (load-config)))

(defn rollback []
  (repl/rollback (load-config)))

(defn create-user! [username pwd]
  (q/db :create-user! {:username username
                       :password (hashers/derive pwd)}))
