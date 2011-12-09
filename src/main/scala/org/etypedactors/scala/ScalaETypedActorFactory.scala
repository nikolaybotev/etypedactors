package org.etypedactors.scala

import scala.actors.Actor
import org.etypedactors.IdiomaticActor
import org.etypedactors.ActorFactory
import org.etypedactors.ActorWithProxy
import org.etypedactors.ETypedActorMessageHandler

class IdiomaticScalaActor(makeActorRef: => Actor) extends IdiomaticActor {
  lazy val actorRef = makeActorRef
  def !(message: Any) = actorRef.! (message)
}

private object ScalaETypedActor {
  case class Exit
  val exitHandler: PartialFunction[Any, Unit] = { case Exit => Actor.exit }
}

private class ScalaETypedActor(
    protected val myself: ActorWithProxy,
    makeImpl: => Any) extends Actor with ETypedActorMessageHandler {

  lazy protected val impl = makeImpl

  def act() {
    impl // Lazily invoke the factory
    Actor.eventloop(messageHandler orElse ScalaETypedActor.exitHandler)
  }

}

class ScalaETypedActorFactory extends ActorFactory {

  def createActor(makeImpl: => Any, makeActor: => ActorWithProxy): IdiomaticActor = {
    new IdiomaticScalaActor(new ScalaETypedActor(makeActor, makeImpl))
  }

  def startActor(actor: IdiomaticActor) = actor match {
    case a: IdiomaticScalaActor => a.actorRef.start
    case _ => throw new IllegalArgumentException("Not an Scala actor: " + actor)
  }

  def stopActor(actor: IdiomaticActor) = actor match {
    case a: IdiomaticScalaActor => a.actorRef ! ScalaETypedActor.Exit
    case _ => throw new IllegalArgumentException("Not an Scala actor: " + actor)
  }

}