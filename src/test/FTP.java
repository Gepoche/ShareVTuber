package test;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FTP extends JFrame {
    public static void main(String[] args) throws IOException {
        Frame();
    }

    public static String getSize(String Byte) {
        Long l1 = Long.parseUnsignedLong(Byte);
        double kb = l1 / 1024.0;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;
        double tb = gb / 1024.0;
        if (tb >= 1) {
            return (double) Math.round(tb * 10) / 10 + "TB";
        } else if (gb >= 1) {
            return (double) Math.round(gb * 10) / 10 + "GB";
        } else if (mb >= 1) {
            return (double) Math.round(mb * 10) / 10 + "MB";
        } else if (kb >= 1) {
            return (double) Math.round(kb * 10) / 10 + "KB";
        } else return Byte + "B";
    }

    public static String[] getList() throws IOException {

        FTPClient client = new FTPClient();
        client.setControlEncoding("UTF-8");

        FTPClientConfig config = new FTPClientConfig();

        config.setDefaultDateFormatStr("yyyy년 M월 d일");
        config.setRecentDateFormatStr("M월 d일 HH:mm");
        client.configure(config);

        client.connect("yomum.duckdns.org");
        client.login("anonymous", "anonymous");
        FTPFile[] files = client.listFiles("/득식체/");

        String[] Filelist = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            FTPFile file = files[i];
            Filelist[i] = file.getName();
        }

        client.logout();
        if (client.isConnected()) {
            client.disconnect();
        }
        return Filelist;
    }

    public static String getImage(String name) throws IOException{

        FTPClient client = new FTPClient();
        client.setControlEncoding("UTF-8");

        FTPClientConfig config = new FTPClientConfig();

        config.setDefaultDateFormatStr("yyyy년 M월 d일");
        config.setRecentDateFormatStr("M월 d일 HH:mm");
        client.configure(config);

        client.connect("localhost");
        client.login("anonymous", "anonymous");

        String ext = name.substring(name.lastIndexOf(".") + 1);
        File tmpFile = File.createTempFile("temp_", "." + ext);
        tmpFile.deleteOnExit();


        String ftp_path = "/득식체/" + name;
        String tmp_path = tmpFile.getAbsolutePath();
        File f = new File(tmp_path);
        System.out.println("다운로드 시작");
        OutputStream outputStream = new FileOutputStream(f);
        Boolean result = client.retrieveFile(ftp_path, outputStream);
        System.out.println(name + " " + getSize(String.valueOf(f.length())));
        System.out.println(tmp_path);
        outputStream.close();
        if(result) {
            System.out.println("다운로드 성공");
        }else   {
            System.err.println("다운로드 실패");
        }
        System.out.println();
        client.logout();
        if (client.isConnected()) {
            client.disconnect();
        }
        return tmp_path;
    }

    public static void Frame() throws IOException{
        JFrame frame = new JFrame();
        frame.setLocation(300, 200);
        frame.setPreferredSize(new Dimension(1000, 500));
        Container contentPane = frame.getContentPane();

        JList ls = new JList<>(getList());
        JPanel panel = new JPanel();
        JScrollPane js = new JScrollPane(ls);
        js.setLocation(10, 10);
        contentPane.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 40));


        ImageIcon icon = new ImageIcon("D:\\chano\\바탕화면\\득식체\\ba2d2758aff30411.png");
        Image im = icon.getImage();
        ImageIcon icon2 = new ImageIcon(im);
        JLabel img = new JLabel(icon2);

        frame.add(img);
        frame.setTitle("Test");
        frame.setLayout(new FlowLayout());
        frame.setSize(500,300);
        frame.setVisible(true);
        frame.pack();
        panel.add(js, "West");
        contentPane.add(panel);

        ls.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    try {
                        String path = getImage((String) ls.getSelectedValuesList().get(0));
                        img.setIcon(new ImageIcon(path));

                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
    }

}




