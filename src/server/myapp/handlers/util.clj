(ns myapp.handlers.util
  (:require [myapp.shared.route-map :as route]
            [myapp.pages.layout :as layout]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [ring.util.http-response :as h-resp]
            [ring.util.response :as res]
            [clojure.pprint :as pprint]))

;; method based dispatch

(defn dispatch [method-map]
  (fn [{:keys [request-method] :as req}]
    (-> (if-let [handler (get method-map request-method false)]
          (handler req)
          (h-resp/method-not-allowed "Method Not allowed"))
        (assoc-in [:headers "Allow"]
                  (map #(-> % name str) (keys method-map))))))

;; auth / restriction

(def unauthorized-page
  (layout/base-template
   [:div
    [:script
     "window.location.href='/login';"]
    [:h1 "You need to" [:a {:href "/login"} "login"] "."]] {}))

(defn unauthorized-handler [request metadata]
  (if (authenticated? request)
    (h-resp/forbidden)
    (-> unauthorized-page
        h-resp/unauthorized
        (h-resp/content-type "text/html ; charset=utf-8"))))

(defn wrap-restricted [handler]
  (fn [req]
    (if (authenticated? req)
      (handler req)
      (throw-unauthorized))))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

(defn wrap-restrict [handler]
  (-> handler
      (wrap-restricted)
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)))

(defn restrict [route-map]
  (into {}
        (map
         (fn [[kw handler]]
           [kw (wrap-restrict handler)])
         route-map)))

(defn wrap-nocache [handler]
  (fn [request]
    (-> (handler request)
        (assoc-in [:headers "Pragma"] "no-cache")
        (assoc-in [:headers "Cache-control"] "no-cache")
        (assoc-in [:headers "Expires"] "-1")
        )))
