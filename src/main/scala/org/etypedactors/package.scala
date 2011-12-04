package org

package object etypedactors {

  type ActorType = {
    def !(message: Any): Unit
    val proxy: AnyRef
  }

  trait ActorFactory {
    def createActor(impl: => Any, proxy: => AnyRef): ActorType
    def stopActor(actor: ActorType)
  }

}