(ns year-calendar-2.event-handlers
	(:require [re-frame.core :as rf]))

(enable-console-print!)

(defonce app-state (atom {:mode :print-calendar
						  :__figwheel_counter 0
						  :year 2017}))

(defn keep-actual [db _]
	(reset! app-state db))

(defn log-to-console [db _]
	(js/console.log "STATE:" db))

(def middle-ware [(rf/after keep-actual)
				  (if (identical? js/goog.DEBUG true)
					  (rf/after log-to-console))
				  ])


(defn register-handlers []
	(rf/reg-event-db
		:initialize
		(fn [_ _] @app-state))

	(rf/reg-event-db
		:the-year-title-click
		middle-ware
		(fn [db v]
			(update db :mode (fn [x]
								 (if (= :select-year x) :print-calendar :select-year)))))

	(rf/reg-event-db
		:remove-year-selector
		middle-ware
		(fn [db v] (assoc db :mode :print-calendar)))

	(rf/reg-event-db
		:swap-year
		middle-ware
		(fn [db [evt delta]] (update db :year (partial + (if (< 0 delta) -1 1)))))

	(rf/reg-event-db
		:select-year
		middle-ware
		(fn [db [_ year]]
			(assoc db :year year)))

	(rf/reg-event-db
		:new-year
		middle-ware
		(fn [db [evt year]]
			(merge db {:year year
					   :mode :print-calendar})
			))

	(rf/dispatch [:initialize]))
