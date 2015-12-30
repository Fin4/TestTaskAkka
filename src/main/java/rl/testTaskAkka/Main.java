package rl.testTaskAkka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import rl.testTaskAkka.actors.AdderManager;

import java.io.*;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        ActorSystem countSystem = ActorSystem.create("countSystem");
        final ActorRef adderManager = countSystem.actorOf(Props.create(AdderManager.class), "adderManager");

        try (BufferedReader reader = new BufferedReader(new FileReader(createFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {

                String[] tokens = line.split(";", 2);
                AdderManager.Message message = new AdderManager.Message(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));

                adderManager.tell(message, ActorRef.noSender());
            }

            adderManager.tell("END_OF_FILE", ActorRef.noSender());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File createFile() throws IOException {

        File directory = new File("files/");

        if (!directory.exists() && directory.mkdir()) {
            throw new IOException();
        }

        File file = new File("files/nonSorted.txt");
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            Random random = new Random();

            for (int i = 0; i <= 99999; i++) {
                String line = String.format("%d;%d", random.nextInt(1001), random.nextInt(10));
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        }
        return file;
    }
}
