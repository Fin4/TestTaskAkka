package rl.testTaskAkka.actors;


import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;

/**
 * actor that counts the amount for the specified id
 */
public class Adder extends AbstractActor {

    private int id;
    private int totalAmount;

    public Adder(int id) {
        this.id = id;
        this.totalAmount = 0;

        receive(ReceiveBuilder
                .match(AdderManager.Message.class, message -> totalAmount += message.getAmount())
                .match(String.class, message -> sender().tell(new AdderManager.AdderResultMessage(this.id, this.totalAmount), self()))
                .build());
    }

}