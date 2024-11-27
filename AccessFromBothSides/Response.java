package AccessFromBothSides;

import java.io.Serializable;
import java.util.ArrayList;

// Klassen Response används för att representera olika typer av svar i spelet.

// Serializable gör att objekt i denna klass kan serialiseras vilket gör det möjligt att lagra eller överföra objekt
// Respons implementerar Serializable. Vilket gör att Response objekt kan konverteras till byte-ström
public class Response implements Serializable {

    // Deklarera konstanter som representerar olika typer av svar.
    // Används för att identifiera vilken typ av response som ska skickas
    public final static int CATEGORY = 0;
    public final static int QUESTION = 1;
    public final static int ANSWER_CHECK = 2;
    public final static int FINAL_SCORE = 3;
    public final static int MESSAGE = 4;
    public static final int ROUND_SCORE = 5;
    public static final int PLAY_AGAIN = 6;

    // Indikerar typen av svar
    private int type;
    // Håller reda på aktuell runda
    private int currentRound = 1;
    // Håller reda på aktuell fråga
    private int currentQ = 1;
    // Poäng för spelarna
    private int player1score = 0;
    private int player2score = 0;
    // Poäng för den aktuella rundan
    private int p1RoundScore = 0;
    private int p2RoundScore = 0;
    // Anger om det är rätt eller fel
    private boolean correctAnswer;
    // Lista av strängar som representerar frågan och dess data
    private ArrayList<String> question;
    // Valfritt meddelande som kan inkluderas i svaret
    private String message;

    // Konstruktor som används för att skapa ett respons-objekt som innehåller info om aktuell runda, fråga, poäng och meddelande
    public Response(int type, int currentRound, int currentQ, int player1score, int player2score,
                    ArrayList<String> question, String optionalMessage) {
        this.type = type;
        this.currentRound = currentRound;
        this.currentQ = currentQ;
        this.player1score = player1score;
        this.player2score = player2score;
        this.question = question;
        this.message = optionalMessage;
    }
    // Konstruktor för att skapa ett responsobjekt som fokuserar på rund och spelar poäng + meddelande
    public Response(int type, int currentRound, int player1score, int player2score,
                    int p1RoundScore, int p2RoundScore, String optionalMessage) {
        this.type = type;
        this.currentRound = currentRound;
        this.player1score = player1score;
        this.player2score = player2score;
        this.p1RoundScore = p1RoundScore;
        this.p2RoundScore = p2RoundScore;
        this.message = optionalMessage;
    }
    // Konstruktor för att skapa ett respons-objekt för att rapportera rundpoäng
    public Response(int type, int currentRound, int p1RoundScore, int p2RoundScore) {
        this.type = type;
        this.currentRound = currentRound;
        this.p1RoundScore = p1RoundScore;
        this.p2RoundScore = p2RoundScore;
    }
    // konstruktor med endast typ och meddelande
    public Response(int type, String message) {
        this.type = type;
        this.message = message;
    }
    // konstruktor för rätt eller fel + meddelande
    public Response(int type, boolean correctAnswer) {
        this.type = type;
        this.correctAnswer = correctAnswer;
    }
    // Getters
    public int getCurrentRound() {
        return currentRound;
    }

    public int getP1RoundScore() {
        return p1RoundScore;
    }

    public int getP2RoundScore() {
        return p2RoundScore;
    }

    public int getPlayer1score() {
        return player1score;
    }

    public int getPlayer2score() {
        return player2score;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public boolean isCorrectAnswer() {
        return correctAnswer;
    }

    //En toString metod bara föra att kunna se i consolen att t ex poängen mm. uppdateras.
    @Override
    public String toString() {
        return "Response{" +
                "type=" + type +
                ", currentRound=" + currentRound +
                ", currentQ=" + currentQ +
                ", player1score=" + player1score +
                ", player2score=" + player2score +
                ", question=" + question +
                ", message='" + message +
                ", roundScore=" + ROUND_SCORE + '\'' +
                '}';
    }

    public ArrayList<String> getQuestionData() {
        return question;
    }
}
