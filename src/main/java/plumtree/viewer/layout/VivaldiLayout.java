package plumtree.viewer.layout;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.awt.*;
import java.awt.geom.Point2D;

public class VivaldiLayout implements Layout<PlumtreeVertex, PlumtreeEdge> {

    private Dimension dimension = new Dimension(800, 600);

    protected LoadingCache<PlumtreeVertex, Point2D> locations =
            CacheBuilder.newBuilder().build(new CacheLoader<PlumtreeVertex, Point2D>() {
                public Point2D load(PlumtreeVertex vertex) {
                    return new Point2D.Double();
                }
            });

    private Graph<PlumtreeVertex, PlumtreeEdge> graph;

    public VivaldiLayout(Graph<PlumtreeVertex, PlumtreeEdge> g) {
        if (g == null)
            throw new IllegalArgumentException("Graph must be non-null");
        this.graph = g;
        build();
    }

    public void rebuild() {
        build();
    }

    private void build() {
        //for (PlumtreeVertex node : graph.getVertices())
        //    locations.getUnchecked(node).setLocation(new Point((int) (node.coord.getX()), (int) (node.coord.getY())));
    }

    @Override
    public Graph<PlumtreeVertex, PlumtreeEdge> getGraph() {
        return graph;
    }

    @Override
    public void setGraph(Graph<PlumtreeVertex, PlumtreeEdge> graph) {
        this.graph = graph;
        build();
    }

    @Override
    public void setSize(Dimension d) {
        throw new UnsupportedOperationException("Size of HierarchyLayout is set" +
                " by vertex spacing in constructor");
    }

    @Override
    public void setLocation(PlumtreeVertex vertex, Point2D location) {
        locations.getUnchecked(vertex).setLocation(location);
    }

    @Override
    public Dimension getSize() {
        return dimension;
    }

    @NullableDecl
    @Override
    public Point2D apply(@NullableDecl PlumtreeVertex input) {
        return locations.getUnchecked(input);
    }

    @Override
    public void reset() {
    }

    @Override
    public void lock(PlumtreeVertex vertex, boolean state) {
    }

    @Override
    public boolean isLocked(PlumtreeVertex vertex) {
        return false;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void setInitializer(Function<PlumtreeVertex, Point2D> initializer) {
    }
}
