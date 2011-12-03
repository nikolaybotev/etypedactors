package org.etypedactors.akka

import akka.actor.Actor
import org.etypedactors.ActorType
import org.etypedactors.OneWayMethodCall
import org.etypedactors.MethodCall
import org.etypedactors.Resolution
import org.etypedactors.Smashing
import org.etypedactors.ETypedActor
import org.etypedactors.Promise

class AkkaEtypedActor(makeImpl: => Any, makeActor: => ActorType) extends Actor {

  lazy private val myself = makeActor

  lazy private val impl = makeImpl

  override def preStart() {
    impl // Lazily invoke the factory
  }
  
  @inline private def withSelf(closure: => Unit) {
      ETypedActor.setCurrentActor(myself)
      try {
        closure
      } finally {
        ETypedActor.setCurrentActor(null)
      }
  }

  def receive = {
    // One-way fire-and-forget
    case OneWayMethodCall(m, args) => withSelf {
      m.invoke(impl, args:_*)
    }

    // Two-way with resolver
    case MethodCall(m, args, resolver) => withSelf {
      try {
        val resultPromise = m.invoke(impl, args:_*).asInstanceOf[Promise[_]]
        resultPromise.when({ result => resolver.resolve(result) }, { case ex => resolver.smash(ex) })
      } catch {
        case exception: Exception => resolver.smash(exception)
      }
    }

     // Internal result response messages
    case Resolution(promise, result) => withSelf { promise.notifyResolved(result) }
    case Smashing(promise, exception) => withSelf { promise.notifySmashed(exception) }
  }

}