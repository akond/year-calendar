(ns year-calendar-2.svg
	(:require [clojure.string :as string]
			  [year-calendar-2.primitive :as primitive]
			  [goog.math.Box]
			  [goog.math.Rect]
			  [goog.math.Coordinate]
			  [cljs.test :refer-macros [is]]))

(def A4-width 210)

(def A4-height 297)

(defn inject-attrs [hiccup-node attrs]
	(if (map? (get hiccup-node 1))
		(update hiccup-node 1 (partial merge attrs))
		(primitive/insert-at hiccup-node 1 [attrs])))

(defn polyline [path]
	(let [value (if (string? path) path (string/join " " (flatten path)))]
		[:path {:stroke "blue" :fill "none" :stroke-width "0.025px" :d value}]))

(defn text-box [x y width height text & [attr]]
	(let [font-size (/ height 2.2)
		  coefficient (* (/ 1 font-size) 12)
		  default-attr {:x           x
						:y           y
						:text-anchor "middle"
						:dx          (/ width 2)
						:dy          (+ (/ height 2) (/ height coefficient 2))
						:font-family "Arial"
						:font-size   font-size}]
		[:text
		 (merge default-attr attr)
		 text]))


(defn box [box & [attr]]
	{:pre [(is (number? (.-left box)))
		   (is (number? (.-right box)))
		   (is (number? (.-top box)))
		   (is (number? (.-bottom box)))]}

	[:path
	 (merge
		 {:stroke-linecap "square"
		  :d              (str "M" (.-left box) "," (.-top box)
							   "L" (.-right box) "," (.-top box)
							   "L" (.-right box) "," (.-bottom box)
							   "L" (.-left box) "," (.-bottom box)
							   "L" (.-left box) "," (.-top box))}
		 attr)])


(defn rectangle [r]
	[:rect {:x      (.-left r)
			:y      (.-top r)
			:width  (.-width r)
			:height (.-height r)}])


(defn rect-text [rect text & [text-attr rect-attr]]
	(let [font-size (/ (.-height rect) 2.2)
		  coefficient (* (/ 1 font-size) 12)
		  center (.getCenter rect)
		  default-attr {:x           (.-x center)
						:y           (.-top rect)
						:text-anchor "middle"
						:dy          (+ -0.2 (/ font-size 0.62))
						:font-family "Arial"
						:font-size   font-size}]

		[:g
		 (when-let [rect-attr rect-attr]
			 (-> (rectangle rect)
				 (inject-attrs
					 (merge {:fill         "white"
							 :stroke       "black"
							 :stroke-width 0.5} rect-attr)))
			 )

		 [:text (merge default-attr text-attr {;:length-adjust "spacingAndGlyphs"
											   ;:text-length   (.-width rect)
											   }) text]]
		))


(defn whole-page [attr & body]
	(into []
		  (concat
			  [:svg (merge {:width    (str A4-width "mm")
							:height   (str A4-height "mm")
							:view-box (clojure.string/join " " [0 0 A4-width A4-height])}
						   attr
						   )]
			  body)))


(defn page-box [& [w h]]
	(goog.math.Rect. 0 0 (or w A4-width) (or h A4-height)))


(defn page-scaled-box [factor]
	(doto (goog.math.Rect. 0 0 A4-width A4-height)
		(.scale factor)))

(defn center [box within]
	(.translate box (.difference goog.math.Coordinate (.getCenter within) (.getCenter box))))
