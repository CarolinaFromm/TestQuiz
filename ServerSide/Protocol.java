package ServerSide;

import AccessFromBothSides.Response;
import java.util.ArrayList;

// Styr spelet logik och kommunikation mellan server och två klienter
// Den här hanterar olika faser i spelet, (kategori, frågor, svar, poängräkning)
// Använder trådar för att hantera spelarna parallellt.
// Finns Exeptions för att hantera anslutningsproblem och hantering av spelarnas bortkoppling
public class Protocol {
    // Konstanter som representerar olika tillsånd i spelet
    private final int CATEGORY = 0;
    private final int QUESTION = 1;
    private final int ANSWER = 2;
    private final int ROUND_SCORE = 3;
    private final int FINAL_SCORE = 4;
    private final int PLAY_AGAIN = 5;
    private final int EXIT = 6;

    private int numQuestion; // Antal frågor per omgång
    private int numRounds; // Antal rundor i spelet
    private int state = CATEGORY; // Spelet startar med att välja kategori
    private int currentRound = 1; // Håller reda på vilken omgång
    private int currentQ = 1; // Håller reda på frågan
    // Poäng för varje spelare, totalen och per omgång
    private int p1Score = 0;
    private int p2Score = 0;
    private int p1RoundScore = 0;
    private int p2RoundScore = 0;
    // Referens till spelare
    private Player player1;
    private Player player2;
    // Vilken spelare är aktiv
    private Player currentPlayer;
    // Array-List för frågor och svar för aktuell kategori
    private ArrayList<ArrayList<String>> questions;
    // Objekt för att hantera frågor och kategorier
    private Category category = new Category();

    // Konstruktor för att initera spelet med antal frågor, omgångar och två spelare
    public Protocol(int numQuestion, int numRounds, Player player1, Player player2) {
        this.numQuestion = numQuestion;
        this.numRounds = numRounds;
        this.player1 = player1;
        this.player2 = player2;
        currentPlayer = player1; // Sätter player 1 som startspelare

        while (true) { // Huvudloop som styr spelets flöde
            if (state == CATEGORY) { // Om spelet är i kategorifasen
                currentPlayer.getOpponent().sendToClient(new Response(Response.MESSAGE,
                        "Please wait for Player " + currentPlayer.getPlayerNum() + " to choose category.")); // Ber p2 att vänta på att p1 väljer kategori
                currentPlayer.sendToClient(new Response(Response.CATEGORY, currentRound, currentQ, p1Score, p2Score, // Skickar till spelare att välja kategori
                        null, null));
                String chosenCategory = currentPlayer.receieveFromClient(); // Tar emot kategorin
                if (chosenCategory.equals("DISCONNECT"))
                    break;
                questions = category.getQuestionsList(chosenCategory); // Hämtar frågorna för den valda kategorin
                if (questions.isEmpty()) {
                    throw new IllegalStateException("No questions found");
                }
                state = QUESTION; // Går vidare till frågefasen
            }
            else if (state == QUESTION) { // Frågefas
                Response qNAs = new Response(Response.QUESTION, currentRound, currentQ, // Skapar en Response med frågan och skickar till båda spelarna
                        p1Score, p2Score, questions.get(currentQ - 1), null);
                sendToBothClients(qNAs);
                state = ANSWER; // Växlar till svarsfältet
            }
            else if (state == ANSWER) {
                /** Skapar trådar för spelarna.
                 * Nu kan t ex player2 skicka in sitt svarsalternativ och få svar på om det var rätt/fel samt få sin poäng
                 * uppdaterat. Innan gick det inte att göra eftersom programmet stod och väntade på player1.
                 */
                Thread player1Thread = new Thread(() -> {
                    try {
                        String player1Answer = player1.receieveFromClient();
                        if (player1Answer.equals("DISCONNECT"))
                            state = EXIT;
                        boolean corrAns; // Ökar poäng om svaret är rätt
                        synchronized (this) {
                            if (questions.get(currentQ - 1).get(1).equals(player1Answer)) {
                                p1RoundScore++; //
                                corrAns = true;
                            } else
                                corrAns = false;
                        }
                        Response answerCheck = new Response(Response.ANSWER_CHECK, corrAns); // Skickar feedback till spelaren om svaret är rätt
                        player1.sendToClient(answerCheck);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                // Samma som för p1
                Thread player2Thread = new Thread(() -> {
                    try {
                        String player2Answer = player2.receieveFromClient();
                        if (player2Answer.equals("DISCONNECT"))
                            state = EXIT;
                        boolean corrAns;
                        synchronized (this) {
                            if (questions.get(currentQ - 1).get(1).equals(player2Answer)) {
                                p2RoundScore++; //
                                corrAns = true;
                            } else
                                corrAns = false;
                        }
                        Response answerCheck = new Response(Response.ANSWER_CHECK, corrAns);
                        player2.sendToClient(answerCheck);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                if (state == EXIT) // Om någon spelare kopplar bort, avsluta spelet
                    break;
                player1Thread.start(); // Starta trådarna för att hantera båda spelarna parallellt
                player2Thread.start();
                try {
                    player1Thread.join();
                    player2Thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Fortsätter med nästa fråga eller fas
                currentQ++;
                if (currentQ > numQuestion && currentRound < numRounds) {
                    state = ROUND_SCORE;
                } else if (currentQ <= numQuestion) {
                    state = QUESTION;
                } else {
                    state = FINAL_SCORE;
                    p1Score += p1RoundScore;
                    p2Score += p2RoundScore;
                }
            }
            else if (state == ROUND_SCORE) { // Visar poäng för omgången och frågar om spelarna vill fortsätta
                Response roundScoreUpdate = new Response(Response.ROUND_SCORE, currentRound,
                        p1RoundScore, p2RoundScore);
                sendToBothClients(roundScoreUpdate);
                // Uppdaterar båda spelarna med poängen

                String p1cont = player1.receieveFromClient();
                String p2cont = player2.receieveFromClient();

                p1Score += p1RoundScore;
                p2Score += p2RoundScore;

                p1RoundScore = 0;
                p2RoundScore = 0;

                currentRound++;
                if (currentRound <= numRounds) {
                    state = CATEGORY;
                    currentQ = 1;
                    currentPlayer = currentPlayer.getOpponent();
                }
            }
            else if (state == FINAL_SCORE) { // Visar slutresultat och spelets avslutning
                if (p1Score > p2Score) {
                    player1.sendToClient(new Response(Response.FINAL_SCORE, currentRound,
                            p1Score, p2Score, p1RoundScore, p2RoundScore, "Victory!"));
                    player2.sendToClient(new Response(Response.FINAL_SCORE, currentRound,
                            p1Score, p2Score, p1RoundScore, p2RoundScore,  "Defeat."));
                } else if (p1Score < p2Score) {
                    player1.sendToClient(new Response(Response.FINAL_SCORE, currentRound,
                            p1Score, p2Score, p1RoundScore, p2RoundScore, "Defeat."));
                    player2.sendToClient(new Response(Response.FINAL_SCORE, currentRound,
                            p1Score, p2Score, p1RoundScore, p2RoundScore, "Victory!"));
                } else {
                    sendToBothClients(new Response(Response.FINAL_SCORE, currentRound,
                            p1Score, p2Score, p1RoundScore, p2RoundScore, "Draw."));
                }
                state = PLAY_AGAIN;
            }
            else if (state == PLAY_AGAIN) { // Hanterar spelarens val om att spela igen
                Thread player1Thread = new Thread(() -> {
                    try {
                        String player1Answer = player1.receieveFromClient();
                        synchronized (this) {
                            if (player1Answer.equals("Again")){
                                Response playAgain = new Response(Response.PLAY_AGAIN, null);
                                player1.sendToClient(playAgain);
                            } else
                                state = EXIT;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                Thread player2Thread = new Thread(() -> {
                    try {
                        String player2Answer = player2.receieveFromClient();
                        synchronized (this) {
                            if (player2Answer.equals("Again")) {
                                Response playAgain = new Response(Response.PLAY_AGAIN, null);
                                player2.sendToClient(playAgain);
                            } else
                                state = EXIT;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                if (state == EXIT)
                    break;

                player1Thread.start();
                player2Thread.start();

                try {
                    player1Thread.join();
                    player2Thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (state == EXIT) { // Avslutar spelet
                break;
            }
        }
        // Stänger anslutningarna för båda spelarna
        player1.closeConnection();
        player2.closeConnection();
    }

    // Skickar meddelande till båda spelarna
    public void sendToBothClients(Response response) {
        player1.sendToClient(response);
        player2.sendToClient(response);
    }
}
