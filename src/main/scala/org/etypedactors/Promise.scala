package org.etypedactors

case object Future {

  /**
   * Java API.
   */
  def create[T](): Future[T] = {
    val (promise, resolver) = Promise[T]()
    return Future(promise, resolver)
  }

}

case class Future[T](promise: Promise[T], resolver: Resolver[T])

object Promise {

  def apply[T](): (Promise[T], Resolver[T]) = {
    val current = ETypedActorSystem.currentActorWithProxy
    if (current == null)
      throw new IllegalArgumentException("No ETypedActor in scope. " +
          "Promise-returning actor methods can only be called from other ETyped actors. ")
    val actor: IdiomaticActor = current.actorRef

    val promise = new InActorPromise[T]()
    val resolver = new Resolver[T](promise, actor)
    return (promise, resolver)
  }
}

trait PromiseListener[-T] {
  @throws(classOf[Exception]) def onResult(result: T): Unit
  @throws(classOf[Exception]) def onException(exception: Exception): Unit
}

trait Promise[T] extends Serializable {

  def isResolved: Boolean

  def isSmashed: Boolean

  def getValue: T

  def getException: Exception

  def when(listener: PromiseListener[T])

  def whenComplete(resultHandler: T => Unit, exceptionHandler: PartialFunction[Exception, Unit] = {case e => throw e}) {
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
  override def whenComplete(resultHandler: T => Unit, exceptionHandler: PartialFunction[Exception, Unit] = {case ex=>}) = resultHandler(result)
}

private[etypedactors] final class InActorPromise[T] extends Promise[T] {

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

private[etypedactors] final class Resolver[T] (val promise: InActorPromise[T], val sender: IdiomaticActor) extends Serializable {

  def resolve(result: T) {
    sender ! Resolution(promise, result)
  }

  def smash(exception: Exception) {
    sender ! Smashing(promise, exception)
  }

}