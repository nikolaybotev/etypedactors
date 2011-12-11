package examples;

import akka.actor.TypedActor;
import akka.dispatch.Future;
import akka.japi.Procedure;

public class BasicExampleAkkaJ {

  public static void log(String msg) {
    System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
  }

  public interface Service {
    public Future<Integer> square(int x);
    public int process(int x);
  }

  public static class ServiceActor extends TypedActor implements Service {

    public int process(int x) {
      return x*x;
    }

    public Future<Integer> square(int x) {
      log("Service enter");
      final int result = process(x);
      log("Service leave");
      return future(result);
    }

  }

  public interface Client {
    public void go(Service service);
    public void other(Object other);
  }

  public static class ClientActor extends TypedActor implements Client {

    public void go(Service service) {
      log("Client enter");
      final Future<Integer> future = service.square(10);
      future.onResult(new Procedure<Integer>() {
        public void apply(Integer x) {
          log("Client got future result " + x);
          getContext().<Client>getSelfAs().other(x + 2);
          try {
            Thread.sleep(1000);
          } catch (Exception ex) {
            // Ignore
          }
          log("Client future callback done.");
        }
      });
      log("Client leave");
    }

    public void other(Object other) {
      log("Client got message " + other);
    }

  }

  public static void main(String[] args) throws InterruptedException {

    final Service service = TypedActor.newInstance(Service.class, ServiceActor.class);
    final Client client = TypedActor.newInstance(Client.class, ClientActor.class);

    client.go(service);

    // The below method calls are unsupported and will throw an exception
    try { service.square(100); } catch (Exception ex) { log(ex.toString()); ex.printStackTrace(System.out); }
    try { service.process(100); } catch (Exception ex)  { log(ex.toString()); ex.printStackTrace(System.out); }

    Thread.sleep(2000);
    log("Shutting down...");
    TypedActor.stop(client);
    TypedActor.stop(service);
    log("Shutdown complete.");
  }

}
