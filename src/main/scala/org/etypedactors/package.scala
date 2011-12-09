package org

package object etypedactors {

  trait IdiomaticActor {
    def !(message: Any): Unit
  }

  class ActorWithProxy(makeActor: => IdiomaticActor, makeProxy: => AnyRef) {
    lazy val actorRef = makeActor
    lazy val proxy = makeProxy
  }

}