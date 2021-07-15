package plumtree.viewer;

import plumtree.viewer.layout.PlumtreeEdge;
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
                    if(l.contains("VIEWS:")) {
                        int startIndex = l.indexOf("VIS-") + 4;
                        String lineContent = l.substring(startIndex);
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

        int x = 100;
        int y = 100;
        Map<Host, PlumtreeVertex> vertices = new HashMap<>();
        for (int i = 0; i < listOfFiles.length; i++) {
            //results-10.0.0.5-5000.log
            String[] strs = listOfFiles[i].getName().split("-");
            Host node = new Host(InetAddress.getByName(strs[1]), Integer.parseInt(strs[2].split("\\.")[0]) + 1000);
            PlumtreeVertex v1 = new PlumtreeVertex(node, new Point(x, y));
            visualizer.addVertex(v1);
            vertices.put(node, v1);
             //COLUMNS OF 10 NODES
            int turn = i%10;
            if (turn < 9) {
                y += 100;
            } else if (turn == 9) {
                y = 100;
                x += 100;
            }
        }
        visualizer.getVertexMap(vertices);

//        visualizer.addEdge(new PlumtreeEdge(v1, v2, PlumtreeEdge.Type.EAGER));
//        visualizer.addEdge(new PlumtreeEdge(v2, v3, PlumtreeEdge.Type.EAGER));
//        visualizer.addEdge(new PlumtreeEdge(v1, v3, PlumtreeEdge.Type.LAZY));
//        visualizer.addEdge(new PlumtreeEdge(v2, v1, PlumtreeEdge.Type.LAZY));

    }

}
