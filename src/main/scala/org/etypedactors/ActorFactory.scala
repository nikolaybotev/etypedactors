package org.etypedactors

trait ActorFactory {
  def createActor(makeImpl: => Any, makeActor: => ActorWithProxy): IdiomaticActor
  def startActor(actor: IdiomaticActor)
  def stopActor(actor: IdiomaticActor)
}
