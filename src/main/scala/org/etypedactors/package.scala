package org

package object etypedactors {

  type ActorType = {
    def !(message: Any): Unit
  }

  type ActorFactoryType = {
    def createActor(impl: => Any): ActorType
  }
  
}