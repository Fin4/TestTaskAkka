package rl.testTaskAkka.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Future;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static akka.dispatch.Futures.sequence;


public class AdderManager extends UntypedActor {

    Map<Integer, ActorRef> actors = new HashMap<>();

    final List<Future<Object>> futures = new ArrayList<>();

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object o) throws Exception {

        if (o instanceof Message) {
            Message msg = (Message) o;
            getActorRef(msg.getId()).tell(new Message(msg.getId(), msg.getAmount()), getSelf());
        }
        else if (o instanceof String) {
            String msg = (String) o;

            if (msg.equals("END_OF_FILE")) {
                for (Map.Entry<Integer, ActorRef> entry : actors.entrySet()) {
                    Timeout timeout = new Timeout(scala.concurrent.duration.Duration.create(10, "seconds"));
                    Future<Object> f1 = Patterns.ask(entry.getValue(), "END_OF_FILE", timeout);
                    futures.add(f1);
                }
                Future<Iterable<Object>> futuresSequence = sequence(futures, context().system().dispatcher());
                futuresSequence.onComplete(new OnComplete<Iterable<Object>>() {
                    @Override
                    public void onComplete(Throwable throwable, Iterable<Object> objects) throws Throwable {
                        File file = new File("files/Sorted.txt");
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        for (Object o : objects) {
                            writer.write(o.toString());
                            writer.newLine();
                        }
                        writer.close();
                        getContext().system().shutdown();
                    }
                }, getContext().dispatcher());
            }

        }

        else unhandled(o);
    }

    private ActorRef getActorRef(int id) {

        final ActorRef actorRef;
        if (actors.containsKey(id)) {
            actorRef = actors.get(id);

        }
        else {
            log.info(String.format("Create new actor Adder with name adder%d", id));
            actorRef = getContext().actorOf(Props.create(Adder.class), "adder" + id);
            actors.put(id, actorRef);
        }
        return actorRef;
    }

    public static class Message implements Serializable {

        private int id;
        private int amount;

        public int getId() {
            return id;
        }

        public int getAmount() {
            return amount;
        }

        public Message(int id, int amount) {
            this.id = id;
            this.amount = amount;
        }
    }

    public static class AdderResultMessage implements Serializable {

        private int id;
        private int totalAmount;

        public int getId() {
            return id;
        }

        public int getTotalAmount() {
            return totalAmount;
        }

        public AdderResultMessage(int id, int totalAmount) {
            this.id = id;
            this.totalAmount = totalAmount;
        }

        @Override
        public String toString() {
            return String.format("%d;%d", id, totalAmount);
        }
    }
}
