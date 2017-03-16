(ns site-extension.application
	(:require [reagent.core :as reagent])
	(:require-macros [site-extension.macro :refer [toggle-css-style]]
					 [reagent.interop :refer [$ $!]]))

(def style-sheets (atom []))

(defn- turn-css [on-or-off]
	(let [disabled (not= :on on-or-off)
		  availables-css (goog.cssom.getAllCssStyleSheets)]

		(when-not (empty? availables-css)
			(reset! style-sheets availables-css))

		(doall (map #(set! (.-disabled %) disabled) @style-sheets))))


(defn register-application [application html-id]
	(assert (fn? application))

	($ js/window addEventListener "DOMContentLoaded"
	   (fn []
		   (letfn [(appended-container [target]
					   (let [id "reagent-application"
							 container (.getElementById js/document id)]

						   (if container
							   container
							   (.appendChild target (doto (.createElement js/document "div")
														(-> (.setAttribute "id" id))))
							   )))]

			   (let [body (-> ($ js/document :body))
					 body-children (-> body (goog.dom.getChildren))
					 content-div (-> body-children
									 (goog.array/filter
										 (fn [v k]
											 (not (or
													  (contains? #{"SCRIPT"} ($ v :tagName))
													  (not= "" ($ v :id))))))
									 js->clj)
					 application-node (appended-container body)]

				   (letfn [(toggle-site-content []
							   (doseq [node content-div]
								   (js/console.log node)
								   (toggle-css-style node "display" "block" "none")))

						   (stop-application []
							   (turn-css :on)
							   (reagent/unmount-component-at-node application-node)
							   (toggle-site-content))

						   (start-application []
							   (toggle-site-content)
							   (turn-css :off)
							   (reagent/render-component [(application (fn [] (js/setTimeout stop-application 10)))]
														 application-node))]

					   (goog.object.set js/window html-id start-application)
					   ))))))
