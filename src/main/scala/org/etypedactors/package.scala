package org

package object etypedactors {

  type ActorType = {
    def !(message: Any): Unit
  }

  class ActorWithProxy[T <: ActorType](makeActor: => T, makeProxy: => AnyRef) {
    lazy val actorRef = makeActor
    lazy val proxy = makeProxy
  }

  type ActorWithProxyType = ActorWithProxy[_ <: ActorType]

  trait ActorFactory[T <: ActorType] {
    def createActor(makeImpl: => Any, makeActor: => ActorWithProxy[T]): T
    def startActor(actor: T)
    def stopActor(actor: ActorType)
  }

}