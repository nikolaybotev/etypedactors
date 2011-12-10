package org.etypedactors

abstract class ETypedActor[T] {
  myself: T =>

  def self: T = ETypedActorSystem.current[T]

  def fulfill[Y](value: Y): Promise[Y] = ETypedActorSystem.fulfill(value)

}