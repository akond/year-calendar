(ns year-calendar-2.math
	(:require [clojure.string :as s]))


(defn- zeller [year month day]
	(let [m (+ (mod (+ month 9) 12) 3)
		  y (- year (quot (- m month) 12))
		  J (quot y 100)
		  K (mod y 100)
		  q day]
		(mod (+ q
				(quot (* 26 (inc m)) 10)
				K
				(quot K 4)
				(quot J 4)
				(* 5 J))
			 7)))

(defn- shift0 [i]
	(+ (mod (+ i 6) 7) 1))


(defn day-of-week [year month day]
	(shift0 (zeller year month day)))


(defn leap-year? [year]
	(cond (zero? (mod year 400)) true
		  (zero? (mod year 100)) false
		  :else (zero? (mod year 4))))

(defn week-number-per-day [week-days]
	(loop [week-days week-days
		   week-number 0
		   result []]
		(cond
			(empty? week-days) result
			(= (first week-days) 7) (recur (rest week-days) (+ 1 week-number) (conj result week-number))
			:else (recur (rest week-days) week-number (conj result week-number)))
		))

(defn days-per-month [year month]
	(let [day-counts [31 28 31 30
					  31 30 31 31
					  30 31 30 31]]
		(+ (day-counts month) (if (and (leap-year? year) (= month 1)) 1 0))))


(def my-tconfig
	{:en {:weekdays ["Su" "Mo" "Tu" "We" "Th" "Fr" "Sa"]
		  :months   ["January" "February" "March"
					 "April" "May" "June"
					 "July" "August" "September"
					 "October" "November" "December"]}
	 :ru {:weekdays ["Вс" "Пн" "Вт" "Ср" "Чт" "Пт" "Сб"]
		  :months   ["Январь" "Февраль" "Март"
					 "Апрель" "Май" "Июнь"
					 "Июль" "Август" "Сентябрь"
					 "Октябрь" "Ноябрь" "Декабрь"]}
	 })

(def preferred-languages (distinct (map #(first (s/split % #"-")) js/window.navigator.languages)))
(def available-lanugages (set (map name (keys my-tconfig))))
(def active-language (keyword (or (some available-lanugages preferred-languages) "ru")))
(def t (my-tconfig active-language))

(def week-days (t :weekdays))
(def month-names (t :months))

(defn days-of-week [year month]
	(cycle (flatten (apply cons (reverse (split-at (- (day-of-week year month 1) 1) (range 1 8)))))))
