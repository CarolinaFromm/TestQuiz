package ClientSide;

import AccessFromBothSides.Response;
import java.io.*;
import java.net.Socket;

// Client klassen hanterar kommunikationen mellan servern för quizkampen.
// Läser av svar från Responsklassen från servern och skickar tillbaka data. Delegerar presentation och användarinteraktion till QuizPanel
// Använder do-while loop för att tillåta att spelet startas om

// hanterar klientens funktionalitet inkl. kommunikation med server
public class Client {
    // Bool som används för att avgöra om spelet ska starta eller om det är klart
    public static boolean replayable = false;

    // konstruktor som etablerar anslutningen till servern och hanterar spelets huvudlogik
    public Client() {
        // IP "namn" + port nr
        String hostName = "localhost";
        int portNumber = 23456;

        // Do-while loop som gör att spelet startas om, om relayable är true
        do {
            try (Socket socket = new Socket(hostName, portNumber); // hostName och portNr för att skapa anslutning
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Skickar data från klient till server via utströmmen
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){ // Läser objekt (tex. response) från servern

                // Quizpanel-objekt skapas för att hantera gränssnitt och interaktionen med användaren
                QuizPanel quizPanel = new QuizPanel(socket, out, in);
                Object obj; // Deklarerar en variabel för att lagra objekt som tas emot av servern

                while (true) {
                    obj = in.readObject(); // Läser in objekt från servern i en loop
                    if (obj instanceof Response response) { // Kollar om mottagna objektet är av typen response, om ja; typkonverterar objekt till en response
                        if (response.getType() == Response.MESSAGE) { // Hanterar message från response och visar det i gränssnittet
                            quizPanel.messageFrame(response.getMessage());
                        } else if (response.getType() == Response.CATEGORY) { // Hanterar kategorivalet från response genom att visa kategori panelen
                            quizPanel.showCategorySelection();
                        } else if (response.getType() == Response.QUESTION) { // Visa frågan och svarsalternativen
                            quizPanel.showQuestionStage(response.getQuestionData());
                        } else if (response.getType() == Response.ANSWER_CHECK) { // Visar om det är rätt eller fel
                            quizPanel.showFeedback(response);
                        } else if (response.getType() == Response.ROUND_SCORE) { // Visar resultat för den aktuella rundan
                            quizPanel.showRoundScore(response);
                        } else if (response.getType() == Response.FINAL_SCORE) { // Visar slutgiltiga reslutatet
                            quizPanel.showFinalScore(response);
                        } else if (response.getType() == Response.PLAY_AGAIN) { // Om spelet ska startas om
                            quizPanel.closeMainPanel();
                            break; // If yes, break
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("The connection was interrupted!"); // Anslutnings-fel
            } catch (ClassNotFoundException e) {
                System.err.println("Incorrect class cast."); // Om det lästa objektet inte matchar förväntad klass (Response)
            }
        } while (replayable); // Fortsätter loopen om replayable är true
    }


    public static void main(String[] args) {
        Client client = new Client();
    }
}