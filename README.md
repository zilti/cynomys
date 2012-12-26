Cynomys
=======

Cynomys is a small library for handling middleware in your project (e.g. as a simple form of a "plugin interface"). It's partially built on top of [Lamina](http://github.com/ztellman/lamina).

## What can/can't I use this for? ##

If you want to provide yourself or others a way to "hook into" your data flow, this library might fit your need, since that's what I've built it for.

You should not be using this library if you don't like documenting where you placed a middleware hook, and therefore this might be a bad library for bigger projects.

## How is it used? ##

### Pure middleware ###

Any function taking one argument can be registered as a middleware for some defined workflow:

```(use 'cynomys.core)
(add-middleware :preprocessing #(assoc % :yet-another 5))```

If you want to provide a middleware entry point in your program, just pass your dataset to the middleware manager (only one argument is allowed right now):

```(use 'cynomys.core)
(exec-middlewares :preprocessing {:blue "red"})
; => {:blue "red", :yet-another 5}```

### Lamina Channels ###

This library has support for Lamina channels because I think it's an awesome implementation of a simple-yet-awesome concept.
The implementation provided in Cynomys might not be that awesome as well, but it's simple enough.
Again you can register and deregister stuff:

```(use 'cynomys.core)
(register-channel :mainstream (channel))```

This registers a new channel. Note, though, that you'll probably want to reference that channel from somewhere, or it will only consume memory and middleware.
Registering a channel chains a middleware-executor at the end of it, so chose wisely where you want to chain your stuff in. The middleware executor will execute middleware saved under the :ch-channelname keyword:

```(use 'cynomys.core)
(add-middleware :ch-mainstream (fn [x] whatever-you-want))```

And if you deregister the channel, all middleware associated to it will be wiped, your channel closed and everything explodes into a cloud of bits and bytes:

```(use 'cynomys.core)
(deregister-channel :ch-mainstream)```

## License ##

Copyright © 2012 Daniel Ziltener

Distributed under the Eclipse Public License, the same as Clojure.
