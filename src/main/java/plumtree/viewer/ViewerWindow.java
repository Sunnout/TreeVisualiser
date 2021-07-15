package plumtree.viewer;

import plumtree.viewer.decorators.*;
import plumtree.viewer.layout.*;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
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
import plumtree.viewer.utils.Host;
import plumtree.viewer.utils.Line;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ViewerWindow extends JApplet {

    Graph<PlumtreeVertex, PlumtreeEdge> graph;
    VisualizationViewer<PlumtreeVertex, PlumtreeEdge> vv;
    StaticLayout<PlumtreeVertex, PlumtreeEdge> layout;
    EdgeShaper transformerAll;

    Map<Host, PlumtreeVertex> vertices;
    List<Line> logs;
    int currLine;

    public ViewerWindow(List<Line> logs) {
        graph = new DirectedSparseMultigraph<>();
        layout = new StaticLayout<>(graph, PlumtreeVertex::getCoord);
        vv = new VisualizationViewer<>(layout, new Dimension(800, 600));
        vv.setBackground(Color.white);
        this.logs = logs;

        //EDGE
        transformerAll = new EdgeShaper(graph, vv.getPickedVertexState());
        vv.getRenderContext().setEdgeShapeTransformer(transformerAll);
        vv.getRenderContext().setEdgeArrowTransformer(new DirectionalEdgeArrowTransformer<>(10, 8, 4));
        EdgePainter arrowPainter = new EdgePainter(graph, vv.getPickedVertexState());
        vv.getRenderContext().setEdgeDrawPaintTransformer(arrowPainter);
        vv.getRenderContext().setArrowDrawPaintTransformer(arrowPainter);
        vv.getRenderContext().setArrowFillPaintTransformer(arrowPainter);

        //VERTEX
        vv.getRenderContext().setVertexLabelTransformer(new VertexLabeler());
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.BLUE));
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
        vv.getRenderContext().setVertexFillPaintTransformer(new VertexPainter(graph, vv.getPickedVertexState()));
        vv.setVertexToolTipTransformer(new ToStringLabeller());

        // ********************************** GUI ******************************************************

        JPanel edgePanel = new JPanel(new GridLayout(0, 1));
        edgePanel.setBorder(BorderFactory.createTitledBorder("EdgeType"));

        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);

        final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();
        vv.setGraphMouse(graphMouse);

        //MODE
        JComboBox<ModalGraphMouse.Mode> modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

        //ZOOM
        final ScalingControl scaler = new CrossoverScalingControl();
        JButton plus = new JButton("+");
        plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
        JButton minus = new JButton("-");
        minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));
        JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

        //STEPS
        JButton prev = new JButton("<");
        prev.addActionListener(e -> processPrevious());
        JButton next = new JButton(">");
        next.addActionListener(e -> processNext());
        JPanel stepGrid = new JPanel(new GridLayout(1, 0));
        stepGrid.setBorder(BorderFactory.createTitledBorder("Steps"));

        //CONTROLS
        JPanel controls = new JPanel();
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        stepGrid.add(prev);
        stepGrid.add(next);
        controls.add(scaleGrid);
        controls.add(stepGrid);
        controls.add(modeBox);
        controls.add(edgePanel);
        content.add(controls, BorderLayout.SOUTH);
    }

    public void addEdge(PlumtreeEdge e) {
        if(!graph.containsVertex(e.getOrigin())) graph.addVertex(e.getOrigin());
        if(!graph.containsVertex(e.getDestiny())) graph.addVertex(e.getDestiny());
        graph.addEdge(e, e.getOrigin(), e.getDestiny());
    }

    private void removeEdge(PlumtreeEdge e) {
        graph.removeEdge(e);
    }

    public void addVertex(PlumtreeVertex v) {
        graph.removeVertex(v);
        graph.addVertex(v);
    }

    public void removeVertex(PlumtreeVertex v) {
        graph.removeVertex(v);
    }

    public void redraw(){
        StaticLayout<PlumtreeVertex, PlumtreeEdge> staticLayout = new StaticLayout<>(layout.getGraph(), PlumtreeVertex::getCoord);
        layout.getGraph().getVertices().forEach(staticLayout::apply);
        LayoutTransition<PlumtreeVertex, PlumtreeEdge> lt = new LayoutTransition<>(vv, staticLayout, layout);
        Animator animator = new Animator(lt);
        animator.start();
        //vv.repaint();
    }

    private void processNext() {
        if(currLine >= logs.size()) {
            System.out.println("No more lines to process");
            return;
        }
        Line l = logs.get(currLine);
        System.out.println("Applying -> NODE: " + l.getNode() + " " + l.getContent());
        for(Map.Entry<String, Host> toAdd : l.getToAdd().entrySet()) {
            switch (toAdd.getKey()) {
                case "eager":
                    addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.EAGER));
                    break;
                case "lazy":
                    addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.LAZY));
                    break;
                case "pending":
                    addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.PENDING));
                    break;
                case "currPending":
                    addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.CURRENT_PENDING));
                    break;
                default:
                    break;
            }
        }

        for(Map.Entry<String, Host> toRemove : l.getToRemove().entrySet()) {
            switch (toRemove.getKey()) {
                case "eager":
                    removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.EAGER));
                    break;
                case "lazy":
                    removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.LAZY));
                    break;
                case "pending":
                    removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.PENDING));
                    break;
                case "currPending":
                    removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.CURRENT_PENDING));
                    break;
                default:
                    break;
            }
        }
        currLine++;
        redraw();
    }

    private void processPrevious() {
        if(currLine == 0) {
            System.out.println("Currently in first line");
            return;
        }
        currLine--;
        Line l = logs.get(currLine);
        System.out.println("Undoing -> NODE: " + l.getNode() + " " + l.getContent());
        for(Map.Entry<String, Host> toAdd : l.getToAdd().entrySet()) {
            switch (toAdd.getKey()) {
                case "eager":
                    removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.EAGER));
                    break;
                case "lazy":
                    removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.LAZY));
                    break;
                case "pending":
                    removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.PENDING));
                    break;
                case "currPending":
                    removeEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toAdd.getValue()), PlumtreeEdge.Type.CURRENT_PENDING));
                    break;
                default:
                    break;
            }
        }

        for(Map.Entry<String, Host> toRemove : l.getToRemove().entrySet()) {
            switch (toRemove.getKey()) {
                case "eager":
                    addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.EAGER));
                    break;
                case "lazy":
                    addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.LAZY));
                    break;
                case "pending":
                    addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.PENDING));
                    break;
                case "currPending":
                    addEdge(new PlumtreeEdge(vertices.get(l.getNode()), vertices.get(toRemove.getValue()), PlumtreeEdge.Type.CURRENT_PENDING));
                    break;
                default:
                    break;
            }
        }
        redraw();
    }

    public void getVertexMap(Map<Host, PlumtreeVertex> vertices) {
        this.vertices = vertices;
    }
}
