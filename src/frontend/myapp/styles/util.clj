(ns myapp.styles.util
  (:require [garden.stylesheet :refer [at-media]]
            [garden.units :refer [percent px em]]))

(defmacro defbreakpoint
  [name media-params]
  `(defn ~name [& rules#] (at-media ~media-params [:& rules#])))

(defbreakpoint mobile
  {:screen true
   :max-width (px 767)})

(defbreakpoint not-mobile
  {:screen true
   :min-width (px 768)})

(defn hover-only [& rules]
  (at-media {:all false
             :hover "none"}
            [:&:hover rules]))

(def mobile-margin
  {:width (percent 90)
   :margin [[0 (percent 5)]]})

(def margin-clear
  {:margin 0
   :padding 0})

(def inline-block {:display 'inline-block :vertical-align 'top})

(def mobile-hidden (mobile {:display 'none}))

(def font-sans {:font-family ["Helvetica" "YuGothic" "arial" 'sans-serif]})

(def font-suisse {:font-family ["Suisse" "Helvetica" "YuGothic" "arial"
                                'sans-serif]
                  :letter-spacing (em 0.12)})

(def font-suisse-r {:font-family ["SuisseRegular" "Helvetica" "YuGothic" "arial"
                                  'sans-serif]
                    :letter-spacing (em 0.12)})

(def letter-spacing-0.5
  {:letter-spacing (px 0.5)})
