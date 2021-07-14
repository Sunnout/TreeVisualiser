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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class ViewerWindow extends JApplet {

    private static final Logger logger = LogManager.getLogger(ViewerWindow.class);

    Graph<PlumtreeVertex, PlumtreeEdge> graph;

    VisualizationViewer<PlumtreeVertex, PlumtreeEdge> vv;

    //VivaldiLayout layout;
    StaticLayout<PlumtreeVertex, PlumtreeEdge> layout;
    //EdgeShaperFather transformerFather;
    EdgeShaper transformerAll;

    public ViewerWindow() {
        // create a simple graph for the demo
        graph = new DirectedSparseMultigraph<>();
        //createTree();

        //layout = new VivaldiLayout(graph);
        layout = new StaticLayout<>(graph, PlumtreeVertex::getCoord);
        vv = new VisualizationViewer<>(layout, new Dimension(800, 600));
        vv.setBackground(Color.white);
        //EDGE
        //transformerFather = new EdgeShaperFather(graph, vv.getPickedVertexState());
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
        //vv.getRenderContext().setVertexFillPaintTransformer(new VertexPainter(graph, vv.getPickedVertexState(),
        //        Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.BLACK, Color.LIGHT_GRAY));

        vv.setVertexToolTipTransformer(new ToStringLabeller());

        // ********************************** GUI ******************************************************

        ButtonGroup radio = new ButtonGroup();
        /*JRadioButton lineButton = new JRadioButton("All", true);
        lineButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                vv.getRenderContext().setEdgeShapeTransformer(transformerAll);
                vv.repaint();
            }
        });
        JRadioButton quadButton = new JRadioButton("Father");
        quadButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                vv.getRenderContext().setEdgeShapeTransformer(transformerFather);
                vv.repaint();
            }
        });
        radio.add(lineButton);
        radio.add(quadButton);*/

        JPanel edgePanel = new JPanel(new GridLayout(0, 1));
        edgePanel.setBorder(BorderFactory.createTitledBorder("EdgeType"));
        //edgePanel.add(lineButton);
        //edgePanel.add(quadButton);

        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);

        final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();
        vv.setGraphMouse(graphMouse);

        JComboBox<ModalGraphMouse.Mode> modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(graphMouse.getModeListener());
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

        final ScalingControl scaler = new CrossoverScalingControl();
        JButton plus = new JButton("+");
        plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
        JButton minus = new JButton("-");
        minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));
        JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

        JPanel controls = new JPanel();
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        controls.add(scaleGrid);
        controls.add(modeBox);
        controls.add(edgePanel);

        content.add(controls, BorderLayout.SOUTH);
    }

    public void addEdge(PlumtreeEdge e) {
        logger.info("Add edge: " + e);
        if(!graph.containsVertex(e.getOrigin())) graph.addVertex(e.getOrigin());
        if(!graph.containsVertex(e.getDestiny())) graph.addVertex(e.getDestiny());
        graph.addEdge(e, e.getOrigin(), e.getDestiny());
    }

    private void removeEdge(PlumtreeEdge e) {
        logger.info("Remove edge: " + e);
        graph.removeEdge(e);
    }

    public void addVertex(PlumtreeVertex v) {
        logger.info("Add vertex: " + v);
        graph.removeVertex(v);
        graph.addVertex(v);
    }

    public void removeVertex(PlumtreeVertex v) {
        logger.info("Remove vertex: " + v);
        graph.removeVertex(v);
    }

    public void redraw(){

        StaticLayout<PlumtreeVertex, PlumtreeEdge> staticLayout = new StaticLayout<>(layout.getGraph(), PlumtreeVertex::getCoord);
        layout.getGraph().getVertices().forEach(staticLayout::apply);
        //layout.;
        LayoutTransition<PlumtreeVertex, PlumtreeEdge> lt = new LayoutTransition<>(vv, staticLayout, layout);
        Animator animator = new Animator(lt);
        animator.start();
        //vv.repaint();
    }
}
