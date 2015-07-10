package redbutton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class BigButtonPane extends JComponent {

    private final MainRedButtonServer mainServer;

    private static int xres;
    private static int yres;

    private static int d;
    private static int[] center = new int[2];
    private static boolean isRed = true;

    private Clip clip;

    // private final ImageIcon kiaPic = new ImageIcon(getClass().getResource(
    // "/client/kiaPic.png"));

    public BigButtonPane(MainRedButtonServer mainServer) {

        this.mainServer = mainServer;

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

    private void playBell() {
        try {
            InputStream is = getClass().getResourceAsStream(
                    "/client/client_bell.wav");
            InputStream bufferedIn = new BufferedInputStream(is);

            AudioInputStream audio = AudioSystem
                    .getAudioInputStream(bufferedIn);

            clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();

        }
        catch (UnsupportedAudioFileException uae) {
            JOptionPane.showMessageDialog(null, uae);
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, "Audio cannot be found");
        }
        catch (LineUnavailableException lua) {
            JOptionPane.showMessageDialog(null, lua);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (isRed)
            g2d.setColor(Color.RED);
        else
            g2d.setColor(Color.GREEN);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // g2d.drawImage(kiaPic.getImage(), 0, 0, this);

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
                isRed = false;
                repaint();
                mainServer.setCurrentState("ringbell");
                // playBell();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            isRed = true;
            repaint();
        }

    }

}
