package adminpanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;

public class MainClientFrame {
    private final JFrame mainFrame;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final SearchPane searchPane;

    private final int PORT = 2222;

    public MainClientFrame() {
        mainFrame = new JFrame();
        searchPane = new SearchPane(this);

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.setTitle("Button Client");
        mainFrame.add(searchPane);

        mainFrame.setSize(300, 115);
        mainFrame.setResizable(false);
        mainFrame.setLocation(600, 400);
        mainFrame.setVisible(true);
    }

    public void attemptConnection(String userInput) {
        try {
            socket = new Socket(userInput, PORT);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // TODO open admin panel
        }
        catch (UnknownHostException e) {
            searchPane.setStatusLabText("Cannot find host: " + userInput);

        }
        catch (IOException e) {
            searchPane
                    .setStatusLabText("Cannot connect to host, check if server active.");
        }
    }

    public static void main(String[] args) {
        new MainClientFrame();
    }

}
