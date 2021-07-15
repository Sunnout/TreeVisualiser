package plumtree.viewer;

import plumtree.viewer.layout.PlumtreeVertex;
import plumtree.viewer.utils.Host;
import plumtree.viewer.utils.Line;
import plumtree.viewer.utils.LineByTimestamp;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Main {

    public static final String FOLDER_PATH = "/tmp/plumtreelogs";

    public static void main(String[] args) throws Exception {

        //LOGS
        List<Line> logs = new ArrayList<>();

        File folder = new File(FOLDER_PATH);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            //results-10.0.0.5-5000.log
            String[] strs = listOfFiles[i].getName().split("-");
            Host node = new Host(InetAddress.getByName(strs[1]), Integer.parseInt(strs[2].split("\\.")[0]) + 1000);

            File file = listOfFiles[i].getAbsoluteFile();

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String l;
                while ((l = br.readLine()) != null) {
                    String lineContent = "";
                    if(l.contains("VIEWS:")) {
                        int startIndex = l.indexOf("VIS-") + 4;
                        lineContent = l.substring(startIndex);
                        String[] parts = l.split(" ");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss,SSS");
                        Date parsedDate = dateFormat.parse(parts[1]);
                        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                        Line line = new Line(node, timestamp, lineContent);
                        logs.add(line);
                    } else if(l.contains("Hello, I am")) {
                        lineContent = l.split(" ", 4)[3];
                        System.out.println(lineContent);
                        String[] parts = l.split(" ");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss,SSS");
                        Date parsedDate = dateFormat.parse(parts[1]);
                        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                        Line line = new Line(node, timestamp, lineContent);
                        logs.add(line);
                    } else if(l.contains("Goodbye")) {
                        lineContent = l.split(" ", 4)[3];
                        System.out.println(lineContent);
                        String[] parts = l.split(" ");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss,SSS");
                        Date parsedDate = dateFormat.parse(parts[1]);
                        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                        Line line = new Line(node, timestamp, lineContent);
                        logs.add(line);
                    }
                }
            }
        }

        Collections.sort(logs, new LineByTimestamp());

        //GRAPH STUFF
        ViewerWindow visualizer = new ViewerWindow(logs);
        JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        content.add(visualizer);
        frame.pack();
        frame.setVisible(true);
    }

}
