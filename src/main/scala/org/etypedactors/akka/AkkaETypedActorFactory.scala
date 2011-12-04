package org.etypedactors.akka

import akka.actor.Actor
import akka.actor.ActorRef
import org.etypedactors.IdiomaticActor
import org.etypedactors.ActorFactory
import org.etypedactors.ActorWithProxy
import org.etypedactors.ETypedActorMessageHandler

class IdiomaticAkkaActor(makeActorRef: => ActorRef) extends IdiomaticActor {
  lazy val actorRef = makeActorRef
  def !(message: Any) = actorRef.! (message) (null) // do not care about Akka sender
}

private class AkkaETypedActor(
    protected val myself: ActorWithProxy[IdiomaticAkkaActor],
    makeImpl: => Any) extends Actor with ETypedActorMessageHandler {

  lazy protected val impl = makeImpl

  override def preStart() {
    impl // Lazily invoke the factory
  }

  def receive = messageHandler

}

class AkkaETypedActorFactory extends ActorFactory[IdiomaticAkkaActor] {

  protected def createAkkaActor(f: => Actor) = Actor.actorOf(f)

  def createActor(makeImpl: => Any, makeActor: => ActorWithProxy[IdiomaticAkkaActor]): IdiomaticAkkaActor = {
    new IdiomaticAkkaActor(createAkkaActor(new AkkaETypedActor(makeActor, makeImpl)))
  }

  def startActor(actor: IdiomaticAkkaActor) {
    actor.actorRef.start()
  }

  def stopActor(actor: IdiomaticActor) = actor match {
    case a:IdiomaticAkkaActor => a.actorRef.stop
    case _ => throw new IllegalArgumentException("Not an Akka actor: " + actor)
  }

}