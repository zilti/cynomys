(ns cynomys.core
  (:use lamina.core
        lamina.viz))

(def mutable-storage (ref {:channels {}
                           :middlewares {}}))

(defn- alter-mutable-storage "Quick access to altering the mutable-storage ref.
path: The root path under which you'll operate, as a vector. Syntax like update-in.
fun: The function taking the value and modifying it."
  [path fun]
  (dosync
   (alter mutable-storage
          (fn [storage]
            (update-in storage path fun)))))

(defn add-middleware "adds a middleware to execute.
middleware: The keyword of the middleware thread to add this to
middleware-fn: The middleware function"
  [middleware middleware-fn]
  (alter-mutable-storage
   [:middlewares]
   (fn [middlewares]
     ;; println middlewares
     (if (contains? middlewares middleware)
       (update-in middlewares [middleware] #(conj % middleware-fn))
       (assoc-in middlewares [middleware] [middleware-fn])))))

(defn remove-middleware "removes a middleware.
middleware: The keyword of the middleware thread to remove this from
middleware-fn: The middleware function or the hash value of it"
  [middleware middleware-fn]
  (alter-mutable-storage
   [:middlewares middleware]
   (fn [middlewares] (remove #(= % middleware-fn) middlewares))))

(defn remove-middleware-key "removes a middleware key and all middleware associated."
  [middleware]
  (alter-mutable-storage
   [:middlewares]
   (fn [middlewares]
     (dissoc middlewares middleware))))

(defn exec-middlewares "Execute all middleware functions of a middleware."
  [middleware arg]
  (reduce
   #(apply %2 [%]) arg (get-in @mutable-storage [:middlewares middleware])))

(defn register-channel "This registers a new channel in the channel registry.
This also adds a link point for middleware, usable as :ch-chname, e.g. :ch-toserver
chname: A keyword which is used as the name of this channel.
channel: The channel itself."
  [chname channel]
  (alter-mutable-storage
   [:channels]
   (fn [channels]
     (if (contains? (keys channels) chname)
       (.Throw (new Exception "Channel already registered."))
       (assoc channels chname
              (->> channel
                   (map* (fn [in]
                           (exec-middlewares in
                                             (keyword (str "ch-" (name chname))))))))
       ))))

(defn deregister-channel "This removes a channel from the channel registry.
It also closes it and removes any bindings so it can be collected by the gc."
  [chname]
  (alter-mutable-storage
   [:channels]
   (fn [channels]
     (do
       (remove-middleware-key (str "ch-" (name chname)))
       (close (chname channels))
       (dissoc channels chname)))))