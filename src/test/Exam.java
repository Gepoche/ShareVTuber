package test;

import java.awt.BorderLayout;
import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Exam extends JFrame {

    public Exam() {
        super("label Test");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        Image image = null;
        URL url = null;
        try {
            url = new URL("http://211.216.126.192:8800/sharevtuber/20201128204225.jpeg");
            image = ImageIO.read(url);
        } catch (MalformedURLException ex) {
            System.out.println("Malformed URL");
        } catch (IOException iox) {
            System.out.println("Can not load file");
        }
        JLabel label = new JLabel(new ImageIcon(image));
        panel.add(label, BorderLayout.CENTER);

        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);

    }
    public static void main(String[] args) {
        Exam d = new Exam();
    }
}

