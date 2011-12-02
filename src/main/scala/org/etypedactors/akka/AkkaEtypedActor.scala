package org.etypedactors.akka

import akka.actor.Actor
import org.etypedactors.OneWayMethodCall
import org.etypedactors.MethodCall
import org.etypedactors.Resolution
import org.etypedactors.Smashing

class AkkaEtypedActor(implFunctor: => Any) extends Actor {

  lazy private val impl = implFunctor

  override def preStart() {
    impl // Lazily invoke the factory
  }

  def receive = {
    case OneWayMethodCall(m, args) => // one-way fire-and-forget
      m.invoke(impl, args)
    case MethodCall(m, args, resolver) => // two-way with resolver
      try {
        val result = m.invoke(impl, args)
        resolver.resolve(result)
      } catch {
        case exception: Exception => resolver.smash(exception)
      }

     // Internal result response messages
    case Resolution(promise, result) => promise.notifyResolved(result)
    case Smashing(promise, exception) => promise.notifySmashed(exception)
  }

}