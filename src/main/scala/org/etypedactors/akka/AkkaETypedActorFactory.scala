package org.etypedactors.akka

import akka.actor.Actor
import akka.actor.ActorRef
import org.etypedactors.IdiomaticActor
import org.etypedactors.ActorFactory
import org.etypedactors.ActorWithProxy
import org.etypedactors.ETypedActorMessageHandler

class AkkaActorType(makeActorRef: => ActorRef) extends IdiomaticActor {
  lazy val actorRef = makeActorRef
  def !(message: Any) = actorRef.! (message) (null) // do not care about Akka sender
}

private class AkkaETypedActor(
    protected val myself: ActorWithProxy[AkkaActorType],
    makeImpl: => Any) extends Actor with ETypedActorMessageHandler {

  lazy protected val impl = makeImpl

  override def preStart() {
    impl // Lazily invoke the factory
  }

  def receive = messageHandler

}

class AkkaETypedActorFactory extends ActorFactory[AkkaActorType] {

  protected def createAkkaActor(f: => Actor) = Actor.actorOf(f)

  def createActor(makeImpl: => Any, makeActor: => ActorWithProxy[AkkaActorType]): AkkaActorType = {
    new AkkaActorType(createAkkaActor(new AkkaETypedActor(makeActor, makeImpl)))
  }

  def startActor(actor: AkkaActorType) {
    actor.actorRef.start()
  }

  def stopActor(actor: IdiomaticActor) = actor match {
    case a:AkkaActorType => a.actorRef.stop
    case _ => throw new IllegalArgumentException("Not an Akka actor: " + actor)
  }

}