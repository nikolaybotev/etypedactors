package org.etypedactors.akka

import akka.actor.Actor
import akka.actor.ActorRef
import org.etypedactors.ActorType
import org.etypedactors.ActorFactory
import org.etypedactors.ActorWithProxy

class AkkaActorType(makeActorRef: => ActorRef) {
  lazy val actorRef = makeActorRef
  def !(message: Any) = actorRef.! (message) (null) // do not care about Akka sender
}

class AkkaEtypedActorFactory extends ActorFactory[AkkaActorType] {

  protected def createAkkaActor(f: => Actor) = Actor.actorOf(f)

  def createActor(makeImpl: => Any, makeActor: => ActorWithProxy[AkkaActorType]): AkkaActorType = {
    new AkkaActorType(createAkkaActor(new AkkaEtypedActor(makeActor, makeImpl)))
  }

  def startActor(actor: AkkaActorType) {
    actor.actorRef.start()
  }

  def stopActor(actor: ActorType) = actor match {
    case a:AkkaActorType => a.actorRef.stop
    case _ => throw new IllegalArgumentException("Not an Akka actor - " + actor)
  }

}