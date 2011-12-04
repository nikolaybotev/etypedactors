package org.etypedactors

import java.lang.reflect.Method

abstract class ETypedMessage

case class OneWayMethodCall[T](method: Method, args: Array[AnyRef]) extends ETypedMessage
case class TwoWayMethodCall[T](method: Method, args: Array[AnyRef], resolver: Resolver[T]) extends ETypedMessage

case class Resolution[T](promise: InActorPromise[T], result :T) extends ETypedMessage
case class Smashing[T](promise: InActorPromise[T], exception: Exception) extends ETypedMessage
