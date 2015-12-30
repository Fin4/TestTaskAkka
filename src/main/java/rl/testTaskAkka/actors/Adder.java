package rl.testTaskAkka.actors;


import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Adder extends UntypedActor {

    private int id;
    private int totalAmount = 0;

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object o) throws Exception {

        if (o instanceof AdderManager.Message) {
            AdderManager.Message msg = (AdderManager.Message) o;
            id = msg.getId();
            totalAmount += msg.getAmount();
        }
        else if (o instanceof String) {
            if (o.toString().equals("END_OF_FILE")) {
                sender().tell(new AdderManager.AdderResultMessage(this.id, this.totalAmount), getSelf());
            }
        }
        else unhandled(o);
    }
}