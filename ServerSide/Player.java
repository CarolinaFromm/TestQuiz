package ServerSide;

import AccessFromBothSides.Response;
import java.io.*;
import java.net.Socket;
import java.util.Objects;

// Klassen hanterar spelare som kommuniserar via sockets.
// Har metoder för att skicka och ta emot data, sätta en motståndare och stänga anslutningar på ett säkert sätt
// Innehåller felhanteringar för att förhindra att resurser läcker eller om servern kraschar
public class Player {
    private Socket socket; // Socket objekt för att hantera anslutning till spelare i servern
    private char playerNum; // Identifierar spelare
    private Player opponent; // Referens till spelarens motståndare
    private BufferedReader in; // Läser indata från klienten via socket
    private ObjectOutputStream out; // Läser utdata till klienten via socket

    // Skapar em spelare med en socket och ett spelarnummer
    public Player(Socket socket, char playerNum) {
        this.socket = socket; // Tilldelar socket till den här instansen
        this.playerNum = playerNum; // Tilldelar spelarnr.
        try {
            out = new ObjectOutputStream(socket.getOutputStream()); // Skapar en ström för att skicka objekt till klienten
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Skapar en ström för att läsa text från klienten
        } catch (IOException e) { // Hanterar IO Exceptions
            e.printStackTrace();
        }
    }

    public void sendToClient(Response response){ // Metod för att skicka ett response-objekt till klienten
        try {
            if (!socket.isClosed() && socket.isConnected()) { // Kontrollerar om den ff är ansluten
                System.out.println("Sending response to client: " + response);
                out.writeObject(response); // Skriver response-objektt till klientens ström
                out.flush(); // Rensar strömmen för att säkerställa att allt skickas
                System.out.println("Response sent successfully.");
            } else {
                System.err.println("Socket disconnected.1");
                closeConnection(); // Om socketen är stängd eller inte ansluten stängs anslutningen
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public Player getOpponent() {
        return opponent;
    }

    public char getPlayerNum() {
        return playerNum;
    }

    // Kontrollerar om socketen är ansluten och försöker läsa från socketens in-ström
    public String receieveFromClient(){
        String input = "";
        try {
            if (!socket.isClosed() && socket.isConnected()) {
                input = in.readLine();
                if (input.equals("DISCONNECT")) {
                    System.out.println("Client has disconnected.");
                    closeConnection();
                }
            } else {
                System.err.println("Socket disconnected.2");
                closeConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNullElse(input, "");
    }

    // Metod för att stänga anslutningen och strömmarna
    public void closeConnection() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket or in/out streams: " + e.getMessage());
        }
    }
}
