package org.etypedactors.akka

import akka.actor.Actor
import org.etypedactors.ActorType
import org.etypedactors.OneWayMethodCall
import org.etypedactors.MethodCall
import org.etypedactors.Resolution
import org.etypedactors.Smashing
import org.etypedactors.ETypedActor

class AkkaEtypedActor(makeImpl: => Any, makeActor: => ActorType) extends Actor {

  lazy private val myself = makeActor

  lazy private val impl = makeImpl

  override def preStart() {
    impl // Lazily invoke the factory
  }

  def receive = {
    case OneWayMethodCall(m, args) => // one-way fire-and-forget
      ETypedActor.setCurrentActor(myself)
      try {
        m.invoke(impl, args)
      } finally {
        ETypedActor.setCurrentActor(null)
      }
    case MethodCall(m, args, resolver) => // two-way with resolver
      ETypedActor.setCurrentActor(myself)
      try {
        val result = m.invoke(impl, args)
        resolver.resolve(result)
      } catch {
        case exception: Exception => resolver.smash(exception)
      } finally {
        ETypedActor.setCurrentActor(null)
      }

     // Internal result response messages
    case Resolution(promise, result) => promise.notifyResolved(result)
    case Smashing(promise, exception) => promise.notifySmashed(exception)
  }

}