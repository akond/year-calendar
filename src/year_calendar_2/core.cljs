(ns year-calendar-2.core
	(:require [reagent.core :as reagent :refer [atom]]
			  [year-calendar-2.view :as view]
			  [site-extension.application :as application]
			  [year-calendar-2.plugin :as plugin]
			  [year-calendar-2.event-handlers :as handlers]))

(enable-console-print!)

(handlers/register-handlers)

(application/register-application view/calendar "application_calendar")

;(defn application-calendar []
;	(calendar)
;	false)
;
;(set! js/application_calendar application-calendar)

(defn on-js-reload []
	;(js/console.log  ($ :body))
	(swap! handlers/app-state update-in [:__figwheel_counter] inc))


