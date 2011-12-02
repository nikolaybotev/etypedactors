package org.etypedactors

import java.lang.reflect.Method

abstract class ETypedMessage

case class OneWayMethodCall[T](method: Method, args: Array[AnyRef]) extends ETypedMessage
case class MethodCall[T](method: Method, args: Array[AnyRef], resolver: Resolver[T]) extends ETypedMessage

case class Resolution[T](promise: Promise[T], result :T) extends ETypedMessage
case class Smashing[T](promise: Promise[T], exception: Exception) extends ETypedMessage
