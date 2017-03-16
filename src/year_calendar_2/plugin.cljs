(ns year-calendar-2.plugin
	(:import goog.cssom))

(defn disable-css []
	(doall (map #(set! (.-disabled %) true) (goog.cssom.getAllCssStyleSheets))))


(defn ^:export run-application [f]
	(js/alert "hello1")
	f)