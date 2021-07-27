package plumtree.viewer.decorators;

import plumtree.viewer.layout.PlumtreeEdge;
import plumtree.viewer.layout.PlumtreeVertex;
import com.google.common.base.Function;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

import java.awt.*;
import java.awt.geom.QuadCurve2D;

public class EdgeShaper implements Function<PlumtreeEdge, Shape> {

    protected PickedInfo<PlumtreeVertex> pi;
    protected Graph<PlumtreeVertex, PlumtreeEdge> graph;

    public EdgeShaper(Graph<PlumtreeVertex, PlumtreeEdge> g, PickedInfo<PlumtreeVertex> pi){
        this.pi = pi;
        this.graph = g;
    }

    @Override
    public Shape apply(PlumtreeEdge edge) {
        return new QuadCurve2D.Float(0.0f, 0.0f, 0.5f, 10, 1.0f, 0.0f);
    }
}
