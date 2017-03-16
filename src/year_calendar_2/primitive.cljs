(ns year-calendar-2.primitive)

(defn insert-at [identities at pretend]
	(let [[before after] (split-at at identities)]
		(vec (concat before pretend after))))
