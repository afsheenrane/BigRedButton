package adminpanel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MainClientFrame {
    private final JFrame mainFrame;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final SearchPane searchPane;
    private AdminPane adminPane;

    private final int PORT = 2222;
    private String buttonStatus;

    public MainClientFrame() {

        buttonStatus = " ";

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
            addAdminPane();
            listenForInput();

        }
        catch (UnknownHostException e) {
            closeAll();
            searchPane.setStatusLabText("Cannot find host: " + userInput);

        }
        catch (IOException e) {
            closeAll();
            searchPane
                    .setStatusLabText("Cannot connect to host, check if server active.");
        }
    }

    private void addAdminPane() {
        adminPane = new AdminPane(this);

        mainFrame.remove(searchPane);
        mainFrame.add(adminPane);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (checkQuit() == JOptionPane.OK_OPTION) {
                    closeAll();
                    mainFrame.dispose();
                    System.exit(0);
                }
            }

        });

        mainFrame.pack();
        mainFrame.validate();
        mainFrame.repaint();
        mainFrame.setVisible(true);
    }

    private void listenForInput() {
        buttonStatus = " ";

        try {
            while ((buttonStatus = in.readLine()) != null) {
                adminPane.updateStatusLabel(buttonStatus);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void closeAll() {
        try {
            socket.close();
            in.close();
            out.close();
        }
        catch (IOException e) {
        }

    }

    /**
     * Sends the big red button server a command.
     * 
     * @param command the command to send.
     */
    public void tellServer(String command) {
        out.println(command);
    }

    /**
     * @return the buttonStatus
     */
    public String getButtonStatus() {
        return buttonStatus;
    }

    public int checkQuit() {
        if (buttonStatus.equalsIgnoreCase("buttondead"))
            return JOptionPane.OK_OPTION;

        return JOptionPane
                .showConfirmDialog(
                        null,
                        "Closing this will cause you to disconnect from the Big Red Button.\nAre you sure you want to exit?",
                        "Shutdown confirmation", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        new MainClientFrame();
    }

}
