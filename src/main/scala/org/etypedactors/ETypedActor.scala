package org.etypedactors

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object ETypedActor {

  def create(actorFactory: ActorFactoryType) = new ETypedActor(actorFactory)

  def currentActor[T]: T = {
    val current = currentActor
    (if (current == null) null else current.proxy).asInstanceOf[T]
  }
  
  def fulfill[T](value: T) = new ResolvedPromise(value)

  private val currentActorHolder = new ThreadLocal[ActorType]()

  private[etypedactors] def setCurrentActor(actor: ActorType) { currentActorHolder.set(actor) }

  @inline implicit private def currentActor: ActorType = currentActorHolder.get()

  private val PromiseClass = classOf[Promise[_]]

  private class ActorInvocationHandler(val actor: ActorType) extends InvocationHandler {

    def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = {
      method.getReturnType match {
        case Void.TYPE => // one-way fire-and-forget
          actor ! OneWayMethodCall(method, args)
          return null
        case PromiseClass =>  // two-way with Resolver
          val (promise, resolver) = Promise[Any]()
          actor ! TwoWayMethodCall(method, args, resolver)
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
      val handler = new ETypedActor.ActorInvocationHandler(actor)
      Proxy.newProxyInstance(interface.getClassLoader(), Array[Class[_]](interface), handler)
    })
    return actor.proxy.asInstanceOf[R]
  }

  def stop(etypedActor: AnyRef) {
    Proxy.getInvocationHandler(etypedActor) match {
      case ic: ETypedActor.ActorInvocationHandler => actorFactory.stopActor(ic.actor)
      case _ => throw new IllegalArgumentException("Not an etyped actor - " + etypedActor)
    }
  }

}