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
import javax.swing.JPanel;

public class MainClientFrame {
    private final JFrame searchFrame;
    private JFrame adminFrame;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final SearchPane searchPane;

    private final int PORT = 2222;
    private final String buttonStatus;

    public MainClientFrame() {

        buttonStatus = " ";

        searchFrame = new JFrame();
        searchPane = new SearchPane(this);

        searchFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        searchFrame.setTitle("Button Client");
        searchFrame.add(searchPane);

        searchFrame.setSize(300, 115);
        searchFrame.setResizable(false);
        searchFrame.setLocation(600, 400);
        searchFrame.setVisible(true);
    }

    public void attemptConnection(String userInput) {
        try {

            socket = new Socket(userInput, PORT);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // TODO open admin panel
            addAdminPane();

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
        searchFrame.dispose();

        adminFrame = new JFrame("Admin Panel");
        JPanel adminPane = new AdminPane(this);

        adminFrame.add(adminPane);

        adminFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (checkQuit() == JOptionPane.OK_OPTION) {
                    closeAll();
                    adminFrame.dispose();
                    System.exit(0);
                }
            }

        });

        adminFrame.pack();
        adminFrame.setResizable(false);
        adminFrame.setVisible(true);
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
