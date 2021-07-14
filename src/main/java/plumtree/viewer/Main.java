package plumtree.viewer;

import plumtree.viewer.layout.PlumtreeEdge;
import plumtree.viewer.layout.PlumtreeVertex;
import network.data.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class Main {

    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {


        ViewerWindow visualizer = new ViewerWindow();
        JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        content.add(visualizer);
        frame.pack();
        frame.setVisible(true);
        PlumtreeVertex v1 = new PlumtreeVertex(new Host(InetAddress.getByName("10.10.0.20"), 20000), new Point(100, 100));
        PlumtreeVertex v2 = new PlumtreeVertex(new Host(InetAddress.getByName("10.10.0.30"), 30000), new Point(200, 100));
        PlumtreeVertex v3 = new PlumtreeVertex(new Host(InetAddress.getByName("10.10.0.40"), 40000), new Point(100, 200));
        visualizer.addVertex(v1);
        visualizer.addVertex(v2);
        visualizer.addVertex(v3);
        visualizer.addEdge(new PlumtreeEdge(v1, v2, PlumtreeEdge.Type.EAGER));
        visualizer.addEdge(new PlumtreeEdge(v2, v3, PlumtreeEdge.Type.EAGER));
        visualizer.addEdge(new PlumtreeEdge(v1, v3, PlumtreeEdge.Type.LAZY));
        visualizer.addEdge(new PlumtreeEdge(v2, v1, PlumtreeEdge.Type.LAZY));

    }


}
