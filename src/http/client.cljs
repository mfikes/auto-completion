(ns http.client
  (:refer-clojure :exclude [get])
  (:require-macros [http.macros :refer [defreq]])
  (:require [clojure.walk :refer [prewalk]]))

(defonce default-client (delay (js/FCJHttpClient.create)))

(defn request
  [{:keys [client timeout keepalive as follow-redirects max-redirects]
    :as   opts
    :or   {client           @default-client
           timeout          60000
           follow-redirects true
           max-redirects    10
           keepalive        120000
           as               :auto}}
   callback]
  (let [{:keys [url method]} opts]
    (.requestMethodUrlParametersCallback client
                                         (name method)
                                         url
                                         (clj->js (dissoc opts :url :method))
                                         (fn [resp]
                                           (callback (assoc (js->clj resp :keywordize-keys true)
                                                       :opts opts))))))

(defreq get request)
(defreq delete request)
(defreq head request)
(defreq post request)
(defreq put request)
(defreq patch request)