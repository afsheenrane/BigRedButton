package adminpanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AdminPane extends JPanel {

    private final MainClientFrame parentFrame;

    private JLabel statusLab;

    private JPanel topPane;
    private JPanel botPane;

    private JButton showBut;
    private JButton hideBut;
    private JButton killBut;

    public AdminPane(MainClientFrame mainClientFrame) {
        super();
        parentFrame = mainClientFrame;

        setLayout(new BorderLayout());

        initComponents();
        addHandlers();
        addComponents();
    }

    private void initComponents() {
        topPane = new JPanel();
        botPane = new JPanel();

        showBut = new JButton("SHOW RED BUTTON");
        hideBut = new JButton("HIDE RED BUTTON");
        killBut = new JButton("SHUTDOWN BUTTON");

        statusLab = new JLabel("Button is currently VISIBLE");
    }

    private void addHandlers() {
        ButtonHandler buttonHandler = new ButtonHandler();

        showBut.addActionListener(buttonHandler);
        hideBut.addActionListener(buttonHandler);
        killBut.addActionListener(buttonHandler);

    }

    private void addComponents() {

        topPane.add(showBut);
        topPane.add(hideBut);
        topPane.add(killBut);

        botPane.add(statusLab);

        add(topPane, BorderLayout.CENTER);
        add(botPane, BorderLayout.SOUTH);

    }

    private int checkKillButton() {
        return JOptionPane
                .showConfirmDialog(
                        null,
                        "Are you sure you want to close the Big Red Button?\n"
                                + "This will disconnect you and all other computers which are connected to it.",
                        "Kill Button Confirmation", JOptionPane.WARNING_MESSAGE);
    }

    public void updateStatusLabel(String status) {
        // TODO
        switch (status) {
            case "visible":
                statusLab.setText("Button is currently VISIBLE");
                break;

            case "hidden":
                statusLab.setText("Button is currently NOT VISIBLE");
                break;

            case "kill":
                statusLab.setText("Button has been shutdown");
                setServerDeadState();
                break;

            default:
                statusLab.setText("Anomalous state: " + status
                        + ". This should not happen");
                break;
        }
    }

    private void setServerDeadState() {
        showBut.setEnabled(false);
        hideBut.setEnabled(false);
        killBut.setEnabled(false);
    }

    private class ButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == showBut) {
                parentFrame.tellServer("showbutton");
            }
            else if (e.getSource() == hideBut) {
                parentFrame.tellServer("hidebutton");
            }
            else if (e.getSource() == killBut) {
                if (checkKillButton() == JOptionPane.OK_OPTION) {
                    parentFrame.tellServer("kill");
                }
            }
        }

    }
}
