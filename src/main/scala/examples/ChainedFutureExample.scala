package examples

import org.etypedactors.ETypedActor
import org.etypedactors.ETypedActorSystem
import org.etypedactors.Promise
import org.etypedactors.akka.AkkaETypedActorFactory
import akka.actor.ActorSystem

object ChainedFutureExample extends App {

  trait Service {
    def doit(x: Int): Promise[Int]
  }

  class ServiceActor extends Service {

    def doit(x: Int): Promise[Int] = {
      val (p, r) = Promise[Int]()
      log("Processing " + x)
      new Thread("background") {
        override def run {
          log("Calculating " + x)
          Thread.sleep(500)
          r.resolve(x * x)
        }
      }.start
      log("Exiting service")
      return p
    }

  }

  trait Client {
    def doit(service: Service)
    def got(x: Int)
  }

  class ClientActor extends ETypedActor[Client] with Client {
    def doit(service: Service) {
      log("Client enter")
      service.doit(42) whenComplete { x =>
        log("Client got " + x + " in when")
        self.got(x)
        Thread.sleep(500)
        log("Client leaving when")
      }
      log("Client leave")
    }
    def got(x: Int) { log("Client got " + x) }
  }

  val akkaSystem = ActorSystem("chained")
  val etypedSystem = ETypedActorSystem.create(new AkkaETypedActorFactory(akkaSystem))

  val service = etypedSystem.createActor(classOf[Service], new ServiceActor)
  val client = etypedSystem.createActor(classOf[Client], new ClientActor)

  log("Sending message")
  client.doit(service)
  log("Message sent")

  Thread.sleep(1500)

  log("Shutting down")
  etypedSystem.stop(service)
  etypedSystem.stop(client)
  akkaSystem.shutdown()
  log("Shutdown complete")

}