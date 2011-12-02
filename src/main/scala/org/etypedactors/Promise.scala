package org.etypedactors

object Promise {
  def apply[T]()(implicit sender: ActorType): (Promise[T], Resolver[T]) = {
    if (sender == null) throw new IllegalAccessError("No etyped actor in scope.")

    val promise = new Promise[T]
    val resolver = new Resolver[T](promise, sender)
    return (promise, resolver)
  }
}

trait PromiseListener[-T] {
  def onResult(result: T): Unit
  def onException(exception: Exception): Unit
}

final class Promise[T] private[etypedactors] extends Serializable {
  
  private var listeners = collection.immutable.List[PromiseListener[T]]()
  
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

final class Resolver[T] private[etypedactors] (val promise: Promise[T], val sender: ActorType) extends Serializable {

  def resolve(result: T) {
    sender ! Resolution(promise, result)
  }

  def smash(exception: Exception) {
    sender ! Smashing(promise, exception)
  }
  
}