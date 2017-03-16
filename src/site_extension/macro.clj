(ns site-extension.macro)

(defmacro toggle-css-style [element attr from to]
	`(do
		 (let [s# (goog.object.get ~element "style")
			   v# (goog.object.get s# ~attr)
			   inv# (if (or (= v# ~from) (= "" v#)) ~to ~from)]
			 (goog.object.set s# ~attr inv#)
			 )))


(defmacro toggle-attr [element from to]
	`(do
		 (set! ~element
			   (if (or (= ~element ~from) (= "" ~element))
				   ~to
				   ~from))))