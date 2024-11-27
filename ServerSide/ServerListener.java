package ServerSide;

import AccessFromBothSides.Response;
import java.io.IOException;
import java.net.ServerSocket;

public class ServerListener {
    public ServerListener() {
        try (ServerSocket listener = new ServerSocket(23456)){ // Skapa en server-socket port: 23456
            System.out.println("The server is running!"); // Säger till om servern körs
            while (true) { // Loop för att lyssna på anslutningar
                Player player1 = new Player(listener.accept(), '1'); // Väntar på att en spelare ansluter
                player1.sendToClient(new Response(Response.MESSAGE,
                        "Welcome Player 1. Please wait for Player 2 to connect.")); // Hälsa första spelaren välkommen
                Player player2 = new Player(listener.accept(), '2'); // Vänta på en andra spelare ska ansluta
                Game game = new Game(player1, player2); // Skapar ett nytt spel för p1, p2.
                game.start(); // Starta ett spel i en separat tråd
            }
        } catch (IOException e) { // Fångar fel vid in/ut ström
            System.err.println("In/Out stream error."); // Logga fel
            e.printStackTrace(); // Print stacktrace
        }
    }

    public static void main(String[] args) {
        ServerListener sl = new ServerListener(); // Skapa och starta servern
    }
}