package redbutton;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainRedButtonServer {

    private JFrame awaitingConnFrame;
    private JFrame buttonFrame;

    private JPanel launchPane;
    private ServerSocket serverSocket = null;
    private final int PORT = 2222;
    private String currentState = "";
    private boolean buttonEnabled = false;
    ArrayList<ConnectionThread> clients;

    public MainRedButtonServer() throws IOException {
        super();
        // First try opening the port.
        try {
            serverSocket = new ServerSocket(PORT);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot listen on port: "
                    + PORT + "\nPossibly another server open on that port");
            serverSocket.close();
            System.exit(1);
        }

        // Once the port has been successfully opened, wait for connections.
        createAwaitingConnWindow();

        clients = new ArrayList<ConnectionThread>();

        while (true) {
            try {
                if (clients.add(new ConnectionThread(serverSocket.accept(),
                        this))) {
                    if (!buttonEnabled) {
                        showBigRedButton();
                        buttonEnabled = true;
                    }
                    clients.get(clients.size() - 1).run();
                }
            }
            catch (SocketException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        }

    }

    private void showBigRedButton() {
        // awaitingConnFrame.setVisible(false);
        awaitingConnFrame.dispose();
        // awaitingConnFrame = null;

        buttonFrame = new JFrame();

        buttonFrame.setFocusable(true);
        buttonFrame.setLocation(1, 1);
        buttonFrame.setUndecorated(true);

        JComponent buttonComp = new BigButtonPane(this);

        buttonFrame.add(buttonComp);

        buttonFrame.pack();
        buttonFrame.setResizable(false);

        buttonFrame.requestFocusInWindow();
        // buttonFrame.setAlwaysOnTop(true);

        buttonFrame.setVisible(true);
    }

    private void updateFrame(JFrame frame) {
        frame.pack();
        frame.validate();
        frame.repaint();
    }

    /**
     * Create the window that says the server is waiting for clients to connect.
     */
    private void createAwaitingConnWindow() {

        JButton shutdownButton = new JButton("SHUTDOWN SERVER");
        JLabel hostNameLabel = new JLabel("Computer Name: " + getMachineName());
        JLabel statusLabel = new JLabel("Awaiting connections on port " + PORT);

        awaitingConnFrame = new JFrame("Button Server");
        launchPane = new JPanel(new BorderLayout());

        awaitingConnFrame.setLocation(400, 400);

        shutdownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // showBigRedButton();
                shutdownServer();
            }
        });

        launchPane.add(statusLabel, BorderLayout.NORTH);
        launchPane.add(shutdownButton, BorderLayout.CENTER);
        launchPane.add(hostNameLabel, BorderLayout.SOUTH);

        awaitingConnFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownServer();
            }
        });
        awaitingConnFrame.add(launchPane);
        awaitingConnFrame.pack();
        awaitingConnFrame.setResizable(false);
        awaitingConnFrame.setVisible(true);

    }

    /**
     * Closes the server socket.
     */
    private void shutdownServer() {
        try {
            for (ConnectionThread c : clients) {
                c.closeStreams();
            }
            serverSocket.close();

            awaitingConnFrame.dispose();
            buttonFrame.dispose();

            System.out.println("shutdown compleyte");
            System.exit(0);
        }
        catch (IOException e) {
        }
    }

    /**
     * Finds the network name of this system.
     * 
     * @return the name of this machine as visible to other computers on
     *         network.
     */
    private String getMachineName() {
        String machName = "Cannot fetch System Name.";
        try {
            java.net.InetAddress info = java.net.InetAddress.getLocalHost();
            machName = info.getHostName();
        }
        catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: System Name cannot be fetched.");
            e.printStackTrace();
        }
        return machName;
    }

    /**
     * Set the current state of the button.
     * 
     * @param state the state to set.
     */
    public void setCurrentState(String state) {

        System.out.println("[SERVER] input received: " + state);

        currentState = state;
        currentState.toLowerCase();
        notifyClientsOfStateChange();
        respondToStateChange();
    }

    private void notifyClientsOfStateChange() {
        // TODO Auto-generated method stub
        for (ConnectionThread c : clients) {
            c.tellClientStateChanged(currentState);
        }

    }

    private void respondToStateChange() {
        switch (currentState) {
            case "visible":
                buttonFrame.setVisible(true);
                buttonFrame.requestFocusInWindow();
                updateFrame(buttonFrame);
                break;

            case "hidden":
                buttonFrame.setVisible(false);
                updateFrame(buttonFrame);
                break;

            case "kill":
                shutdownServer();
                break;

            default:
                break;
        }
    }

    public static void main(String[] args) throws IOException {
        new MainRedButtonServer();
    }

}
