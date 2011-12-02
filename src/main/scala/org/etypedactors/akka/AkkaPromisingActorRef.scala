package org.etypedactors.akka

import akka.actor.ActorRef

class AkkaPromisingActorRef(actor: ActorRef) {

  def !(message: Any) { actor ! message }

}