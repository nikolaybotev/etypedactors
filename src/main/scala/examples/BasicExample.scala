package examples

import org.etypedactors.ETypedActor
import org.etypedactors.Promise
import org.etypedactors.akka.AkkaEtypedActorFactory

object BasicExample extends App {

  def log(msg: String) {
    println("[" + Thread.currentThread.getName + "] " + msg)
  }

  trait Service {
    def square(x: Int): Promise[Int]
  }

  class ServiceActor extends Service with Serializable {

    def process(x: Int) = x*x

    def square(x: Int) = {
      log("Service enter")
      val result = process(x)
      log("Service leave")
      ETypedActor.fulfill(result)
    }

  }

  trait Client {
    def go(service: Service)
    def other(other: Any)
  }

  class ClientActor extends Client with Serializable {

    def go(service: Service) {
        log("Client enter")
        val future = service.square(10)
        future when {
          x =>
            log("Client got future result " + x)
            ETypedActor.currentActor[Client].other(x + 2)
            Thread.sleep(1000)
            log("Client future callback done.")
        }
        log("Client leave")
    }

    def other(other: Any) {
      log("Client got message " + other)
    }

  }

  val etypedSystem = ETypedActor.create(new AkkaEtypedActorFactory())

  val service = etypedSystem.createActor(classOf[Service], new ServiceActor)
  val client = etypedSystem.createActor(classOf[Client], new ClientActor)

  client.go(service)

  service.square(100)

  Thread.sleep(2000)
  log("Shutting down...")
  etypedSystem.stop(client)
  etypedSystem.stop(service)
  log("Shutdown complete.")

}