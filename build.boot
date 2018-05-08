(set-env!
 :resource-paths #{"resources"}
 :source-paths #{"src/server" "src/frontend" "src/tasks"}
 :dependencies '[;; pin depenencies
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 ;; server
                 [bidi "2.1.3" :exclusions [ring/ring-core]]
                 [buddy "2.0.0"]
                 [cider/cider-nrepl "0.17.0-SNAPSHOT"]
                 [com.rpl/specter "1.1.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.2.0"]
                 [me.raynes/fs "1.4.6"]
                 [metosin/ring-http-response "0.9.0"
                  :exclusions [clj-time]]
                 [clj-time "0.14.2"]
                 [prone "1.5.0"]
                 [ring "1.6.3"]
                 [ring-middleware-format "0.7.2"]
                 [ring/ring-defaults "0.3.1"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 ;; logging
                 [com.taoensso/timbre "4.10.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 ;; system management
                 [mount "0.1.11"]
                 [environ "1.1.0"]
                 [com.cemerick/piggieback "0.2.2"]
                 ;; database
                 [hikari-cp "2.2.0"]
                 [com.layerware/hugsql "0.4.8"]
                 [org.postgresql/postgresql "42.2.2"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [ragtime "0.7.2"]
                 [funcool/struct "1.2.0"]
                 ;; boot
                 [adzerk/boot-cljs "2.1.4"                  :scope "boot"
                  :exclusions [org.clojure/clojurescript]]
                 [adzerk/boot-cljs-repl "0.3.3"             :scope "boot"]
                 [poyo.co/boot-create-html "0.1.1-SNAPSHOT" :scope "boot"]
                 [adzerk/boot-reload "0.5.2"                :scope "boot"]
                 [boot-environ "1.1.0"                      :scope "boot"]
                 [samestep/boot-refresh "0.1.0"             :scope "boot"]
                 [weasel "0.7.0"                            :scope "boot"]
                 [danielsz/boot-autoprefixer "0.1.0"        :scope "boot"]
                 ;; frontend
                 [com.andrewmcveigh/cljs-time "0.5.1"       :scope "cljs"]
                 [day8.re-frame/http-fx "0.1.5"             :scope "cljs"]
                 [garden "1.3.3"                            :scope "cljs"]
                 [re-frame "0.10.4"                         :scope "cljs"]
                 [re-frisk "0.5.3"                          :scope "cljs" :exclusions [ring/ring-core]]
                 [reagent "0.7.0"                           :scope "cljs"]
                 [venantius/accountant "0.2.3"              :scope "cljs" :exclusions [org.clojure/core.async]]])

(require
 '[adzerk.boot-cljs :refer [cljs]]
 '[environ.boot :refer [environ]]
 '[garden :refer [build-garden]]
 '[myapp.core]
 '[adzerk.boot-reload :refer [reload]]
 '[samestep.boot-refresh :refer [refresh]]
 '[clojure.java.io :as io]
 '[clojure.string :as s]
 '[myapp.repl]
 '[boot.util :as util]
 '[hiccup.page :as hp]
 '[danielsz.autoprefixer :refer [autoprefixer]]
 '[mount.core :as mount]
 '[environ.core :refer [env]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

(def prod-environment {:http-port "3000"
                       :dir-static "static"
                       :dir-migrations "resources/migrations"
                       :db-adapter "postgresql"
                       })

(deftask prod-options []
  (task-options!
   pom {:project 'myapp
        :version "0.1.1"}
   aot {:namespace #{'myapp.core}}
   jar {:main 'myapp.core
        :file "myapp.jar"}
   uber {:exclude-scope #{"cljs" "boot"}}
   ;;frontend
   cljs {:optimizations :advanced
         :compiler-options
         {:closure-defines {'goog.DEBUG false}
          :parallel-build true}}
   autoprefixer {:files ["styles.css"]})
  (environ :env prod-environment))

(def dev-environment {:debug "true"
                      :http-port "3000"
                      :dir-static "static"
                      :dir-migrations "resources/migrations"
                      :db-adapter "postgresql"
                      :db-url "jdbc:postgresql://localhost:5432/shouter"})

(deftask dev-options []
  (task-options! cljs {:optimizations :none
                       :source-map true
                       :compiler-options
                       {:closure-defines {'goog.DEBUG true}
                        :parallel-build true}})
  (environ :env dev-environment))

(deftask cider "CIDER profile" []
  (alter-var-root #'clojure.main/repl-requires conj
                  '[myapp.repl :refer [start! stop! restart!]])
  (require 'boot.repl)
  (swap! @(resolve 'boot.repl/*default-dependencies*)
         concat '[[cider/cider-nrepl "0.16.0-SNAPSHOT"]
                  [refactor-nrepl "2.4.0-SNAPSHOT"]])
  (swap! @(resolve 'boot.repl/*default-middleware*)
         concat '[cider.nrepl/cider-middleware
                  refactor-nrepl.middleware/wrap-refactor])
  (repl :server true))

(deftask build-styles []
  (comp
   (build-garden :styles-var 'myapp.styles.core/combined
                 :output-to "public/css/styles.css"
                 :css-prepend []
                 :auto-prefix #{:cursor :transform}
                 :pretty-print (:debug env false))))

(deftask build-frontend []
  (comp
   (build-styles)
   (cljs)))

(deftask dev []
  (comp
   (cider)
   (dev-options)
   (cljs-repl)
   (watch :verbose true)
   (refresh)
   (reload :asset-path "/public")
   (build-frontend)))

(deftask run
  [m main-namespace NAMESPACE str "main namespace"
   a arguments      EXPR      [edn] "optional arguments"]
  (with-pre-wrap fs
    (require (symbol main-namespace) :reload)
    (if-let [f (resolve (symbol main-namespace "-main"))]
      (apply f arguments)
      (throw (ex-info "No -main method found"
                      {:main-namespace main-namespace})))
    fs))

(deftask prod-run [i init bool "should init"]
  (comp
   (prod-options)
   (if init identity (build-frontend))
   (run
    :main-namespace "myapp.core"
    :arguments (when init ["init"]))
   (wait)))

(deftask build-dist []
  (comp
   (aot)
   (pom)
   (uber)
   (jar)))

;; lein based uber

(defn- allowed-scope-filter [dep]
  (if-let [i (.indexOf dep :scope)]
    (not (contains? #{"boot"
                      "cljs"
                      "test"}
                    (get dep (inc i))))
    true))

(defn- generate-lein-project-file! [& {:keys [keep-project] :or {keep-project true}}]
  (require 'clojure.java.io)
  (let [pfile ((resolve 'clojure.java.io/file) "project.clj")
        ;; Only works when pom options are set using task-options!
        {:keys [project version]} (:task-options (meta #'boot.task.built-in/pom))
        {:keys [main]} (:task-options (meta #'boot.task.built-in/jar))
        prop #(when-let [x (get-env %2)] [%1 x])
        head (list* 'defproject (or project 'boot-project) (or version "0.0.0-SNAPSHOT")
                    (concat
                     (prop :url :url)
                     (prop :license :license)
                     (prop :description :description)
                     [:dependencies (filter allowed-scope-filter (get-env :dependencies))
                      :repositories (get-env :repositories)
                      :source-paths (vec (concat (get-env :source-paths)
                                                 (get-env :resource-paths)))]
                     [:main main
                      :aot [main]]))
        proj (pp-str head)]
    (if-not keep-project (.deleteOnExit pfile))
    (spit pfile proj)))

(deftask lein-generate!
  "Generate a leiningen `project.clj` file.
   This task generates a leiningen `project.clj` file based on the boot
   environment configuration, including project name and version (generated
   if not present), dependencies, and source paths. Additional keys may be added
   to the generated `project.clj` file by specifying a `:lein` key in the boot
   environment whose value is a map of keys-value pairs to add to `project.clj`."
  []
  (with-pass-thru fs (generate-lein-project-file! :keep-project true)))

(deftask lein-uberjar! []
  (with-pass-thru _
    (let [{:keys [project version]} (:task-options (meta #'boot.task.built-in/pom))]
      (util/dosh "lein" "uberjar")
      (util/dosh "rm" "-rf" "target/stale")
      (util/dosh "rm" "-rf" "target/classes")
      (util/dosh "rm" (str "target/" project "-" version ".jar")))))

(deftask build-lein-jar []
  (comp (lein-generate!)
        (lein-uberjar!)))

(deftask build-spit-frontend []
  (comp
   (build-frontend)
   (sift :include #{#"^public/"})
   (sift :invert true :include #{#"^out/"})
   (sift :include #{#"\.out"} :invert true)
   (target :dir #{"target"}
           :no-clean true)))

(deftask build []
  (comp
   (prod-options)
   (build-lein-jar)
   ;; frontend
   (build-spit-frontend)))
