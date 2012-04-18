package examples;

import akka.actor.ActorSystem;
import akka.actor.TypedActor;
import akka.actor.TypedActorExtension;
import akka.actor.TypedProps;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;

public class BasicExampleAkkaJ {

  public static void log(String msg) {
    System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
  }

  public interface Service {
    public Future<Integer> square(int x);
    public int process(int x);
  }

  public static class ServiceActor implements Service {

    public int process(int x) {
      return x*x;
    }

    public Future<Integer> square(int x) {
      log("Service enter");
      final int result = process(x);
      log("Service leave");
      return Futures.successful(result, null);
    }

  }

  public interface Client {
    public void go(Service service);
    public void other(int other);
  }

  public static class ClientActor implements Client {

    public void go(Service service) {
      log("Client enter");
      final Future<Integer> future = service.square(10);
      TypedActor.<Client>self().other(1);
      future.onSuccess(new OnSuccess<Integer>() {
        public void onSuccess(Integer x) {
          log("Client got future result " + x);
          TypedActor.<Client>self().other(x + 2);
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

    public void other(int other) {
      log("Client got message " + other);
    }

  }

  public static void main(String[] args) throws InterruptedException {
    
    final ActorSystem actorSystem = ActorSystem.create("basicJ");
    final TypedActorExtension typedActors = TypedActor.get(actorSystem);

    final Service service =  typedActors.typedActorOf(new TypedProps<ServiceActor>(Service.class, ServiceActor.class));
    final Client client = typedActors.typedActorOf(new TypedProps<ClientActor>(Client.class, ClientActor.class));

    client.go(service);

    // The below method calls are unsupported and will throw an exception
    try { service.square(100); } catch (Exception ex) { log(ex.toString()); ex.printStackTrace(System.out); }
    try { service.process(100); } catch (Exception ex)  { log(ex.toString()); ex.printStackTrace(System.out); }

    Thread.sleep(2000);
    log("Shutting down...");
    typedActors.stop(client);
    typedActors.stop(service);
    actorSystem.shutdown();
    log("Shutdown complete.");
  }

}
