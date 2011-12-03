package org.etypedactors.akka

import akka.actor.Actor
import org.etypedactors.ActorType

class AkkaEtypedActorFactory {

  protected def createAkkaActor(f: => Actor) = Actor.actorOf(f)

  def createActor(impl: => Any, makeProxy: => AnyRef): ActorType = {
    lazy val actor: ActorType = new {
      lazy val proxy = makeProxy
      lazy val actorRef = createAkkaActor(new AkkaEtypedActor(impl, actor))
      def !(message: Any) = actorRef ! message
    }
    return actor
  }

}