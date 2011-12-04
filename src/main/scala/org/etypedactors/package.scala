package org

package object etypedactors {

  type ActorType = {
    def !(message: Any): Unit
  }

  class ActorWithProxy[T <: ActorType](makeProxy: => AnyRef, makeActor: => T) {
    lazy val proxy = makeProxy
    lazy val actorRef = makeActor
  }

  type ActorWithProxyType = ActorWithProxy[_ <: ActorType]

  trait ActorFactory[T <: ActorType] {
    def createActor(impl: => Any, proxy: => AnyRef): ActorWithProxy[T]
    def stopActor(actor: ActorType)
  }

}