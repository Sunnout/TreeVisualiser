package plumtree.viewer;

import com.google.common.base.Functions;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest2;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.DirectionalEdgeArrowTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;
import plumtree.viewer.decorators.EdgePainter;
import plumtree.viewer.decorators.EdgeShaper;
import plumtree.viewer.decorators.VertexLabeler;
import plumtree.viewer.decorators.VertexPainter;
import plumtree.viewer.layout.PlumtreeEdge;
import plumtree.viewer.layout.PlumtreeVertex;
import plumtree.viewer.utils.Host;
import plumtree.viewer.utils.Line;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

public class ViewerWindow extends JApplet {

    private static final String TIME_FORMAT = "%02d:%02d:%02d:%03d";

    Graph<PlumtreeVertex, PlumtreeEdge> graph;
    VisualizationViewer<PlumtreeVertex, PlumtreeEdge> vv;
    EdgeShaper transformerAll;

    Layout<PlumtreeVertex, PlumtreeEdge> currentLayout;
    Map<Host, PlumtreeVertex> vertices;
    List<Line> logs; // List of lines that have relevant information
    int currLine;
    long startTimeMillis; // Start time of experiment in milliseconds

    JLabel sliderLabelCurrentTime;
    JLabel sliderLabelCurrentLine;
    JSlider timeSlider;

    boolean internalChanging = false;

    public ViewerWindow(List<Line> logs) {
        this.logs = logs;
        this.vertices = new HashMap<>();
        this.currLine = -1;
        this.startTimeMillis = logs.get(0).getTs().getTime();
        int endTimeMillis = (int)(logs.get(logs.size()-1).getTs().getTime() - startTimeMillis); // End time of experiment in milliseconds

        // MININUM SPANNING TREE
        graph = new DirectedSparseMultigraph<>();
        Forest<PlumtreeVertex, PlumtreeEdge> tree = new MinimumSpanningForest2<>(graph,
                new DelegateForest<>(), DelegateTree.getFactory(),
                Functions.constant(1.0)).getForest();
        TreeLayout<PlumtreeVertex, PlumtreeEdge> layout = new TreeLayout<>(tree);
        currentLayout = new StaticLayout<>(graph, layout);
        vv = new VisualizationViewer<>(currentLayout, new Dimension(850, 850));
        vv.setBackground(Color.white);

        // EDGES
        transformerAll = new EdgeShaper(graph, vv.getPickedVertexState());
        vv.getRenderContext().setEdgeShapeTransformer(transformerAll);
        vv.getRenderContext().setEdgeArrowTransformer(new DirectionalEdgeArrowTransformer<>(10, 8, 4));
        EdgePainter arrowPainter = new EdgePainter(graph, vv.getPickedVertexState());
        vv.getRenderContext().setEdgeDrawPaintTransformer(arrowPainter);
        vv.getRenderContext().setArrowDrawPaintTransformer(arrowPainter);
        vv.getRenderContext().setArrowFillPaintTransformer(arrowPainter);

        // VERTEXES
        vv.getRenderContext().setVertexLabelTransformer(new VertexLabeler());
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.BLUE));
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
        vv.getRenderContext().setVertexFillPaintTransformer(new VertexPainter(graph, vv.getPickedVertexState()));
        vv.setVertexToolTipTransformer(new ToStringLabeller());

        // GUI
        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();
        vv.setGraphMouse(graphMouse);

        //********** BOTTOM BOX **********//

        // MODE
        JComboBox<ModalGraphMouse.Mode> modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

        // ZOOM
        final ScalingControl scaler = new CrossoverScalingControl();
        JButton plus = new JButton("+");
        plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
        JButton minus = new JButton("-");
        minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));
        JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

        // STEPS
        JButton prev = new JButton("<");
        prev.addActionListener(e -> jumpToLine(currLine - 1, true));
        JButton prev10 = new JButton("<<");
        prev10.addActionListener(e -> jumpToLine(currLine - 10, true));
        JButton prev100 = new JButton("<<<");
        prev100.addActionListener(e -> jumpToLine(currLine - 100, true));
        JButton next = new JButton(">");
        next.addActionListener(e -> jumpToLine(currLine + 1, true));
        JButton next10 = new JButton(">>");
        next10.addActionListener(e -> jumpToLine(currLine + 10, true));
        JButton next100 = new JButton(">>>");
        next100.addActionListener(e -> jumpToLine(currLine + 100, true));
        JPanel stepGrid = new JPanel(new GridLayout(1, 0));
        stepGrid.setBorder(BorderFactory.createTitledBorder("Steps (1, 10 or 100)"));

        //CONTROLS
        JPanel controls = new JPanel();
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        stepGrid.add(prev100);
        stepGrid.add(prev10);
        stepGrid.add(prev);
        stepGrid.add(next);
        stepGrid.add(next10);
        stepGrid.add(next100);
        controls.add(scaleGrid);
        controls.add(stepGrid);
        controls.add(modeBox);
        content.add(controls, BorderLayout.SOUTH);

        //********** TOP BOX  **********//

        // SLIDER
        JPanel sliderPanel = new JPanel(new BorderLayout(10,3));
        sliderPanel.setBorder(new EmptyBorder(2, 10, 0, 10));

        sliderLabelCurrentTime = new JLabel("Time");
        sliderLabelCurrentLine = new JLabel("-1");
        Timestamp firstTS = logs.get(0).getTs();
        JLabel sliderLabelMin = new JLabel(String.format(TIME_FORMAT,
                firstTS.getHours(), firstTS.getMinutes(), firstTS.getSeconds(), firstTS.getTime()%1000));
        Timestamp lastTS = logs.get(logs.size()-1).getTs();
        JLabel sliderLabelMax = new JLabel(String.format(TIME_FORMAT,
                lastTS.getHours(), lastTS.getMinutes(), lastTS.getSeconds(), lastTS.getTime()%1000));

        timeSlider = new JSlider(JSlider.HORIZONTAL, 0, endTimeMillis, 0);
        timeSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            long newTs = source.getValue() + startTimeMillis;
            Timestamp timestamp = new Timestamp(newTs);
            sliderLabelCurrentTime.setText(String.format(TIME_FORMAT,
                    timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds(), timestamp.getTime()%1000));

            if (!internalChanging && !source.getValueIsAdjusting())
                jumpToTime(source.getValue());

        });
        timeSlider.setMajorTickSpacing(60000);
        timeSlider.setMinorTickSpacing(10000);
        timeSlider.setPaintTicks(true);

        sliderPanel.add(timeSlider);
        sliderPanel.add(sliderLabelMin, BorderLayout.WEST);
        sliderPanel.add(sliderLabelMax, BorderLayout.EAST);
        JPanel timePanel = new JPanel();
        timePanel.add(sliderLabelCurrentTime);
        timePanel.add(sliderLabelCurrentLine);
        sliderPanel.add(timePanel, BorderLayout.SOUTH);
        content.add(sliderPanel, BorderLayout.NORTH);

        // EVENT MARKERS
        Hashtable<Integer, JLabel> markers = new Hashtable<>();
        JLabel eventLabelHello = new JLabel("|");
        eventLabelHello.setForeground(Color.GREEN.darker());
        eventLabelHello.setFont(new Font("Arial", Font.PLAIN, 16));
        JLabel eventLabelGoodbye = new JLabel("|");
        eventLabelGoodbye.setForeground(Color.RED.darker());
        eventLabelGoodbye.setFont(new Font("Arial", Font.PLAIN, 16));
        JLabel eventLabel = new JLabel("|");
        eventLabel.setForeground(new Color(0.5f,0.5f,0.5f,0.2f));
        eventLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        logs.forEach(l -> {
            if (l.getHello() != null) {
                markers.put((int) (l.getTs().getTime() - logs.get(0).getTs().getTime()), eventLabelHello);
            } else if (l.getGoodbye() != null) {
                markers.put((int) (l.getTs().getTime() - logs.get(0).getTs().getTime()), eventLabelGoodbye);
            } else {
                markers.put((int) (l.getTs().getTime() - logs.get(0).getTs().getTime()), eventLabel);
            }
        });
        timeSlider.setLabelTable(markers);
        timeSlider.setPaintLabels(true);
    }

    private void jumpToTime(int ts) {
        System.out.println("jumpToTime " + ts);
        long targetTime = ts + startTimeMillis;
        int target = -1;
        while(target < (logs.size() - 1) && logs.get(target+1).getTs().getTime() <= targetTime){
            target++;
        }
        jumpToLine(target, false);
    }

    private void jumpToLine(int targetLine, boolean updateSlider) {
        if (targetLine >= logs.size() || targetLine < 0) {
            System.out.println("Cannot jump to line " + targetLine);
            return;
        }
        if(targetLine == currLine)
            return;

        if(targetLine < currLine) {
            resetGraph();
            currLine = -1;
        }
        System.out.println("Moving " + (targetLine - currLine) + " lines");
        for(int i = currLine+1;i<=targetLine;i++){
            processLine(logs.get(i));
        }
        currLine = targetLine;
        sliderLabelCurrentLine.setText(currLine + "/" + (logs.size()-1));
        if (updateSlider) {
            internalChanging = true;
            timeSlider.setValue((int) (logs.get(currLine).getTs().getTime() - startTimeMillis));
            internalChanging = false;
        }
        redraw();
    }

    private void resetGraph(){
        Iterator<Map.Entry<Host, PlumtreeVertex>> iterator = vertices.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Host, PlumtreeVertex> next = iterator.next();
            removeVertex(next.getValue());
            iterator.remove();
        }
        System.out.println("Reset");
    }

    private void processLine(Line l){
        System.out.println(l.getTs() + " Processing -> NODE: " + l.getNode() + " " + l.getContent());

        if (l.getHello() != null) {
            PlumtreeVertex v1 = new PlumtreeVertex(l.getHello());
            addVertex(v1);
            vertices.put(l.getHello(), v1);
        } else if (l.getGoodbye() != null) {
            PlumtreeVertex v1 = new PlumtreeVertex(l.getGoodbye());
            removeVertex(v1);
        } else {
            for (Map.Entry<String, Host> toAdd : l.getToAdd().entrySet()) {
                switch (toAdd.getKey()) {
                    case "eager":
                        addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.EAGER));
                        break;
                    case "lazy":
                        addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.LAZY));
                        break;
                    case "pendingIncomingSyncs":
                        addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.PENDING_INCOMING_SYNCS));
                        break;
                    case "incomingSync":
                        addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.INCOMING_SYNC));
                        break;
                    default:
                        break;
                }
            }

            for (Map.Entry<String, Host> toRemove : l.getToRemove().entrySet()) {
                switch (toRemove.getKey()) {
                    case "eager":
                        removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.EAGER));
                        break;
                    case "lazy":
                        removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.LAZY));
                        break;
                    case "pendingIncomingSyncs":
                        removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.PENDING_INCOMING_SYNCS));
                        break;
                    case "incomingSync":
                        removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.INCOMING_SYNC));
                        break;
                    default:
                        break;
                }
            }
        }
    }


    private void addEdge(PlumtreeEdge e) {
        if (!graph.containsVertex(e.getOrigin())) graph.addVertex(e.getOrigin());
        if (!graph.containsVertex(e.getDestiny())) graph.addVertex(e.getDestiny());
        graph.addEdge(e, e.getOrigin(), e.getDestiny());
    }

    private void removeEdge(PlumtreeEdge e) {
        graph.removeEdge(e);
    }

    private void addVertex(PlumtreeVertex v) {
        graph.addVertex(v);
    }

    private void removeVertex(PlumtreeVertex v) {
        graph.removeVertex(v);
    }

    private void redraw() {
        Forest<PlumtreeVertex, PlumtreeEdge> newTree = new MinimumSpanningForest2<>(graph, new DelegateForest<>(),
                DelegateTree.getFactory(), e -> e.getType() == PlumtreeEdge.Type.EAGER ? 0d : 10d).getForest();
        TreeLayout<PlumtreeVertex, PlumtreeEdge> newTreeLayout = new TreeLayout<>(newTree, 50, 100);
        StaticLayout<PlumtreeVertex, PlumtreeEdge> newLayout = new StaticLayout<>(graph, newTreeLayout);

        LayoutTransition<PlumtreeVertex, PlumtreeEdge> lt = new LayoutTransition<>(vv, currentLayout, newLayout);
        Animator animator = new Animator(lt);
        animator.start();
        currentLayout = newLayout;
    }
}
