(ns ironproject.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]))

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]
    ["/items"
     ["" :items]
     ["/:item-id" :item]]
    ["/about" :about]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components
(defn kiss-cpt []
    [:p '😽😽])


(defn greet-cpt []
  [:div
   [:h4 "I am Ironman."]
   [:p
    "I have a " [:strong "bold"]
    [:span {:style {:color "red"}} " heart!"]]])

(defn home-page []
  (fn []
    [:span.main
     [:h1 "Welcome to Ting's"]
     [:h1 "2022 IT Ironman project"]
     [kiss-cpt]
     [greet-cpt]
     {:hello "👋 Good morning my love"  :cat (map #(repeat % '😻) (range 1 5))}

     [:ul
      [:li [:a {:href (path-for :items)} "My IronProject"]]
      [:li [:a {:href "/broken/link"} "Articles link "]]]]))

(defn items-page []
  (fn []
    [:span.main
     [:h1 "The items of ironproject"]
     [:ul (map (fn [item-id]
                 [:li {:name (str "item-" item-id) :key (str "item-" item-id)}
                  [:a {:href (path-for :item {:item-id item-id})} "Item: " item-id]])
               (range 1 60))]]))


(defn item-page []
  (fn []
    (let [routing-data (session/get :route)
          item (get-in routing-data [:route-params :item-id])]
      [:span.main
       [:h1 (str "Item " item " of ironproject")]
       [:p [:a {:href (path-for :items)} "Back to the list of items"]]])))


(defn about-page []
  (fn [] [:span.main 
          [:h1 "About ironproject"]
          [:h2 "貓貓工程師的鐵人檔案"]

          [kiss-cpt] 
          [:p [:a {:href "https://ithelp.ithome.com.tw/users/20111177/ironman/1613"} "2018 iThome 鐵人賽: 30天修煉Ruby面試精選30題"]]
          [:p [:a {:href "https://ithelp.ithome.com.tw/users/20111177/ironman/2960"} "2020 iThome 鐵人賽: 「VR 」前端後端交響曲 - 30天開發 Vue.js feat. Ruby on Rails即時互動網站"]]
          [:p [:a {:href "https://ithelp.ithome.com.tw/users/20111177/ironman/5153"} "2022 iThome 鐵人賽: 後端Developer實戰ClojureScript: Reagent與前端框架 Reframe"]] 
          ]))



;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :about #'about-page
    :items #'items-page))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p [:a {:href (path-for :index)} "Home"] " | "
         [:a {:href (path-for :about)} "About ironproject"]]]
       [page]
       [:footer
        [:p "ironproject was generated by the "
         [:a {:href "https://github.com/reagent-project/reagent-template"} "Reagent Template"] "."]]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)))

    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))

(defn ^:dev/after-load reload! []
  (mount-root))
