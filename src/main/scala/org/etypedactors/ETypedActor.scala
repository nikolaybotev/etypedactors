package org.etypedactors

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object ETypedActor extends App {

  def create(actorFactory: ActorFactoryType) = new ETypedActor(actorFactory)

  def self[T]: T = null.asInstanceOf[T] // FIXME

  implicit private def currentEtypedActor: ActorType = null // FIXME

  private val PromiseClass = classOf[Promise[_]]

  private class ActorInvocationHandler(actor: ActorType) extends InvocationHandler {

    def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = {
      method.getReturnType match {
        case Void.TYPE => // one-way fire-and-forget
          actor ! OneWayMethodCall(method, args)
          return null
        case PromiseClass =>  // two-way with Resolver
          val (promise, resolver) = Promise[Any]()
          actor ! MethodCall(method, args, resolver)
          return promise
        case _ => 
          throw new IllegalAccessError("Only void methods or methods returning a Promise are supported. " +
            "Problematic method: " + method)
      }
    }

  }

}

class ETypedActor(actorFactory: ActorFactoryType) {
  
  def createActor[R <: AnyRef, T <: R](interface: Class[R], impl: => T): R = {
    val actor = actorFactory.createActor(impl)
    val handler = new ETypedActor.ActorInvocationHandler(actor)
    val proxy = Proxy.newProxyInstance(interface.getClassLoader(), Array[Class[_]](interface), handler)
    return proxy.asInstanceOf[R]
  }
  
}