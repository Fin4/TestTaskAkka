package rl.testTaskAkka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import rl.testTaskAkka.actors.AdderManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.*;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {

        ActorSystem countSystem = ActorSystem.create("countSystem");
        final ActorRef adderManager = countSystem.actorOf(Props.create(AdderManager.class), "adderManager");

        createFile("files/");

        try {
            Files.lines(Paths.get("files/nonSorted.txt")).forEach(s -> {
                String[] tokens = s.split(";", 2);
                AdderManager.Message message = new AdderManager.Message(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));

                adderManager.tell(message, ActorRef.noSender());
            });

            adderManager.tell("END_OF_FILE", ActorRef.noSender());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createFile(String path) throws IOException {

        File directory = new File(path);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException();
        }

        File file = new File(path + "nonSorted.txt");
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
