package redbutton;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MainRedButtonServer extends Observable {

    private JFrame mainFrame;
    private JPanel launchPane;
    private ServerSocket serverSocket = null;
    private final int PORT = 2222;
    private String currentState = "";
    private boolean buttonEnabled = false;

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

        ArrayList<ConnectionThread> clients = new ArrayList<ConnectionThread>();

        while (true) {
            clients.add(new ConnectionThread(serverSocket.accept(), this));
            clients.get(clients.size() - 1).run();
            addObserver(clients.get(clients.size() - 1));

            if (!buttonEnabled) {
                showBigRedButton();
                buttonEnabled = true;
            }
        }

    }

    private void showBigRedButton() {
        mainFrame.remove(launchPane);

        mainFrame.setLocation(1, 1);

        JComponent buttonComp = new BigButtonPane(this);
        mainFrame.add(buttonComp);
        updateFrame();
        mainFrame.setVisible(true);

    }

    private void updateFrame() {
        mainFrame.pack();
        mainFrame.validate();
        mainFrame.repaint();
    }

    /**
     * Create the window that says the server is waiting for clients to connect.
     */
    private void createAwaitingConnWindow() {

        JButton shutdownButton = new JButton("SHUTDOWN SERVER");
        JLabel hostNameLabel = new JLabel("Computer Name: " + getMachineName());
        JLabel statusLabel = new JLabel("Awaiting connections on port " + PORT);

        mainFrame = new JFrame("Button Server");
        launchPane = new JPanel(new BorderLayout());

        mainFrame.setLocation(400, 400);

        shutdownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shutdownServer();
            }
        });

        launchPane.add(statusLabel, BorderLayout.NORTH);
        launchPane.add(shutdownButton, BorderLayout.CENTER);
        launchPane.add(hostNameLabel, BorderLayout.SOUTH);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownServer();
            }
        });
        mainFrame.add(launchPane);
        mainFrame.pack();
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);
        System.out.println("is visible!");

    }

    /**
     * Closes the server socket.
     */
    private void shutdownServer() {
        try {
            serverSocket.close();
            mainFrame.dispose();
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
        currentState = state;
        currentState.toLowerCase();
        setChanged();
        notifyObservers(currentState);
        respondToStateChange();
    }

    /**
     * Changes the state of the button depending on the input received from the
     * clients.
     */
    private void respondToStateChange() {
        switch (currentState) {
            case "visible":
                mainFrame.setVisible(true);
                mainFrame.requestFocusInWindow();
                updateFrame();
                break;

            case "hidden":
                mainFrame.setVisible(false);
                updateFrame();
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
