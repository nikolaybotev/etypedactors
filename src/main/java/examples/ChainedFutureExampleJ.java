package examples;

import org.etypedactors.ETypedActor;
import org.etypedactors.Future;
import org.etypedactors.Promise;
import org.etypedactors.PromiseListener;
import org.etypedactors.ETypedActorSystem;
import org.etypedactors.akka.AkkaETypedActorFactory;

import akka.actor.ActorSystem;

class ChainedFutureExampleJ {

  public static void log(String msg) {
    System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
  }

  public interface Service {
    public Promise<Integer> doit(int x);
  }

  public static class ServiceActor implements Service {

    public Promise<Integer> doit(final int x) {
      final Future<Integer> f = Future.create();
      log("Processing " + x);
      new Thread("background") {
        public void run() {
          log("Calculating " + x);
          try {
            Thread.sleep(500);
          } catch (InterruptedException ex) {
            // Ignore
          }
          f.resolver().resolve(x * x);
        }
      }.start();
      log("Exiting service");
      return f.promise();
    }

  }

  public interface Client {
    public void doit(Service service);
    public void got(int x);
  }

  public static class ClientActor extends ETypedActor<Client> implements Client {
    public void doit(Service service) {
      log("Client enter");
      service.doit(42).when(new PromiseListener<Integer>() {
        public void onResult(Integer x) throws InterruptedException {
          log("Client got " + x + " in when");
          self().got(x);
          Thread.sleep(500);
          log("Client leaving when");
        }
        public void onException(Exception ex) {
          // Empty
        }
      });
      log("Client leave");
    }
    public void got(int x) { log("Client got " + x); }
  }

  public static void main(String[] args) throws InterruptedException {
    final ActorSystem akkaSystem = ActorSystem.create("basic");
    final ETypedActorSystem etypedSystem = ETypedActorSystem.create(new AkkaETypedActorFactory(akkaSystem));

    final Service service = etypedSystem.createActor(Service.class, ServiceActor.class);
    final Client client = etypedSystem.createActor(Client.class, ClientActor.class);

    log("Sending message");
    client.doit(service);
    log("Message sent");

    Thread.sleep(1500);

    log("Shutting down");
    etypedSystem.stop(service);
    etypedSystem.stop(client);
    akkaSystem.shutdown();
    log("Shutdown complete");
  }

}