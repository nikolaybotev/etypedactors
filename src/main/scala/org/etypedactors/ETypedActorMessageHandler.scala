package org.etypedactors

trait ETypedActorMessageHandler {

  protected val myself: ActorWithProxy

  protected val impl: Any

  @inline private def withMyself(closure: => Unit) {
      ETypedActorSystem.setCurrentActor(myself)
      try {
        closure
      } finally {
        ETypedActorSystem.setCurrentActor(null)
      }
  }

  protected val messageHandler: PartialFunction[Any, Unit] = {
    // One-way fire-and-forget
    case OneWayMethodCall(m, args) => withMyself {
      m.invoke(impl, args:_*)
    }

    // Two-way with resolver
    case TwoWayMethodCall(m, args, resolver) => withMyself {
      try {
        val resultPromise = m.invoke(impl, args:_*).asInstanceOf[Promise[_]]
        resultPromise.when({ result => resolver.resolve(result) }, { case ex => resolver.smash(ex) })
      } catch {
        case exception: Exception => resolver.smash(exception)
      }
    }

     // Internal result response messages
    case Resolution(promise, result) => withMyself { promise.notifyResolved(result) }
    case Smashing(promise, exception) => withMyself { promise.notifySmashed(exception) }
  }

}