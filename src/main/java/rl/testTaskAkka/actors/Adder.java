package rl.testTaskAkka.actors;


import akka.actor.*;

public class Adder extends UntypedActor {

    private int id;
    private int totalAmount;

    @Override
    public void onReceive(Object o) throws Exception {

        if (o instanceof AdderManager.Message) {
            AdderManager.Message msg = (AdderManager.Message) o;
            if (id == msg.getId()) {
                totalAmount += msg.getAmount();
            }
        }
        else if (o instanceof String) {
            if (o.toString().equals("END_OF_FILE")) {
                sender().tell(new AdderManager.AdderResultMessage(this.id, this.totalAmount), getSelf());
            }
        }
        else unhandled(o);
    }

    public Adder(int id) {
        this.id = id;
        this.totalAmount = 0;
    }
}