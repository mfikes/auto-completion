;; Need to set js/React first so that Om can load
(set! js/React (js/require "react-native/Libraries/react-native/react-native.js"))

(ns auto-completion.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer [<! >! put! chan to-chan]]
            [clojure.string :as string]
            [om.next :as om :refer-macros [defui]]
            [http.client :as http]))

;; Reset js/React back as the form above loads in an different React
(set! js/React (js/require "react-native/Libraries/react-native/react-native.js"))

;; Setup some methods to help create React Native elements
(defn view [opts & children]
  (apply js/React.createElement js/React.View (clj->js opts) children))

(defn text [opts & children]
  (apply js/React.createElement js/React.Text (clj->js opts) children))

(defn text-input [opts & children]
  (apply js/React.createElement js/React.TextInput (clj->js opts) children))

(def base-url
  "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")

(defn jsonp
  ([uri] (jsonp (chan) uri))
  ([c uri]
   (http/get uri nil (fn [res] (put! c (:response-object res))))
   c))

(defmulti read om/dispatch)

(defmethod read :search/results
  [{:keys [state ast] :as env} k {:keys [query]}]
  #_(prn 'read env)
  (merge
    {:value (get @state k [])}
    (when-not (and (string/blank? query)
                (<= 2 (count query)))
      {:search ast})))

(defn result-list [results]
  (view {:style {:flexDirection "column" :margin 40}}
    (map #(text nil %) results)))

(defn search-field [ac query]
  (text-input
    {:style {:height 40}
     :value query
     :onChangeText
            (fn [new-text]
              (om/set-query! ac
                {:params {:query new-text}}))}))

(defui AutoCompleter
  static om/IQueryParams
  (params [_]
    {:query ""})
  static om/IQuery
  (query [_]
    '[(:search/results {:query ?query})])
  Object
  (render [this]
    (let [{:keys [search/results]} (om/props this)]
      (view {:style {:flexDirection "column" :margin 40}}
        (text nil "Autocompleter")
        (cond->
          [(search-field this (:search-query (om/get-params this)))]
          (not (empty? results)) (conj (result-list results)))))))

(def send-chan (chan))

(defn search-loop [c]
  (go
    (loop [[query cb] (<! c)]
      (let [[_ results] (<! (jsonp (str base-url query)))]
        (cb {:search/results results}))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search]} cb]
    (when search
      (let [{[search] :children} (om/query->ast search)
            query (get-in search [:params :query])]
        (put! c [query cb])))))

(def reconciler
  (om/reconciler
    {:state        {:search/results []}
     :parser       (om/parser {:read read})
     :send         (send-to-chan send-chan)
     :remotes      [:remote :search]
     :root-render  #(.render js/React %1 %2)
     :root-unmount #(.unmountComponentAtNode js/React %)}))

(search-loop send-chan)

(om/add-root! reconciler AutoCompleter 1)

(defn ^:export init []
  ((fn render []
     (.requestAnimationFrame js/window render))))
