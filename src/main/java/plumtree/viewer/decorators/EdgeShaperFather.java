package plumtree.viewer.decorators;

import com.google.common.base.Function;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import plumtree.viewer.layout.CalmEdge;
import plumtree.viewer.layout.CalmVertex;

import java.awt.*;
import java.awt.geom.Line2D;

import static plumtree.viewer.layout.CalmEdge.Type.*;

public class EdgeShaperFather implements Function<CalmEdge, Shape> {

    protected PickedInfo<CalmVertex> pi;
    protected Graph<CalmVertex, CalmEdge> graph;

    public EdgeShaperFather(Graph<CalmVertex, CalmEdge> g, PickedInfo<CalmVertex> pi) {
        this.pi = pi;
        this.graph = g;
    }

    @Override
    public Shape apply(CalmEdge edge) {
        if (pi.isPicked(edge.getOrigin()) && edge.getType() != PASSIVE && edge.getType() != BLACKLIST)
            return new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
        else if (edge.getDestiny().getLayer() != edge.getOrigin().getLayer() && edge.getType() == ACTIVE)
            return new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
        return new Line2D.Float(0.0f, 0.0f, 0.0f, 0.0f);
    }
}
