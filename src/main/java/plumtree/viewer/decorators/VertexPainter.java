package plumtree.viewer.decorators;

import com.google.common.base.Function;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import plumtree.viewer.layout.PlumtreeEdge;
import plumtree.viewer.layout.PlumtreeVertex;

import java.awt.*;

public class VertexPainter implements Function<PlumtreeVertex, Paint> {

    private static final Paint PICKED_PAINT = Color.yellow;
    private static final Paint NORMAL_PAINT = Color.black;

    protected PickedInfo<PlumtreeVertex> pi;
    protected Graph<PlumtreeVertex, PlumtreeEdge> graph;

    public VertexPainter(Graph<PlumtreeVertex, PlumtreeEdge> g, PickedInfo<PlumtreeVertex> pi) {
        if (pi == null)
            throw new IllegalArgumentException("PickedInfo instance must be non-null");
        this.pi = pi;
        this.graph = g;
    }

    @NullableDecl
    @Override
    public Paint apply(@NullableDecl PlumtreeVertex vertex) {
        if (pi.isPicked(vertex))
            return PICKED_PAINT;
        else
            return NORMAL_PAINT;
    }
}
