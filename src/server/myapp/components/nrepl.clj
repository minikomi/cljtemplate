(ns myapp.components.nrepl
  (:require [mount.core :refer [defstate] :as mount]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            [clojure.tools.nrepl.server :as nrepl-server]))

(defn nrepl-handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))

(mount/defstate ^{:on-reload :noop}
  nrepl
  :start
  (let [nrepl-port (env :nrepl-port 9091)
        nrepl-bind (env :nrepl-bind "0.0.0.0")]
    (require 'cider.nrepl)
    (ns-resolve 'cider.nrepl 'cider-nrepl-handler)
    (timbre/info (str "Starting nrepl on port " nrepl-port))
    (nrepl-server/start-server
     :port nrepl-port
     :init-ns 'myapp.repl
     :handler (nrepl-handler)
     :bind nrepl-bind))
  :stop
  (when nrepl
    (nrepl-server/stop-server nrepl)))
