package org.etypedactors.akka

import akka.actor.Actor
import org.etypedactors.ActorType
import org.etypedactors.ETypedActorMessageHandler

class AkkaEtypedActor(protected val myself: ActorType, makeImpl: => Any) extends Actor with ETypedActorMessageHandler {

  lazy protected val impl = makeImpl

  override def preStart() {
    impl // Lazily invoke the factory
  }

  def receive = messageHandler

}