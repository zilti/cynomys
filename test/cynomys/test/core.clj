(ns cynomys.test.core
  (:use cynomys.core
        midje.sweet))

(def mutable-storage-skeleton @mutable-storage)
(defchecker is-class [cls]
  (checker [actual]
           (= (class cls) (class actual))))
(against-background
 [(before :contents
          (dosync
           (alter mutable-storage
                  (fn [stor] mutable-storage-skeleton))
           (alter mutable-storage
                  (fn [stor] (assoc-in stor [:middlewares :empty] {}))))
          )]
 (fact "Trying to execute middleware from a non-existent key returns the input."
       (exec-middlewares :nonexist :val) => :val)
 (fact "Trying to remove a non-existant middleware key does not throw."
       (remove-middleware-key :nonexist) => #(= (class {}) (class (:middlewares %))))
 (fact "Removing a non-existent middleware does not throw."
       (remove-middleware :empty #(%)) => (is-class {}))

 (defn twoinc [x] (+ x 2))
 
 (fact "Inserting two middlewares."
       (add-middleware :full twoinc) => (is-class {})
       (add-middleware :full inc) => (is-class {}))
 (fact "Applying both inserted middlewares."
       (exec-middlewares :full 2) => 5)
 (fact "Removing one middleware."
       (remove-middleware :full inc) => (is-class {}))
 (fact "Applying the remaining middleware."
       (exec-middlewares :full 2) => 4)
 (fact "Removing remaining middleware."
       (remove-middleware :full twoinc) => (is-class {}))
 (fact "Apply empty middleware chain."
       (exec-middlewares :full 2) => 2)
 )


(use 'lamina.core)
(def channel-one (lamina.core/channel))
(against-background
 [(before :contents
          (dosync
           (alter mutable-storage
                  (fn [stor] mutable-storage-skeleton))))]
 (fact "Register a new channel."
       (register-channel :one channel-one) => (is-class {}))
 (fact "Flush a value through the channel."
       (enqueue channel-one 5) => :lamina/enqueued)
 (fact "Add middleware to it."
       (add-middleware "ch-one" twoinc) => (is-class {}))
 (fact "Deregister the channel."
       (deregister-channel :one) => (is-class {}))
 (fact "Executing middlewares of ch-one."
       (exec-middlewares :twoinc 2) => 2))