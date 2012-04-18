package examples;

import org.etypedactors.ETypedActor;
import org.etypedactors.ETypedActorSystem;
import org.etypedactors.Promise;
import org.etypedactors.PromiseListener;
import org.etypedactors.akka.AkkaETypedActorFactory;

import akka.actor.ActorSystem;

public class BasicExampleJ {

  public static void log(String msg) {
    System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
  }

  public interface Service {
    public Promise<Integer> square(int x);
    public int process(int x);
  }

  public static class ServiceActor extends ETypedActor<Service> implements Service {

    public int process(int x) {
      return x*x;
    }

    public Promise<Integer> square(int x) {
      log("Service enter");
      final int result = process(x);
      log("Service leave");
      return fulfill(result);
    }

  }

  public interface Client {
    public void go(Service service);
    public void other(int other);
  }

  public static class ClientActor extends ETypedActor<Client> implements Client {

    public void go(Service service) {
      log("Client enter");
      final Promise<Integer> future = service.square(10);
      self().other(1);
      future.when(new PromiseListener<Integer>() {
        public void onResult(Integer x) {
          log("Client got future result " + x);
          self().other(x + 2);
          try {
            Thread.sleep(1000);
          } catch (Exception ex) {
            // Ignore
          }
          log("Client future callback done.");
        }
        public void onException(Exception exception) { /* empty */ }
      });
      log("Client leave");
    }

    public void other(int other) {
      log("Client got message " + other);
    }

  }

  public static void main(String[] args) throws InterruptedException {
    final ActorSystem akkaSystem = ActorSystem.create("basic");
    final ETypedActorSystem etypedSystem = ETypedActorSystem.create(new AkkaETypedActorFactory(akkaSystem));

    final Service service = etypedSystem.createActor(Service.class, ServiceActor.class);
    final Client client = etypedSystem.createActor(Client.class, ClientActor.class);

    client.go(service);

    // The below method calls are unsupported and will throw an exception
    try { service.square(100); } catch (Exception ex) { log(ex.toString()); ex.printStackTrace(System.out); }
    try { service.process(100); } catch (Exception ex)  { log(ex.toString()); ex.printStackTrace(System.out); }

    Thread.sleep(2000);
    log("Shutting down...");
    etypedSystem.stop(client);
    etypedSystem.stop(service);
    akkaSystem.shutdown();
    log("Shutdown complete.");
  }

}
