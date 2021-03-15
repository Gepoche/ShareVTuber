import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import org.apache.commons.net.*;
import org.apache.commons.net.ftp.*;

import javax.imageio.ImageIO;
import javax.swing.*;

class fileThumbnail extends JPanel {
    Image image = null;
    URL url = null;
    public Image setImage(String fileurl) {
        try {
            url = new URL(fileurl);
            image = ImageIO.read(url);
        } catch (MalformedURLException ex) {
            System.out.println("Malformed URL");
        } catch (IOException iox) {
            System.out.println("Can not load file");
        }
        return image;
    }
}

class fileDialog extends JDialog {
    JList list = new JList();
    JButton bt1 = new JButton("CANCEL");
    JButton bt2 = new JButton("SELECT");
    JPanel p1 = new JPanel();
    JPanel p2 = new JPanel();
    JScrollPane js = new JScrollPane(list);
    JLabel label = new JLabel();
    ImageIcon image = new ImageIcon();
    fileThumbnail filethumbnail = new fileThumbnail();

    public fileDialog(Frame frame) {
        super(frame, "SELECT FROM NAS");
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setLayout(new GridLayout(1, 2));
        p1.setLayout(new BorderLayout());
        p2.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p2.add(bt1);
        p2.add(bt2);
        p1.add(label, BorderLayout.CENTER);
        p1.add(p2, BorderLayout.SOUTH);
        getContentPane().add(js);
        add(p1);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.width;
        double screenHeight = screenSize.height;
        int locationX = (int)(screenWidth/2);
        int locationY = (int)(screenHeight/2);
        setSize(600, 400);
        setLocation(locationX-300, locationY-200);
        setVisible(false);
        setResizable(false);
        setAlwaysOnTop(true);
    }

    public synchronized void imageChanger(Image image) throws NullPointerException {
        this.image.setImage(image);
        imageSizer(image);
        repaint();
        list.requestFocus();
    }

    public void imageSizer(Image image) {
        double originalWidth = image.getWidth(this);
        System.out.println(originalWidth);
        double originalHeight = image.getHeight(this);
        System.out.println(originalHeight);
        double resolution = originalWidth / originalHeight;

        Image img = this.image.getImage();

        Image changeImg;
        if(((originalHeight >= originalWidth && resolution > 0.9) || originalWidth > originalHeight)) {
            changeImg = img.getScaledInstance(300, (int) (300 / resolution), Image.SCALE_SMOOTH);
            this.image = new ImageIcon(changeImg);
            ImageIcon changeIcon = new ImageIcon(changeImg);
            label.setIcon(changeIcon);
            label.setSize(300, (int)(300 / resolution));
            label.setLocation(0, 165 - (label.getHeight() / 2));
        }
        else {
            changeImg = img.getScaledInstance((int) (330 * resolution), 330, Image.SCALE_SMOOTH);
            ImageIcon changeIcon = new ImageIcon(changeImg);
            label.setIcon(changeIcon);
            label.setSize((int)(330 * resolution), 330);
            label.setLocation(150-(label.getWidth() / 2), 0);
        }
        label.setHorizontalAlignment(JLabel.CENTER);
        System.out.println(label.getSize().toString());
        System.out.println(label.getLocation().toString());
    }
}

class FTPUploader {

    FTPClient ftp = null;
    FTPClientConfig config = null;

    //( host server ip, username, password )
    public FTPUploader(String host, String user, String pwd) throws Exception{
        ftp = new FTPClient();
        ftp.setControlEncoding("UTF-8");
        config = new FTPClientConfig();
        config.setDefaultDateFormatStr("yyyy년 M월 d일");
        config.setRecentDateFormatStr("M월 d일 HH:mm");
        ftp.configure(config);
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        ftp.connect(host);//호스트 연결
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(user, pwd);//로그인
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }
    //( 보낼파일경로+파일명, 호스트에서 받을 파일 이름, 호스트 디렉토리 )
    public void uploadFile(String localFileFullName, String fileName, String hostDir) throws Exception {
        try(InputStream input = new FileInputStream(new File(localFileFullName))){
            this.ftp.storeFile(hostDir + fileName, input);
            //storeFile() 메소드가 전송하는 메소드
        }
    }

    public String getListOfFiles() {
        String fileNames = "";
        try {
            FTPFile[] files = ftp.listFiles("/public/sharevtuber/");
            for (FTPFile file : files) {
                if (file.getName().startsWith("202")) {
                    fileNames = fileNames + file.getName() + "~";
                }
            }
        } catch (IOException ignored) { }
        return fileNames;
    }

    public void disconnect(){
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (SocketException ignored) { } catch (IOException f) {
                f.printStackTrace();
            }
        }
    }
}

class URLShortener {
    public String sendGet(String url) throws Exception {
        URL obj = new URL("https://api-ssl.bitly.com/v3/shorten?access_token=04d03c88b387939a770cdbbf7b586b1e6e9c872e&longUrl=" + url + "&format=txt");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //전송방식
        con.setRequestMethod("GET");
        //Request Header 정의
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
        con.setConnectTimeout(10000);       //컨텍션타임아웃 10초
        con.setReadTimeout(5000);           //컨텐츠조회 타임아웃 5총

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        Charset charset = Charset.forName("UTF-8");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),charset));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println("조회결과 : " + response.toString());
        return  response.toString();
    }
}

class MyFrame extends Frame implements WindowFocusListener, ActionListener {

    SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
    String format_time;
    Button bt1 = new Button("CREATE URL");
    Button bt2 = new Button("UPLOAD TO NAS");
    Button bt3 = new Button("UPLOAD");
    Button bt4 = new Button("...");
    TextField tf1 = new TextField("", 20);
    Checkbox cb1 = new Checkbox("Merge Twit", false);
    BorderLayout bl = new BorderLayout();
    BorderLayout bl2 = new BorderLayout();
    GridLayout gl = new GridLayout(1, 1);
    FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
    Panel p1 = new Panel();
    Panel p2 = new Panel();
    Panel p3 = new Panel();
    Panel p4 = new Panel();
    Panel p5 = new Panel();
    FTPUploader ftpuploader = null;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = clipboard.getContents(clipboard);
    String twitBuffer = new String();
    String FileLocation = new String();
    fileDialog filelist = new fileDialog(this);
    int lastIndex = 0;
    Image[] globalImage = new Image[255];
    Image[] empty = new Image[255];
    boolean isLoading = false;

    FileDialog uploader = new FileDialog(this, "UPLOAD", FileDialog.LOAD);
    FilenameFilter extension = (dir, name) -> name.endsWith(".png") || name.endsWith(".jpg")
            || name.endsWith(".gif") || name.endsWith(".jpeg") || name.endsWith(".PNG")
            || name.endsWith(".JPG") || name.endsWith(".GIF") || name.endsWith(".JPEG");

    public MyFrame(String title) {
        super(title);
        init();
        start();
        double screenWidth = screenSize.getWidth();
        double screenHeight = screenSize.getHeight();
        int locationX = (int)(screenWidth/2);
        int locationY = (int)(screenHeight/2);
        this.setSize(350, 200);
        this.setLocation(locationX-175,locationY-100);
        this.setVisible(true);
        this.setResizable(false);
        cb1.setEnabled(false);
        bt3.setEnabled(false);
    }

    public void init() {
        this.setLayout(bl);
        p2.setLayout(gl);
        p1.add(tf1);
        p1.add(cb1);
        p2.add(bt1);
        p3.add(bt2);
        p3.add(bt4);
        p3.add(bt3);
        this.add(p1, BorderLayout.NORTH);
        this.add(p2, BorderLayout.CENTER);
        this.add(p3, BorderLayout.SOUTH);
        p5.setLayout(fl);
        p4.setLayout(bl2);
        p4.add(p5, BorderLayout.SOUTH);

        tf1.setEditable(false);
        uploader.setFilenameFilter(extension);

    }
    public void start() {
        this.addWindowFocusListener(this);
        bt1.addActionListener(this);
        bt2.addActionListener(this);
        bt3.addActionListener(this);
        bt4.addActionListener(this);
        filelist.bt1.addActionListener(this);
        filelist.bt2.addActionListener(this);
        filelist.list.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) {
                Thread thread = new Thread(() -> {
                    try {
                        System.out.println(filelist.list.getSelectedIndex());
                        System.out.println(filelist.list.getSelectedValue().toString());
                        lastIndex = filelist.list.getSelectedIndex();
                        filelist.imageChanger(globalImage[filelist.list.getSelectedIndex()]);
                        Thread thread1 = new Thread(() -> {
                            String fileurl = "http://211.216.126.192:8800/sharevtuber/" + filelist.list.getSelectedValue().toString();
                            globalImage[filelist.list.getSelectedIndex()] = filelist.filethumbnail.setImage(fileurl);
                        });
                        thread1.setPriority(Thread.MAX_PRIORITY);
                        thread1.start();
                    } catch (NullPointerException ignored){ }
                    filelist.bt2.setEnabled(true);
                });
                thread.start();
            }
        });

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    public void getClipboard() {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        contents = clipboard.getContents(clipboard);
        if (contents != null) {
            try {
                String pasteString = (String)(contents.getTransferData(DataFlavor.stringFlavor));
                tf1.setText(pasteString);
            } catch (Exception ignored) {}
        }
    }

    public String urlMaker() {
        String sGot = tf1.getText();
        String result = new String();
        String buffer = new String();
        URLShortener urlShortener = new URLShortener();

        if(sGot.contains("bit.ly")) {
            return sGot;
        }
        else if(sGot.contains("sharevtuber.kro.kr")) {
            buffer = sGot;
        }
        else if(sGot.contains("https://youtu.be/")) {
            buffer = "http://sharevtuber.kro.kr:8652/?youtube=" + sGot.substring(17);
        }
        else if(sGot.contains("https://www.youtube.com/watch?v=")) {
            if(sGot.contains("&")) {
                buffer = "http://sharevtuber.kro.kr:8652/?youtube=" + sGot.substring(32, sGot.indexOf('&'));
            }
            else {
                buffer = "http://sharevtuber.kro.kr:8652/?youtube=" + sGot.substring(32);
            }
        }
        else if(sGot.contains("https://pbs.twimg.com/media/")) {
            if(!cb1.getState()) {
                try {
                    buffer = "http://sharevtuber.kro.kr:8652/?twitterimg=" + sGot.substring(28, sGot.indexOf('?'));
                } catch (StringIndexOutOfBoundsException exception) {
                    buffer = "http://sharevtuber.kro.kr:8652/?twitterimg=" + sGot.substring(28);
                }
                cb1.setEnabled(false);
                twitBuffer = "";
            }
            else if(cb1.getState()) {
                if(!twitBuffer.equals("")) {
                    try {
                        buffer = "http://sharevtuber.kro.kr:8652/?twitterimg=" + twitBuffer.substring(28, twitBuffer.indexOf('?')) + "?twitterimg1=" + sGot.substring(28, sGot.indexOf('?'));
                    } catch (StringIndexOutOfBoundsException exception1) {
                        try {
                            buffer = "http://sharevtuber.kro.kr:8652/?twitterimg=" + twitBuffer.substring(28, twitBuffer.indexOf('?')) + "?twitterimg1=" + sGot.substring(28);
                        } catch (StringIndexOutOfBoundsException exception2) {
                            try {
                                buffer = "http://sharevtuber.kro.kr:8652/?twitterimg=" + twitBuffer.substring(28) + "?twitterimg1=" + sGot.substring(28, twitBuffer.indexOf('?'));
                            } catch (StringIndexOutOfBoundsException exception3) {
                                buffer = "http://sharevtuber.kro.kr:8652/?twitterimg=" + twitBuffer.substring(28) + "?twitterimg1=" + sGot.substring(28);
                            }
                        }
                    }
                }
                else {
                    try {
                        buffer = "http://sharevtuber.kro.kr:8652/?twitterimg=" + sGot.substring(28, sGot.indexOf('?'));
                    } catch (StringIndexOutOfBoundsException exception) {
                        buffer = "http://sharevtuber.kro.kr:8652/?twitterimg=" + sGot.substring(28);
                    }
                }
                twitBuffer = "";
                cb1.setState(false);
                cb1.setEnabled(false);
            }
        }
        else if(sGot.contains(".jpg") || sGot.contains(".gif") || sGot.contains(".png") || sGot.contains(".jpeg")) {
            buffer = "http://sharevtuber.kro.kr:8652/?nas=" + sGot;
        }
        else if(sGot.contains("https://twitter.com/") && sGot.contains("status")) {
            buffer = "http://sharevtuber.kro.kr:8652/?twit=" + sGot.substring(20);
        }
        else if(sGot.contains("https://www.pixiv.net/artworks/")) {
            buffer = "http://sharevtuber.kro.kr:8652/?pixiv=" + sGot.substring(31);
        }
        else {
            return "";
        }

        try {
            result = urlShortener.sendGet(buffer);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return result;
    }

    public void actionPerformed(ActionEvent e) {
        String result;
        File f;
        if (e.getSource() == bt1) {
            result = urlMaker();
            StringSelection contents = new StringSelection(result);
            clipboard.setContents(contents, null);
            tf1.setText(result);
        }
        if (e.getSource() == bt2) {
            uploader.setVisible(true);
            try {
                f = new File(uploader.getFile());
                FileLocation = uploader.getDirectory() + f.getName();
                bt2.setLabel(f.getName());
                if (!bt2.getLabel().equals("UPLOAD TO NAS")) {
                    bt3.setEnabled(true);
                    System.out.println(FileLocation);
                }
            } catch (NullPointerException ignored) { }
        }
        if (e.getSource() == bt3) {
            try {
                ftpuploader = new FTPUploader("211.216.126.192", "Anonymous1", "Anonymous1");
                format_time = format1.format(System.currentTimeMillis());
                f = new File(uploader.getFile());
                String FileName;
                FileName = format_time + f.getName().substring(f.getName().lastIndexOf('.'));
                FileName = FileName.toLowerCase();
                System.out.println(FileName);
                try {
                    ftpuploader.uploadFile(FileLocation, FileName, "/public/sharevtuber/");
                } catch (Exception ignored) {
                }
                System.out.println("SENT");
                bt3.setEnabled(false);
                bt2.setLabel("UPLOAD TO NAS");
                tf1.setText(FileName);
                ftpuploader.disconnect();
            } catch (Exception ignored) { }
        }
        if (e.getSource() == bt4) {
            try {
                ftpuploader = new FTPUploader("211.216.126.192", "Anonymous1", "Anonymous1");
                String[] files = ftpuploader.getListOfFiles().split("~");
                for (int i = 0; i < files.length; i++) {
                    globalImage[i] = filelist.filethumbnail.setImage("http://211.216.126.192:8800/sharevtuber/empty.png");
                }
                DefaultListModel listModel = new DefaultListModel();
                for (String name : files) {
                    listModel.addElement(name);
                }
                filelist.list.setModel(listModel);
                filelist.setVisible(true);
                filelist.list.setSelectedIndex(lastIndex);
                filelist.bt2.setEnabled(false);
                Thread thread = new Thread(() -> {
                    isLoading = true;
                    for (int i = 0; i < files.length; i++) {
                        globalImage[i] = filelist.filethumbnail.setImage("http://211.216.126.192:8800/sharevtuber/" + files[i]);
                        try {
                            filelist.imageChanger(globalImage[filelist.list.getSelectedIndex()]);
                        } catch (ArrayIndexOutOfBoundsException exception) {
                            filelist.list.setSelectedIndex(files.length-1);
                            filelist.imageChanger(globalImage[files.length-1]);
                        }
                    }
                    isLoading = false;
                    ftpuploader.disconnect();
                    if(!filelist.isVisible()) {
                        globalImage = empty;
                        System.out.println("Image Cleared");
                    }
                });
                thread.start();
            } catch (Exception ignored) { }
        }
        if (e.getSource() == filelist.bt2) {
            StringSelection contents = new StringSelection(filelist.list.getSelectedValue().toString());
            clipboard.setContents(contents, null);
            System.out.println(filelist.list.getSelectedValue().toString());
            filelist.dispose();
            if(!isLoading) globalImage = empty;
            System.out.println("Dialog Closed");
        }
        if (e.getSource() == filelist.bt1) {
            filelist.dispose();
            if(!isLoading) globalImage = empty;
            System.out.println("Dialog Closed");
        }
    }
    public void windowGainedFocus(WindowEvent e) {
        getClipboard();
        if(tf1.getText().contains("https://pbs.twimg.com/media/")) {
            if(twitBuffer.contains("twimg.com") && cb1.getState()) {
                cb1.setEnabled(false);
            }
            else {
                cb1.setEnabled(true);
                twitBuffer = tf1.getText();
                System.out.println(twitBuffer);
            }
        }
        else {
            cb1.setEnabled(false);
            cb1.setState(false);
            twitBuffer = "";
        }
    }
    public void windowLostFocus(WindowEvent e) {
        tf1.setText("");
    }
}

public class Sharevtuber {
    public static void main(String[] args) {
        String OS = System.getProperty("os.name").toLowerCase();
        System.out.println(OS);
        try{
            MyFrame myframe = new MyFrame("공유용");
            if(OS.indexOf("mac") >= 0) {
                com.apple.eawt.Application macApp = com.apple.eawt.Application.getApplication();
                macApp.setDockIconImage(new ImageIcon(Sharevtuber.class.getResource("icon.png")).getImage());
            }
            else if(OS.indexOf("win") >= 0) {
                URL imageURL = Sharevtuber.class.getClassLoader().getResource("icon.png");
                ImageIcon img = new ImageIcon(imageURL);
                myframe.setIconImage(img.getImage());
            }
        } catch(Exception ignored){ }
    }
}