package plumtree.viewer.decorators;

import plumtree.viewer.layout.PlumtreeEdge;
import plumtree.viewer.layout.PlumtreeVertex;
import com.google.common.base.Function;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

import java.awt.*;

public class EdgePainter implements Function<PlumtreeEdge, Paint> {

    private static final Color EAGER_PAINT = Color.red;
    private static final Color LAZY_PAINT = Color.green;
    private static final Color PENDING_PAINT = Color.black;
    private static final Color CURRENT_PENDING_PAINT = Color.gray;

    protected PickedInfo<PlumtreeVertex> pi;
    protected Graph<PlumtreeVertex, PlumtreeEdge> graph;

    public EdgePainter(Graph<PlumtreeVertex, PlumtreeEdge> g, PickedInfo<PlumtreeVertex> pi) {
        if (pi == null)
            throw new IllegalArgumentException("PickedInfo instance must be non-null");
        this.pi = pi;
        this.graph = g;
    }

    @Override
    public Paint apply(PlumtreeEdge edge) {

        if(pi.isPicked(graph.getSource(edge))) {
            switch (edge.getType()){
                case LAZY:
                    return LAZY_PAINT;
                case EAGER:
                    return EAGER_PAINT;
                case PENDING:
                    return PENDING_PAINT;
                case CURRENT_PENDING:
                    return CURRENT_PENDING_PAINT;
            }
        } else if(edge.getType() == PlumtreeEdge.Type.EAGER)
            return EAGER_PAINT;

        return null;
    }
}
