package org

package object etypedactors {

  type ActorType = {
    def !(message: Any): Unit
    val proxy: AnyRef
  }
  
  type ActorFactoryType = {
    def createActor(impl: => Any, proxy: => AnyRef): ActorType
  }
  
}