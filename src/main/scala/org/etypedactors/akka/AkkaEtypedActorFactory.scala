package org.etypedactors.akka

import akka.actor.Actor
import akka.actor.ActorRef
import org.etypedactors.ActorType

private class AkkaActorType(makeProxy: => AnyRef, makeActorRef: => ActorRef) {
  lazy val proxy = makeProxy
  lazy val actorRef = makeActorRef
  def !(message: Any) = actorRef ! message
}

class AkkaEtypedActorFactory {

  protected def createAkkaActor(f: => Actor) = Actor.actorOf(f)

  def createActor(makeImpl: => Any, makeProxy: => AnyRef): ActorType = {
    lazy val actor: AkkaActorType = new AkkaActorType(makeProxy, createAkkaActor(new AkkaEtypedActor(makeImpl, actor)))
    actor.actorRef.start()
    return actor
  }

}