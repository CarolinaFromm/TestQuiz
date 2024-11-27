package ClientSide;

// Importerar enum och response för att kunna hantera kategorier och serverns svar
import AccessFromBothSides.EnumCategories;
import AccessFromBothSides.Response;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import static java.lang.Thread.sleep;


public class QuizPanel {
    private JFrame frame; // Huvudfönster
    private JPanel mainPanel; // Hantera innehållet i fönstret
    private JButton clickedButton; // Håller koll på vilket svar användaren har klickat på
    private final String backgroundImagePath = "src/ClientSide/graphics/gradient.png"; // Bakgrundsbild
    private final String buttonImagePath = "src/ClientSide/graphics/button.png"; // knappar
    private final String buttonGreenImagePath = "src/ClientSide/graphics/GrönKnapp.png"; // Grön för rätt svar
    private ImageIcon buttonIcon = new ImageIcon(buttonImagePath); // Knappens ikon
    private ImageIcon buttonGreenIcon = new ImageIcon(buttonGreenImagePath); // Ikon för korrekt svar
    private ImageIcon backgroundIcon = new ImageIcon(backgroundImagePath); // Ikon för bakgrund
    private Image background = backgroundIcon.getImage(); // Bakgrundsbilden
    private ArrayList<String> roundScoreList = new ArrayList<>(); // Lista som håller poäng för rundan
    private Font buttonFont = new Font("Arial", Font.PLAIN, 16); // Standard font för knappar
    private Font headerFont = new Font("Montserrat", Font.PLAIN, 24); // Standard font för rubriker
    private Socket socket; // Närverksanslutning
    private ObjectInputStream in; // Läsa objekt från servern
    private PrintWriter out; // Skicka meddelande till servern
    private JScrollPane scrollPane; // Scroll

    // Konstruktor för nätverk + gui
    public QuizPanel(Socket socket, PrintWriter out, ObjectInputStream in) {
        this.socket = socket; // Tilldela socket
        this.out = out; // Tilldela output stream
        this.in = in; // Tilldela input stream
        mainFrame(); // Initera huvudfönstret
    }

    private void mainFrame() {
        frame = new JFrame("Quizkampen"); // Skapa huvudfönster
        frame.setSize(600, 400); // Storlek
        frame.setLocationRelativeTo(null); // Mitten på skärmen
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() { // Lyssnare för stängningshändelse
            @Override
            public void windowClosing(WindowEvent e) {
                if (!socket.isClosed()) {
                    closeConnection();
                }
                System.exit(0); // Avsluta programmet
            }
        });
        mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) { // Anpassad bakgrundsbild
                super.paintComponent(g);
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        };

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Visa endast scroll om det behövs
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // visar meddelanden typ välkommen
    public void messageFrame(String message) {
        mainPanel.removeAll(); // Rensa panelen
        JLabel label = new JLabel("<html><div style='width:400px;'>" + message + "</div></html>", JLabel.CENTER); // Dynamisk bredd
        label.setFont(headerFont); // Sätter font
        label.setForeground(Color.DARK_GRAY); // Textfärg
        mainPanel.add(label, BorderLayout.CENTER); // Texten i mitten
        mainPanel.revalidate();
        mainPanel.repaint(); // Uppdatera panelen
    }

    public void showCategorySelection() {
        mainPanel.removeAll(); // Rensa panelen

        JLabel label = new JLabel("Choose your category", JLabel.CENTER); // Rubrik
        label.setFont(headerFont);
        label.setForeground(Color.DARK_GRAY);
        mainPanel.add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3,2));
        buttonPanel.setOpaque(false);

        // Hämta kategorierna från enum
        ArrayList<EnumCategories> listOfCategories = new ArrayList<>();
        Collections.addAll(listOfCategories, EnumCategories.values());

        for (EnumCategories enumCategories : listOfCategories) { // Skapa knapparna för varje kategori
            JButton button = new JButton(enumCategories.getText(), buttonIcon);
            button.setBorderPainted(false); // Kanterna syns inte
            button.setContentAreaFilled(false); // Transparent
            button.setFocusPainted(false);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.CENTER);
            button.addActionListener(e -> sendStringToServer(enumCategories.getValue()));
            button.setFont(buttonFont);
            buttonPanel.add(button);
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showQuestionStage(ArrayList<String> questionData) {
        mainPanel.removeAll();

        JLabel questionLabel = new JLabel("<html><div style='width:400px;'>" +
                questionData.getFirst() + "</div></html>", JLabel.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(questionLabel, BorderLayout.NORTH);

        JPanel answerPanel = new JPanel();
        answerPanel.setLayout(new GridLayout(2,2));
        answerPanel.setOpaque(false);

        for (int i = 2; i < questionData.size(); i++) { // Lägg till svarsknappar
            JButton answerButton = new JButton(questionData.get(i), buttonIcon);
            answerButton.setBorderPainted(false);
            answerButton.setContentAreaFilled(false);
            answerButton.setHorizontalTextPosition(SwingConstants.CENTER);
            answerButton.setVerticalTextPosition(SwingConstants.CENTER);
            answerButton.setFont(buttonFont);
            answerButton.setFocusPainted(false);
            answerButton.addActionListener(e -> {
                sendStringToServer(answerButton.getText()); // Skicka svaret till servern
                clickedButton = answerButton; // Spara klickad knapp
                for (Component component : answerPanel.getComponents()) { // hindrar att man kan klicka på flera svar
                    if (component instanceof JButton && component != clickedButton) {
                        component.setEnabled(false);
                    }
                }
            });
            answerPanel.add(answerButton);
        }
        mainPanel.add(answerPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Visa feedback på svaret
    public void showFeedback(Response feedback) {
        if (!feedback.isCorrectAnswer()) {
            clickedButton.setForeground(Color.red); // röd är fel svar
        } else
            clickedButton.setIcon(buttonGreenIcon); // grön är rätt
        try {
            sleep(700); // Pausa lite för att hinna se om man svarade rätt
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Visar poäng för den aktuella ronden + en knapp för att fortsätta. Om spelaren klickar, skickas svar till servern och sen inväntar motspelare
    public void showRoundScore(Response response) {
        final String cont = "Continue";
        mainPanel.removeAll();
        JPanel centrePanel = new JPanel(new GridLayout(10, 1));
        centrePanel.setOpaque(false);
        mainPanel.add(centrePanel,BorderLayout.CENTER);
        JLabel label = new JLabel("Round " + response.getCurrentRound() + " score", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.BLACK);
        JLabel waiting = new JLabel("Waiting for other player", JLabel.CENTER);
        waiting.setFont(headerFont);
        JButton contButton = new JButton(cont);
        contButton.addActionListener(e -> {
            sendStringToServer(cont);
            mainPanel.add(waiting, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        roundScoreList.add("Player 1: " + response.getP1RoundScore() + "\t\t\t\t\t\t" + response.getCurrentRound()
                + "\t\t\t\t\t\tPlayer 2: " + response.getP2RoundScore());
        for (String s : roundScoreList) {
            JLabel rScoreLabel = new JLabel(s, JLabel.CENTER);
            rScoreLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            centrePanel.add(rScoreLabel, BorderLayout.CENTER);
        }
        mainPanel.add(label, BorderLayout.NORTH);
        mainPanel.add(contButton, BorderLayout.SOUTH);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Visa slutresultatet + alla ronders poänd och slutpoängen. Spelaren kan välja att spela igen.
    public void showFinalScore(Response response) {
        mainPanel.removeAll();
        final String playAgain = "Again";
        JPanel centrePanel = new JPanel(new GridLayout(10, 1));
        mainPanel.add(centrePanel,BorderLayout.CENTER);
        JLabel finalScoreLabel1 = new JLabel(response.getMessage(), JLabel.CENTER);
        JLabel finalScoreLabel2 = new JLabel("Player 1: "
                + response.getPlayer1score() + "\t\t\t - \t\t\tPlayer 2: "
                + response.getPlayer2score(), JLabel.CENTER);
        finalScoreLabel1.setFont(new Font("Arial", Font.BOLD, 20));
        finalScoreLabel2.setFont(new Font("Arial", Font.BOLD, 16));
        centrePanel.add(finalScoreLabel1);
        centrePanel.add(finalScoreLabel2);
        centrePanel.setOpaque(false);
        JPanel southPanel = new JPanel(new GridLayout(1,2));

        roundScoreList.add("Player 1: " + response.getP1RoundScore() + "\t\t\t\t\t\t" + response.getCurrentRound()
                + "\t\t\t\t\t\tPlayer 2: " + response.getP2RoundScore());
        for (String s : roundScoreList) {
            JLabel rScoreLabel = new JLabel(s, JLabel.CENTER);
            centrePanel.add(rScoreLabel);
        }
        JButton playAgainButton = new JButton("Play again");
        JButton exitButton = new JButton("Exit");
        playAgainButton.addActionListener(e -> {
            Client.replayable = true;
            sendStringToServer(playAgain);
        });
        exitButton.addActionListener(e -> {
            closeConnection();
            System.exit(0);
        });
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        southPanel.setOpaque(false);
        southPanel.add(playAgainButton);
        southPanel.add(exitButton);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Skickar meddelande från client till servern via socket. Om anslutningen är bruten stängs strömmarna
    public void sendStringToServer(String message) {
        try {
            if (!socket.isClosed() && socket.isConnected()) { // kollar att socketen är aktiv
                out.println(message); // Skicka meddelande
                out.flush(); // Töm output-buffer
            } else {
                System.err.println("Socket disconnected."); // Felmeddelande om anslutning är bruten
                closeConnection();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Felhantering
        }
    }

    public void closeMainPanel(){
        frame.dispose();
    } // Stäng GUI-fönstret

    // Säkerställer att socketen och strömmarna stängs korrekt när spelet är slut
    public void closeConnection() {
        try {
            out.println("DISCONNECT"); // Skicka "DISCONNECT" till servern
            out.flush();
            out.close(); // Stäng output ström
            in.close(); // Stäng input ström
            socket.close(); // Stäng socketen
        } catch (IOException e) {
            System.err.println("Error closing socket or in/out streams: " + e.getMessage());
        }
    }
}
