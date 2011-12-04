package org.etypedactors

object Promise {
  def apply[T](sender: ActorType): (Promise[T], Resolver[T]) = {
    if (sender == null) throw new IllegalAccessError("No etyped actor in scope.")

    val promise = new UnresolvedPromise[T]()
    val resolver = new Resolver[T](promise, sender)
    return (promise, resolver)
  }
}

trait PromiseListener[-T] {
  def onResult(result: T): Unit
  def onException(exception: Exception): Unit
}

trait Promise[T] extends Serializable {

  def isResolved: Boolean

  def isSmashed: Boolean

  def getValue: T

  def getException: Exception

  def when(listener: PromiseListener[T])

  def when(resultHandler: T => Unit, exceptionHandler: PartialFunction[Exception, Unit] = {case ex=>}) {
    when(new PromiseListener[T] {
      def onResult(result: T) = resultHandler(result)
      def onException(exception: Exception) = exceptionHandler(exception)
    })
  }

}

private[etypedactors] final class ResolvedPromise[T](result: T) extends Promise[T] {
  def isResolved = true
  def isSmashed = false
  def getValue = result
  def getException = null
  def when(listener: PromiseListener[T]) = listener.onResult(result)
  override def when(resultHandler: T => Unit, exceptionHandler: PartialFunction[Exception, Unit] = {case ex=>}) = resultHandler(result)
}

private[etypedactors] final class UnresolvedPromise[T] extends Promise[T] {

  private var listeners: List[PromiseListener[T]] = collection.immutable.List[PromiseListener[T]]()

  private var resolution: Either[T, Exception] = _

  def isResolved = resolution != null

  def isSmashed = resolution match { case Right(_) => true case _ => false }

  def getValue = resolution match { case Left(result) => result }

  def getException = resolution match { case Right(exception) => exception }

  def when(listener: PromiseListener[T]) {
    listeners = listeners :+ listener
  }

  private[etypedactors] def notifyResolved(result: T) {
    for (listener <- listeners) listener onResult result
  }

  private [etypedactors] def notifySmashed(exception: Exception) {
    for (listener <- listeners) listener onException exception
  }

}

private[etypedactors] final class Resolver[T] (val promise: UnresolvedPromise[T], val sender: ActorType) extends Serializable {

  def resolve(result: T) {
    sender ! Resolution(promise, result)
  }

  def smash(exception: Exception) {
    sender ! Smashing(promise, exception)
  }

}