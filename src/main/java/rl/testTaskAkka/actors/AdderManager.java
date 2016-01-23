package rl.testTaskAkka.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Future;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static akka.dispatch.Futures.sequence;

/**
 * Actor, which receiving messages from outside and send them to actor Adder selected by id
 */
public class AdderManager extends AbstractActor {

    Map<Integer, ActorRef> actorRefs = new HashMap<>();

    final List<Future<Object>> futures = new ArrayList<>();

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public AdderManager() {
        receive(ReceiveBuilder
                .match(Message.class, msg -> getActorRef(msg.getId()).tell(new Message(msg.getId(), msg.getAmount()), self()))
                .match(String.class, msg -> generateResultAndShutdown())
                .build());
    }

    private ActorRef getActorRef(int id) {

        final ActorRef actorRef;
        if (actorRefs.containsKey(id)) {
            actorRef = actorRefs.get(id);
        }
        else {
            log.info(String.format("Create new actor Adder with name adder%d", id));
            actorRef = getContext().actorOf(Props.create(Adder.class, () -> new Adder(id)));
            actorRefs.put(id, actorRef);
        }
        return actorRef;
    }

    private void generateResultAndShutdown() {

        actorRefs.entrySet().stream().forEach(integerActorRefEntry -> {
            Timeout timeout = new Timeout(scala.concurrent.duration.Duration.create(10, "seconds"));
            Future<Object> f1 = Patterns.ask(integerActorRefEntry.getValue(), "END_OF_FILE", timeout);
            futures.add(f1);
        });

        sequence(futures, context().system().dispatcher()).onComplete(new OnComplete<Iterable<Object>>() {
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
