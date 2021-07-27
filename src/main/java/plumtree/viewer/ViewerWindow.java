package plumtree.viewer;

import com.google.common.base.Functions;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
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
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewerWindow extends JApplet {

    Graph<PlumtreeVertex, PlumtreeEdge> graph;
    VisualizationViewer<PlumtreeVertex, PlumtreeEdge> vv;
//    FREmaLayout layout;
    EdgeShaper transformerAll;

    Layout<PlumtreeVertex, PlumtreeEdge> currentLayout;
    Map<Host, PlumtreeVertex> vertices;
    List<Line> logs;
    int currLine;

    public ViewerWindow(List<Line> logs) {
//        graph = new DirectedSparseMultigraph<>();
//        layout = new FREmaLayout(graph, new Dimension(850, 850));
//        Layout<PlumtreeVertex, PlumtreeEdge> staticLayout = new StaticLayout<>(graph, layout);

        graph = new DirectedSparseMultigraph<>();

        Forest<PlumtreeVertex, PlumtreeEdge> tree = new MinimumSpanningForest2<>(graph,
                new DelegateForest<>(), DelegateTree.getFactory(),
                Functions.constant(1.0)).getForest();
        TreeLayout<PlumtreeVertex, PlumtreeEdge> layout = new TreeLayout<>(tree);
        currentLayout = new StaticLayout<>(graph, layout);
        vv = new VisualizationViewer<>(currentLayout, new Dimension(850, 850));
        vv.setBackground(Color.white);

        vertices = new HashMap<>();
        this.logs = logs;
        currLine = 0;

        //EDGE
        transformerAll = new EdgeShaper(graph, vv.getPickedVertexState());
        vv.getRenderContext().setEdgeShapeTransformer(transformerAll);
//        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line(graph));
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
        prev.addActionListener(e -> processPrevious(1));
        JButton prevprev = new JButton("<<");
        prevprev.addActionListener(e -> processPrevious(10));
        JButton prevprevprev = new JButton("<<<");
        prevprevprev.addActionListener(e -> processPrevious(100));
        JButton next = new JButton(">");
        next.addActionListener(e -> processNext(1));
        JButton nextNext = new JButton(">>");
        nextNext.addActionListener(e -> processNext(10));
        JButton nextNextNext = new JButton(">>>");
        nextNextNext.addActionListener(e -> processNext(100));
        JPanel stepGrid = new JPanel(new GridLayout(1, 0));
        stepGrid.setBorder(BorderFactory.createTitledBorder("Steps (1, 10 or 100)"));

        //CONTROLS
        JPanel controls = new JPanel();
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        stepGrid.add(prevprevprev);
        stepGrid.add(prevprev);
        stepGrid.add(prev);
        stepGrid.add(next);
        stepGrid.add(nextNext);
        stepGrid.add(nextNextNext);
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
        graph.addVertex(v);
    }

    public void removeVertex(PlumtreeVertex v) {
        graph.removeVertex(v);
    }

//    public void redraw(){
//        layout.initialize();
//        Relaxer relaxer = new VisRunner((IterativeContext)layout);
//        relaxer.stop();
//        relaxer.prerelax();
//        StaticLayout<PlumtreeVertex,PlumtreeEdge> staticLayout = new StaticLayout<>(graph, layout);
//        LayoutTransition<PlumtreeVertex,PlumtreeEdge> lt = new LayoutTransition<>(vv, vv.getGraphLayout(), staticLayout);
//        Animator animator = new Animator(lt);
//        animator.start();
//        vv.repaint();
//    }

    public void redraw(){
        Forest<PlumtreeVertex, PlumtreeEdge> newTree = new MinimumSpanningForest2<>(graph, new DelegateForest<>(),
                DelegateTree.getFactory(), e-> e.getType()== PlumtreeEdge.Type.EAGER ? 0d : 10d ).getForest();
        TreeLayout<PlumtreeVertex, PlumtreeEdge> newTreeLayout = new TreeLayout<>(newTree, 50, 100);
        StaticLayout<PlumtreeVertex, PlumtreeEdge> newLayout = new StaticLayout<>(graph, newTreeLayout);

        LayoutTransition<PlumtreeVertex, PlumtreeEdge> lt = new LayoutTransition<>(vv, currentLayout, newLayout);
        Animator animator = new Animator(lt);
        animator.start();
        currentLayout = newLayout;
    }

    private void processNext(int numberOfLines) {
        if (currLine + numberOfLines > logs.size()) {
            System.out.println("Cannot process " + numberOfLines + " lines");
            return;
        }

        for (int i = 0; i < numberOfLines; i++) {
            Line l = logs.get(currLine);
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

                for (Map.Entry<String, Host> toRemove : l.getToRemove().entrySet()) {
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
            }
            currLine++;
        }
        redraw();
    }

    private void processPrevious(int numberOfLines) {
        if (currLine - numberOfLines < 0) {
            System.out.println("Cannot undo " + numberOfLines + " lines");
            return;
        }

        for (int i = 0; i < numberOfLines; i++) {
            currLine--;
            Line l = logs.get(currLine);
            System.out.println(l.getTs() + " Undoing -> NODE: " + l.getNode() + " " + l.getContent());

            if (l.getHello() != null) {
                PlumtreeVertex v1 = new PlumtreeVertex(l.getHello());
                removeVertex(v1);
            } else if (l.getGoodbye() != null) {
                PlumtreeVertex v1 = new PlumtreeVertex(l.getGoodbye());
                addVertex(v1);
                vertices.put(l.getGoodbye(), v1);
            } else {
                for (Map.Entry<String, Host> toAdd : l.getToAdd().entrySet()) {
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

                for (Map.Entry<String, Host> toRemove : l.getToRemove().entrySet()) {
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
            }
        }
        redraw();
    }
}
