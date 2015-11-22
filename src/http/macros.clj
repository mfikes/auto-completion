(ns http.macros
  (:require [clojure.string :as str]))

(defmacro ^:private defreq [method request]
  `(defn ~method
     ~(str "Issues an async HTTP " (str/upper-case method) " request. "
           "See `request` for details.")
     ~'{:arglists '([url opts callback] [url callback])}
     ~'[url & [s1 s2]]
     (if (fn? ~'s1)
       (~request {:url ~'url :method ~(keyword method)} ~'s1)
       (~request (merge ~'s1 {:url ~'url :method ~(keyword method)}) ~'s2))))