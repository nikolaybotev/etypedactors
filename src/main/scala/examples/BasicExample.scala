package examples

import org.etypedactors.ETypedActor
import org.etypedactors.ETypedActorSystem
import org.etypedactors.Promise
import org.etypedactors.akka.AkkaETypedActorFactory
import org.etypedactors.scala.ScalaETypedActorFactory
import akka.actor.ActorSystem

object BasicExample extends App {

  trait Service {
    def square(x: Int): Promise[Int]
    def process(x: Int): Int
  }

  class ServiceActor extends ETypedActor[Service] with Service {

    def process(x: Int) = x*x

    def square(x: Int) = {
      log("Service enter")
      val result = process(x)
      log("Service leave")
      fulfill(result)
    }

  }

  trait Client {
    def go(service: Service)
    def other(other: Any)
  }

  class ClientActor extends ETypedActor[Client] with Client {

    def go(service: Service) {
      log("Client enter")
      val future = service.square(10)
      self.other(1)
      future whenComplete { x =>
        log("Client got future result " + x)
        self.other(x + 2)
        Thread.sleep(1000)
        log("Client future callback done.")
      }
      log("Client leave")
    }

    def other(other: Any) {
      log("Client got message " + other)
    }

  }

  val akkaSystem = ActorSystem("basic")
  val etypedSystem = ETypedActorSystem.create(new AkkaETypedActorFactory(akkaSystem))

  val service = etypedSystem.createActor(classOf[Service], new ServiceActor)
  val client = etypedSystem.createActor(classOf[Client], new ClientActor)

  client.go(service)

  // The below method calls are unsupported and will throw an exception
  try { service.square(100) } catch { case ex => log(ex.toString()); ex.printStackTrace(System.out) }
  try { service.process(100) } catch { case ex => log(ex.toString()); ex.printStackTrace(System.out) }

  Thread.sleep(2000)
  log("Shutting down...")
  etypedSystem.stop(client)
  etypedSystem.stop(service)
  akkaSystem.shutdown()
  log("Shutdown complete.")

}