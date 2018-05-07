(ns myapp.handlers.login
  (:require [myapp.components.queries :refer [db]]
            [myapp.handlers.util :refer [dispatch]]
            [myapp.pages.layout :as layout]
            [myapp.shared.route-map :as r]
            [buddy.hashers :as hashers]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :as res]))

;; Login

(defn login-page [{:keys [error username]}]
  [:div#login-form
   [:h2 "Login"]
   (when error
     [:h3.error error])
   [:form {:method "POST" :action "/login"}
    (anti-forgery-field)
    [:label "Username:"
     [:input {:type "text"
              :name "username"
              :value username}]]
    [:br]
    [:label "Password:"
     [:input {:type "password"
              :name "password"}]]
    [:input {:type "submit" :value "Submit"}]]])

(defn get-login [req]
  (layout/render (layout/base-template (login-page {}))))

(defn authenticate [username password]
  (let [u (db :get-user-by-name {:username username})
        unauthed [false {:message "Invalid useranme of password"}]]
    (if (and u (hashers/check password (:password u)))
      [true {:user (dissoc u :password)}]
      unauthed)))

(defn post-login [{:keys [params session]}]
  (let [username (:username params)
        password (:password params)
        [authenticated? data] (authenticate username password)]
    (if authenticated?
      (let [new-session (assoc session
                               :identity (-> data :user :username)
                               :user-id (-> data :user :id)
                               :role (if (-> data :user :is_admin)
                                       "admin"
                                       "editor"))]
        (-> (res/redirect (r/to :admin/home))
            (assoc :session new-session)))
      (layout/render
       (layout/base-template
        (login-page {:username username
                     :error (:message data)})
        {})))))

;; Logout

(defn logout [req]
  (-> (res/redirect "/")
      (assoc :session {})))

;; Handler Ma

(defn handlers [kw]
  (case kw
    :login/login (dispatch {:get get-login
                            :post post-login})
    :login/logout logout
    false))
