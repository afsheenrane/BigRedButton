package redbutton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

@SuppressWarnings("serial")
public class BigButtonPane extends JComponent {

    private final MainRedButtonServer mainServer;

    private final int xres;
    private final int yres;

    private boolean isRed = true;

    private final int d;
    private final int[] center = new int[2];

    private ImageIcon bgPic;

    public BigButtonPane(MainRedButtonServer mainServer) {

        this.mainServer = mainServer;

        loadPicture();

        xres = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        yres = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        setPreferredSize(new Dimension(xres, yres));
        setSize(xres, yres);

        center[0] = xres / 2;
        center[1] = 750;
        d = (int) (xres * 0.25);

        addMouseListener(new Handler());
        setFocusable(true);
    }

    private void loadPicture() {
        bgPic = new ImageIcon("./resources/background.png");
    }

    private void playBell() {
        try {
            InputStream in = new FileInputStream("./resources/button_bell.wav");

            AudioStream audioStream = new AudioStream(in);

            AudioPlayer.player.start(audioStream);
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, "Audio cannot be found");
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(bgPic.getImage(), 0, 0, this);

        if (isRed)
            g2d.setColor(Color.RED);
        else
            g2d.setColor(Color.GREEN);

        // Draw the button
        g2d.fillOval(center[0] - (d / 2), center[1] - (d / 2), d, d);

        g2d.setColor(Color.BLACK);
        g2d.drawOval(center[0] - (d / 2) - 1, center[1] - (d / 2) - 1, d, d);

        g2d.dispose();
    }

    private class Handler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            int[] point = new int[2];
            point[0] = e.getX();
            point[1] = e.getY();

            double dist = Math.sqrt(Math.pow((point[0] - center[0]), 2)
                    + Math.pow(point[1] - center[1], 2));

            // distance formula for hit detection
            if (dist <= d / 2) {
                Timer makeGreen = new Timer();

                makeGreen.schedule(new ButtonClicked(), 500);

                isRed = false;
                repaint();

                mainServer.setCurrentState("ringbell");
                playBell();
            }
        }
    }

    private class ButtonClicked extends TimerTask {

        @Override
        public void run() {
            isRed = true;
            repaint();
        }

    }

}
