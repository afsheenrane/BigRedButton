package redbutton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

public class ConnectionThread implements Runnable, Observer {

    // I/O streams
    private BufferedReader in;
    private PrintWriter out;
    private final Socket socket;
    private boolean connected = false;

    private final MainRedButtonServer parentFrame;

    public ConnectionThread(Socket socket, MainRedButtonServer parentFrame) {
        this.socket = socket;
        this.parentFrame = parentFrame;
    }

    @Override
    public void run() {

        try {
            if (!initializeIOStreams())
                System.exit(1);

            connected = true;

            String input = " ";

            while ((input = in.readLine()) != null) {
                input.toLowerCase();
                if (input.equals("showbutton")) {
                    parentFrame.setCurrentState("visible");
                }
                if (input.equals("hidebutton")) {
                    parentFrame.setCurrentState("hidden");
                }
                if (input.equals("killserver")) {
                    parentFrame.setCurrentState("kill");
                }
            }
        }
        catch (IOException e) {

        }

    }

    /**
     * Open the input and output streams of this socket.
     */
    private boolean initializeIOStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        }
        catch (IOException e) {
            closeStreams();
            JOptionPane.showMessageDialog(null,
                    "[SERVER] Failed to open streams to client.",
                    "[SERVER] Stream failure", JOptionPane.ERROR_MESSAGE);
            return false;
        }

    }

    /**
     * Closes input and output streams, and the socket.
     */
    private void closeStreams() {
        try {
            in.close();
            out.close();
            socket.close();
        }
        catch (IOException e) {
        }
    }

    /**
     * @return whether the client on this socket has successfully connected.
     */
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void update(Observable o, Object arg) {
        if ((MainRedButtonServer) o == parentFrame) {
            out.println((String) arg);
        }
    }

}
