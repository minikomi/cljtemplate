(ns myapp.styles.core
  (:require
   [myapp.styles.util :as u]
   [garden.selectors :as gs]
   [garden.units :as units :refer [percent px em]]))

(def combined
  [:html
   {:margin 0
    :padding 0
    :height (percent 100)}
   [:body
    {:margin 0
     :height (percent 100)
     :width (percent 100)
     :padding 0}
    [:pre {:display 'none}]]])
