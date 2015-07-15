package redbutton;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainRedButtonServer {

    private JFrame awaitingConnFrame;
    private JFrame buttonFrame;

    private ServerSocket serverSocket = null;
    private int PORT;
    private String currentState = "visible";
    private boolean buttonEnabled = false;

    private ArrayList<ConnectionThread> clientObjs;
    private ArrayList<Thread> clientThreads;

    private final String propPath = "./button_resources/server_info.properties";

    public MainRedButtonServer() throws IOException {
        super();

        assignPort();
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

        clientThreads = new ArrayList<Thread>(5);
        clientObjs = new ArrayList<ConnectionThread>(5);

        while (true) {
            ConnectionThread ct;

            ct = new ConnectionThread(serverSocket.accept(), this);

            Thread t = new Thread(ct);
            t.start();

            if (clientThreads.add(t) && clientObjs.add(ct)) {
                System.out.println("[SERVER] Client num: " + clientObjs.size());

                if (!buttonEnabled) {
                    showBigRedButton();
                    buttonEnabled = true;
                }
            }
        }
    }

    private void assignPort() {
        Properties prop = new Properties();

        InputStream in;
        try {
            in = new FileInputStream(propPath);
            prop.load(in);
            PORT = Integer.parseInt(prop.getProperty("PORT"));
        }
        catch (IOException | NumberFormatException e) {
            PORT = 2222;
            JOptionPane.showMessageDialog(
                    null,
                    "Error reading from " + propPath
                            + "\nSetting default PORT = 2222\nERROR: "
                            + e.getMessage());
        }

    }

    private boolean getAlwaysOnTop() {
        Properties prop = new Properties();

        InputStream in;
        try {
            in = new FileInputStream(propPath);
            prop.load(in);
            return Boolean.parseBoolean(prop.getProperty("AlwaysOnTop"));
        }
        catch (IOException e) {
            return false;
        }
    }

    private void showBigRedButton() {
        awaitingConnFrame.dispose();

        buttonFrame = new JFrame();

        buttonFrame.setFocusable(true);
        buttonFrame.setLocation(1, 1);
        buttonFrame.setUndecorated(true);

        JComponent buttonComp = new BigButtonPane(this);

        buttonFrame.add(buttonComp);

        buttonFrame.pack();
        buttonFrame.setResizable(false);

        buttonFrame.requestFocusInWindow();
        buttonFrame.setAlwaysOnTop(getAlwaysOnTop());

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

        JPanel launchPane = new JPanel(new BorderLayout());

        awaitingConnFrame.setLocation(400, 400);

        shutdownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shutdownServer();
            }
        });

        launchPane.add(statusLabel, BorderLayout.NORTH);
        launchPane.add(shutdownButton, BorderLayout.CENTER);
        launchPane.add(hostNameLabel, BorderLayout.SOUTH);

        launchPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

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
            for (ConnectionThread c : clientObjs) {
                c.closeStreams();
            }
            serverSocket.close();

            awaitingConnFrame.dispose();

            if (buttonFrame != null)
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
        notifyClientsOfCurrentState();
        respondToStateChange();
    }

    public void notifyClientsOfCurrentState() {
        System.out.println("[SERVER] num threads: " + clientThreads.size());
        for (ConnectionThread c : clientObjs) {
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

    public void removeDisconnectedClient() {
        for (int i = 0; i < clientObjs.size(); i++) {
            if (!clientObjs.get(i).isConnected()) {
                clientObjs.remove(i);
                clientThreads.remove(i);
            }
        }

        // Kills the button if no more clients connected.
        if (clientObjs.size() == 0) {
            shutdownServer();
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            new MainRedButtonServer();
        }
        catch (IOException e) {
        }
    }

}
