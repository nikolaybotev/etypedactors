package org.etypedactors

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object ETypedActor extends App {

  def create(actorFactory: ActorFactoryType) = new ETypedActor(actorFactory)

  def self[T]: T = {
    val current = currentActor
    (if (current == null) null else current.proxy).asInstanceOf[T]
  }

  private val currentActorHolder = new ThreadLocal[ActorType]()
  
  private[etypedactors] def setCurrentActor(actor: ActorType) { currentActorHolder.set(actor) }

  @inline implicit private def currentActor: ActorType = currentActorHolder.get()

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
    lazy val actor: ActorType = actorFactory.createActor(impl, {
      val handler: ETypedActor.ActorInvocationHandler = new ETypedActor.ActorInvocationHandler(actor)
      Proxy.newProxyInstance(interface.getClassLoader(), Array[Class[_]](interface), handler)
    })
    return actor.proxy.asInstanceOf[R]
  }

}