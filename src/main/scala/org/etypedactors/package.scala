package org

package object etypedactors {

  trait IdiomaticActor {
    def !(message: Any): Unit
  }

  class ActorWithProxy[T <: IdiomaticActor](makeActor: => T, makeProxy: => AnyRef) {
    lazy val actorRef = makeActor
    lazy val proxy = makeProxy
  }

  type ActorWithProxyType = ActorWithProxy[_ <: IdiomaticActor]

  trait ActorFactory[T <: IdiomaticActor] {
    def createActor(makeImpl: => Any, makeActor: => ActorWithProxy[T]): T
    def startActor(actor: T)
    def stopActor(actor: IdiomaticActor)
  }

}