package examples

import org.etypedactors.Promise
import org.etypedactors.ETypedActor
import org.etypedactors.akka.AkkaETypedActorFactory

object ChainedFutureExample extends App {

  trait Service {
    def doit(x: Int): Promise[Int]
  }

  class ServiceActor extends Service {

    def doit(x: Int): Promise[Int] = {
      val (p, r) = Promise.apply[Int]()
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

  class ClientActor extends Client {
    def doit(service: Service) {
      log("Client enter")
      service.doit(42) when { x =>
        log("Client got " + x + " in when")
        ETypedActor.current[Client].got(x)
        Thread.sleep(500)
        log("Client leaving when")
      }
      log("Client leave")
    }
    def got(x: Int) { log("Client got " + x) }
  }

  val etypedSystem = ETypedActor.create(new AkkaETypedActorFactory())

  val service = etypedSystem.createActor(classOf[Service], new ServiceActor)
  val client = etypedSystem.createActor(classOf[Client], new ClientActor)

  log("Sending message")
  client.doit(service)
  log("Message sent")

  Thread.sleep(1500)

  log("Shutting down")
  etypedSystem.stop(service)
  etypedSystem.stop(client)
  log("Shutdown complete")

}