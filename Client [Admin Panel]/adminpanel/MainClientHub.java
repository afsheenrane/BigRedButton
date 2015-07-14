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
import javax.swing.SwingUtilities;

public class MainClientHub {
    private final JFrame searchFrame;
    private JFrame adminFrame;

    private AdminPane adminPane;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final SearchPane searchPane;

    private final int PORT = 2222;
    private String buttonStatus;

    public MainClientHub() {

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

            addAdminPane();

            // TODO create a input listening thread
            // (new InputListener(in, adminPane)).run();

            while ((buttonStatus = in.readLine()) != null) {
                adminPane.updateStatusLabel(buttonStatus);
            }

        }
        catch (UnknownHostException e) {
            // closeAll();
            searchPane.setStatusLabText("Cannot find host: " + userInput);

        }
        catch (IOException e) {
            closeAll();
            searchPane.setStatusLabText("Cannot connect to " + userInput
                    + ", check if server active.");
        }
    }

    private void addAdminPane() {
        searchFrame.dispose();

        adminFrame = new JFrame("Admin Panel");
        adminPane = new AdminPane(this);

        adminFrame.add(adminPane);

        adminFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("[ADMIN PANEL] window close requested!");
                if (checkQuit() == JOptionPane.OK_OPTION) {
                    tellServer("clientdisconnected");
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
            if (socket != null)
                socket.close();

            if (in != null)
                in.close();

            if (out != null)
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
        System.out.println("[CLIENT] telling server: " + command);
        command.toLowerCase();
        out.println(command);
    }

    /**
     * @return the buttonStatus
     */
    public String getButtonStatus() {
        return buttonStatus;
    }

    public int checkQuit() {
        if (buttonStatus == null || buttonStatus.equalsIgnoreCase("buttondead"))
            return JOptionPane.OK_OPTION;

        return JOptionPane
                .showConfirmDialog(
                        null,
                        "Closing this will cause you to disconnect from the Big Red Button.\nAre you sure you want to exit?",
                        "Shutdown confirmation", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainClientHub();
            }
        });
    }

}
