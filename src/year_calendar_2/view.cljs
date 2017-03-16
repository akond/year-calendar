(ns year-calendar-2.view
	(:require [reagent.core :as reagent]
			  [re-frame.core :as rf]
			  [year-calendar-2.math :as math]
			  [year-calendar-2.svg :as svg]
			  [goog.math.Rect]
			  [goog.math.Box]
			  [goog.math.Coordinate]
			  [goog.date.Date]))

(enable-console-print!)

(def current-year (.getYear (goog.date.Date.)))

(defn query [id f]
	(rf/reg-sub id
				(fn [db v] (f db))))

(defn query-prop [id]
	(rf/reg-sub id
				(fn [db v] (get db id))))

(query-prop :year)
(query-prop :mode)
(query-prop :__figwheel_counter)


(defn dispatch [& id]
	(fn [] (rf/dispatch (vec id))))

(defn js-distatch [& data]
	(fn [e]
		(.stopPropagation e)
		(.preventDefault e)

		(letfn [(substitute-event-data [item]
					(if (symbol? item)
						(aget e (name item))
						item))]
			(rf/dispatch (into [] (map substitute-event-data data))))
		))

(defn divisible-withing-page [big-rect small-rect]
	(letfn [(apu [length quotient]
				(* quotient (quot length quotient)))]

		(goog.math.Rect. (.-left big-rect)
						 (.-top big-rect)
						 (apu (.-width big-rect) (.-width small-rect))
						 (apu (.-height big-rect) (.-height small-rect)))))

(defn year-selector-dialog []
	(let [page (svg/page-box)
		  rect (svg/center (.scale (.clone page) 0.75) page)
		  [W H] [30 10]
		  year @(rf/subscribe [:year])
		  box (.toBox rect)
		  year-rect (goog.math.Rect. 0 0 W H)
		  normalized-rect (divisible-withing-page rect year-rect)
		  normalized-box (.toBox normalized-rect)]

		[:g
		 (svg/box normalized-box
				  {:stroke       "red"
				   :fill         "#eeeeee"
				   :stroke-width "0.15px"})

		 (doall
			 (let [selectors (for [x (range 0 (.-width normalized-rect) W)
								   y (range 0 (.-height normalized-rect) H)]
								 (doto
									 (goog.math.Rect. x y W H)
									 (.translate (goog.math.Coordinate. (.-left rect) (.-top rect)))))
				   total (count selectors)
				   years (range (- year (quot total 2)) (* 2 year))]

				 (map
					 (fn [y r]
						 (-> (svg/rect-text r y {:font-size 5} {:stroke       "CadetBlue"
																:fill         (condp = y
																				  year "Coral"
																				  current-year "Gainsboro"
																				  "GhostWhite")
																:stroke-width 0.1})
							 (svg/inject-attrs {:cursor   "pointer"
												:on-click (js-distatch :new-year y)})
							 (with-meta {:key (str "year-selector-" y)})
							 ))
					 years selectors)
				 ))
		 ]))


(defn header-text []
	[:text
	 {:x           (/ 210 2)
	  :y           13
	  :cursor      "pointer"
	  :text-anchor "middle"
	  :on-wheel    (js-distatch :swap-year 'deltaY)
	  :on-click    (js-distatch :the-year-title-click)}
	 @(rf/subscribe [:year])])


(defn month-box [month-name x y width days week-days-this-month]
	(let [lines 6
		  cell-width (/ width 7)
		  right-x (+ x width)
		  bottom-y (+ y (* (+ 2 lines) cell-width))
		  box ["M" x y
			   "L" right-x y
			   "L" right-x bottom-y
			   "L" x bottom-y
			   "L" x y]
		  title ["M" x (+ y cell-width) "L" right-x (+ y cell-width)]
		  bars (map #(-> ["M" (+ x (* % cell-width)) (+ cell-width y)
						  "L" (+ x (* % cell-width)) bottom-y]) (range 1 7))
		  rows (map #(-> ["M" x (+ y (* (+ 2 %) cell-width))
						  "L" right-x (+ y (* (+ 2 %) cell-width))])
					(range lines))
		  week-days-line (map #(with-meta (svg/text-box
											  (+ x (* cell-width (% 1)))
											  (+ cell-width y)
											  cell-width
											  cell-width
											  (% 0)
											  {:font-weight "bold"})
										  {:key (str "wd" (hash [month-name "wd" %]))})
							  (zipmap math/week-days (range)))]

		^{:key (str "month" (hash month-name))}
		[:g
		 (svg/polyline (merge title box bars rows))
		 week-days-line
		 (svg/text-box x y width cell-width month-name {:font-weight "bold"})
		 (map
			 #(with-meta (svg/text-box
							 (+ x (* cell-width (- %3 1)))
							 (+ y (* cell-width (+ 2 %2)))
							 cell-width
							 cell-width
							 %1
							 {:font-size 3.5})
						 {:key (str "t" (hash [month-name %1]))})
			 days
			 (math/week-number-per-day week-days-this-month)
			 week-days-this-month
			 )]))


(defn- print-month [year y-offset box-width month x]
	(let [number-of-days-this-month (math/days-per-month year month)
		  adjusted-month-number (+ 1 month)
		  week-days-this-month (take number-of-days-this-month (math/days-of-week year adjusted-month-number))]
		^{:key (str "month" (hash month))}
		(month-box
			(math/month-names month)
			x
			(+ y-offset (* (quot month 3) 70))
			box-width
			(range 1 (+ 1 number-of-days-this-month))
			week-days-this-month)))

(defn- close-application-button [close-application]
	(fn []
		@(rf/subscribe [:__figwheel_counter])
		[:g {:transform "translate(185,1)"
			 :on-click  close-application
			 :cursor "pointer"}
		 [:rect {:x       0 :y 0 :width 12 :height 12
				 :opacity 1
				 :fill    "white"}]
		 [:path {:stroke       "grey"
				 :fill         "none"
				 :stroke-width "1px"
				 :d            "M3,3 L9,9 M9,3 L3,9"}
		  [:title "Закрыть"]]]))

(defn calendar [close-application]
	(fn []
		(let [tick @(rf/subscribe [:__figwheel_counter])
			  box-width 58
			  y-offset 15
			  year @(rf/subscribe [:year])
			  edit-year? (= @(rf/subscribe [:mode]) :select-year)]

			(svg/whole-page
				{:on-click (dispatch :remove-year-selector)}
				[header-text]


				(map
					(partial print-month year y-offset box-width)
					(range 12)
					(cycle [10 75 140]))

				(when edit-year?
					[year-selector-dialog])

				[(close-application-button close-application)]
				)
			)))
