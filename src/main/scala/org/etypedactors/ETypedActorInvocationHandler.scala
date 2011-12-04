package org.etypedactors

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

object ETypedActorInvocationHandler {
  private val PromiseClass = classOf[Promise[_]]
}

class ETypedActorInvocationHandler(val actor: ActorType) extends InvocationHandler {

  def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = {
    method.getReturnType match {
      case Void.TYPE => // one-way fire-and-forget
        actor ! OneWayMethodCall(method, args)
        return null
      case ETypedActorInvocationHandler.PromiseClass =>  // two-way with Resolver
        val (promise, resolver) = Promise[Any]()(ETypedActor.currentActorInternal)
        actor ! TwoWayMethodCall(method, args, resolver)
        return promise
      case _ =>
        throw new IllegalAccessError("Only void methods or methods returning a Promise are supported. " +
          "Problematic method: " + method)
    }
  }

}
