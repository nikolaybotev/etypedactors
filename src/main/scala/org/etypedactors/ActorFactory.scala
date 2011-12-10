package org.etypedactors

trait ActorFactory {
  def createActor(makeImpl: => Any, makeActor: => ActorWithProxy): IdiomaticActor
  def startActor(actor: IdiomaticActor)
  def stopActor(actor: IdiomaticActor)
}

trait IdiomaticActor {
  def !(message: Any): Unit
}

class ActorWithProxy(makeActor: => IdiomaticActor, makeProxy: => AnyRef) {
  lazy val actorRef = makeActor
  lazy val proxy = makeProxy
}
