package ServerSide;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// Game ärver thread för att köra spelprocessen i en separat tråd
// Gör att servern kan hantera flera spel samtidigt utan att blocka andra anslutningar
public class Game extends Thread {
    private Player player1;
    private Player player2;
    private int numQuestion; // Antal frågor som hämtas från properties filen
    private int numRounds; // Antal rundor som hämtas därifrån med
    private Properties p = new Properties(); // Instans av Properties för att läsa spelinställningarna

    public Game(Player player1, Player player2) {
        try {
            p.load(new FileInputStream("src/ServerSide/Quizkampen.properties")); // Läser egenskaper från fil
        } catch (IOException e) {
            e.printStackTrace(); // Loggar fel vid läsning
        }
        this.player1 = player1;
        this.player2 = player2;
        this.player1.setOpponent(player2); // Anger spelare 2 som motståndare till 1
        this.player2.setOpponent(player1); // Anger spelare 1 som motståndare till 2
        numQuestion = Integer.parseInt(p.getProperty("numberOfQuestions")); // Hämtar antalet frågor
        numRounds = Integer.parseInt(p.getProperty("numberOfRounds")); // Hämtar antalet rundor
    }

    // Metoden run körs när tråden game.start() startar.
    // Skapar en instans av protocol och hanterar spelinställningarna
    public void run() {
        Protocol protocol = new Protocol(numQuestion, numRounds, player1, player2);
    }
}


