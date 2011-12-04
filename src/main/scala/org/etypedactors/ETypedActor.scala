package org.etypedactors

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object ETypedActor {

  def create(actorFactory: ActorFactory) = new ETypedActor(actorFactory)

  def currentActor[T]: T = {
    val current = currentActorInternal
    (if (current == null) null else current.proxy).asInstanceOf[T]
  }

  def fulfill[T](value: T) = new ResolvedPromise(value)

  private val currentActorHolder = new ThreadLocal[ActorType]()

  private[etypedactors] def setCurrentActor(actor: ActorType) { currentActorHolder.set(actor) }

  @inline implicit private[etypedactors] def currentActorInternal: ActorType = currentActorHolder.get()

}

class ETypedActor(actorFactory: ActorFactory) {

  def createActor[R <: AnyRef, T <: R](interface: Class[R], impl: => T): R = {
    lazy val actor: ActorType = actorFactory.createActor(impl, {
      val handler = new ETypedActorInvocationHandler(actor)
      Proxy.newProxyInstance(interface.getClassLoader(), Array[Class[_]](interface), handler)
    })
    return actor.proxy.asInstanceOf[R]
  }

  def stop(etypedActor: AnyRef) {
    Proxy.getInvocationHandler(etypedActor) match {
      case ic: ETypedActorInvocationHandler => actorFactory.stopActor(ic.actor)
      case _ => throw new IllegalArgumentException("Not an etyped actor - " + etypedActor)
    }
  }

}