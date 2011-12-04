package org.etypedactors

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object ETypedActor {

  def create(actorFactory: ActorFactory[_ <: ActorType]) = new ETypedActor(actorFactory)

  def currentActor[T]: T = {
    val current = currentActorWithProxy
    (if (current == null) null else current.proxy).asInstanceOf[T]
  }

  def fulfill[T](value: T) = new ResolvedPromise(value)

  private val currentActorHolder = new ThreadLocal[ActorWithProxyType]()

  private[etypedactors] def setCurrentActor(actor: ActorWithProxyType) { currentActorHolder.set(actor) }

  @inline implicit private[etypedactors] def currentActorWithProxy: ActorWithProxyType = currentActorHolder.get()

}

class ETypedActor[X <: ActorType](actorFactory: ActorFactory[X]) {

  def createActor[R <: AnyRef, T <: R](interface: Class[R], impl: => T): R = {
    lazy val actor: ActorWithProxy[X] = new ActorWithProxy(actorFactory.createActor(impl, actor), {
      val handler = new ETypedActorInvocationHandler(actor.actorRef)
      Proxy.newProxyInstance(interface.getClassLoader(), Array[Class[_]](interface), handler)
    })
    actorFactory.startActor(actor.actorRef)
    return actor.proxy.asInstanceOf[R]
  }

  def stop(etypedActor: AnyRef) {
    Proxy.getInvocationHandler(etypedActor) match {
      case ic: ETypedActorInvocationHandler => actorFactory.stopActor(ic.actor)
      case _ => throw new IllegalArgumentException("Not an etyped actor - " + etypedActor)
    }
  }

}