package org.etypedactors.akka

import akka.actor.Actor
import akka.actor.ActorRef
import org.etypedactors.IdiomaticActor
import org.etypedactors.ActorFactory
import org.etypedactors.ActorWithProxy
import org.etypedactors.ETypedActorMessageHandler
import akka.actor.ActorSystem
import akka.actor.Props

class IdiomaticAkkaActor(makeActorRef: => ActorRef) extends IdiomaticActor {
  lazy val actorRef = makeActorRef
  def !(message: Any) = actorRef.! (message) (null) // do not care about Akka sender
}

private class AkkaETypedActor(
    protected val myself: ActorWithProxy,
    makeImpl: => Any) extends Actor with ETypedActorMessageHandler {

  lazy protected val impl = makeImpl

  override def preStart() {
    withMyself { impl } // Lazily invoke the factory
  }

  def receive = messageHandler

}

class AkkaETypedActorFactory(akkaSystem: ActorSystem) extends ActorFactory {

  protected def createAkkaActor(f: => Actor): ActorRef = akkaSystem.actorOf(Props(f))

  def createActor(makeImpl: => Any, makeActor: => ActorWithProxy): IdiomaticActor = {
    new IdiomaticAkkaActor(createAkkaActor(new AkkaETypedActor(makeActor, makeImpl)))
  }

  def startActor(actor: IdiomaticActor) = actor match {
    case a: IdiomaticAkkaActor => // no-op, akka actors auto-start
    case _ => throw new IllegalArgumentException("Not an Akka actor: " + actor)
  }

  def stopActor(actor: IdiomaticActor) = actor match {
    case a:IdiomaticAkkaActor => akkaSystem.stop(a.actorRef)
    case _ => throw new IllegalArgumentException("Not an Akka actor: " + actor)
  }

}