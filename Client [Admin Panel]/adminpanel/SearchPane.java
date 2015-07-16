package adminpanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class SearchPane extends JPanel implements ActionListener {

    private JButton connectButton;
    private JTextField manualConnText;
    private JLabel statusLab;

    private String userInput = "";

    private JPanel buttonHolder;

    private final MainClientHub parentHub;

    public SearchPane(MainClientHub parentHub) {
        this.parentHub = parentHub;

        setLayout(new BorderLayout());

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        initComponents();
        addEventHandlers();
        addComponents();

        setVisible(true);
    }

    /**
     * Initialize all the components that will go on this panel.
     */
    private void initComponents() {

        buttonHolder = new JPanel(new BorderLayout());
        buttonHolder.setBorder(BorderFactory.createEmptyBorder(4, 40, 4, 40));

        connectButton = new JButton("CONNECT");
        manualConnText = new JTextField("Enter host name", 16);

        statusLab = new JLabel("Waiting for host name");
    }

    private void addEventHandlers() {
        connectButton.addActionListener(this);
        manualConnText.addActionListener(this);
    }

    private void addComponents() {
        buttonHolder.add(connectButton);

        add(manualConnText, BorderLayout.NORTH);
        add(statusLab, BorderLayout.SOUTH);
        add(buttonHolder, BorderLayout.CENTER);
    }

    public void setStatusLabText(String text) {
        statusLab.setText("<html>" + text + "</html>"); // html tags to wrap
                                                        // text.
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectButton || e.getSource() == manualConnText) {

            userInput = manualConnText.getText().trim();

            if (isCleanInput()) {
                statusLab.setText(
                        "Attemping connection to: " + manualConnText.getText());

                Thread queryThread = new Thread() {
                    @Override
                    public void run() {
                        parentHub.attemptConnection(userInput);
                    }
                };
                queryThread.start();
            }
        }

    }

    /**
     * Checks if input is sanitary.
     * 
     * @return true if the input is a valid string, false otherwise.
     */
    private boolean isCleanInput() {
        if (userInput.length() <= 0) {
            manualConnText.setText("Invalid Input");
            return false;
        }
        return true;
    }

}
