package examples;

import org.etypedactors.ETypedActorSystem;
import org.etypedactors.Promise;
import org.etypedactors.PromiseListener;
import org.etypedactors.akka.AkkaETypedActorFactory;

public class BasicExampleJ {

  public static void log(String msg) {
    System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
  }

  public interface Service {
    public Promise<Integer> square(int x);
    public int process(int x);
  }

  public class ServiceActor implements Service {

    public int process(int x) {
      return x*x;
    }

    public Promise<Integer> square(int x) {
      log("Service enter");
      final int result = process(x);
      log("Service leave");
      return ETypedActorSystem.fulfill(result);
    }

  }

  public interface  Client {
    public void go(Service service);
    public void other(Object other);
  }

  public class ClientActor implements Client {

    public void go(Service service) {
      log("Client enter");
      final Promise<Integer> future = service.square(10);
      future.when(new PromiseListener<Integer>() {
        public void onResult(Integer x) {
          log("Client got future result " + x);
          ETypedActorSystem.<Client>current().other(x + 2);
          Thread.sleep(1000);
          log("Client future callback done.");
        }
        public void onException(Exception exception) { /* empty */ }
      });
      log("Client leave");
    }

    public void other(Object other) {
      log("Client got message " + other);
    }

  }

  public static void main(String[] args) {
    final ETypedActorSystem<?> etypedSystem = ETypedActorSystem.create(new AkkaETypedActorFactory());

    final Service service = etypedSystem.createActor(Service.class, new ServiceActor());
    final Client client = etypedSystem.createActor(Client.class, new ClientActor());

    client.go(service);

    // The below method calls are unsupported and will throw an exception
    try { service.square(100); } catch (Exception ex) { log(ex.toString()); ex.printStackTrace(System.out); }
    try { service.process(100); } catch (Exception ex)  { log(ex.toString()); ex.printStackTrace(System.out); }

    Thread.sleep(2000);
    log("Shutting down...");
    etypedSystem.stop(client);
    etypedSystem.stop(service);
    log("Shutdown complete.");
  }

}
