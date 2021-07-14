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
import java.util.*;

public class CalmLayout implements Layout<CalmVertex, CalmEdge> {

    public static int DEFAULT_DIST_X = 50;
    public static int DEFAULT_DIST_Y = 50;

    private int distX;
    private int distY;

    private Dimension dimension = new Dimension(800, 600);
    protected LoadingCache<CalmVertex, Point2D> locations =
            CacheBuilder.newBuilder().build(new CacheLoader<CalmVertex, Point2D>() {
                public Point2D load(CalmVertex vertex) {
                    return new Point2D.Double();
                }
            });

    private Graph<CalmVertex, CalmEdge> graph;

    public CalmLayout(Graph<CalmVertex, CalmEdge> g) {
        this(g, DEFAULT_DIST_X, DEFAULT_DIST_Y);
    }

    public CalmLayout(Graph<CalmVertex, CalmEdge> g, int distX) {
        this(g, distX, DEFAULT_DIST_Y);
    }

    public CalmLayout(Graph<CalmVertex, CalmEdge> g, int distX, int distY) {
        if (g == null)
            throw new IllegalArgumentException("Graph must be non-null");
        if (distX < 1 || distY < 1)
            throw new IllegalArgumentException("X and Y distances must each be positive");
        this.graph = g;
        this.distX = distX;
        this.distY = distY;
        build();
    }

    public void rebuild() {
        build();
    }

    private void build() {
        NavigableMap<Integer, Set<CalmVertex>> unplacedNodes = new TreeMap<>();
        graph.getVertices().forEach(v -> unplacedNodes.computeIfAbsent(v.getLayer(), k -> new HashSet<>()).add(v));

        int horizontalSize = unplacedNodes.values().stream().mapToInt((Set::size)).max().orElse(0) * distX;

        NavigableMap<Integer, LinkedList<CalmVertex>> sortedNodesPerLayer = new TreeMap<>();
        unplacedNodes.keySet().forEach(k -> sortedNodesPerLayer.put(k, new LinkedList<>()));

        //Sort (possibly multiple) trees
        while (!unplacedNodes.isEmpty()) {
            Set<CalmVertex> firstSet = unplacedNodes.remove(unplacedNodes.firstKey());
            for (Iterator<CalmVertex> it = firstSet.iterator(); it.hasNext(); ) {
                CalmVertex node = it.next();
                it.remove();
                sortedNodesPerLayer.get(node.getLayer()).add(node);
                sortChildren(node, unplacedNodes, sortedNodesPerLayer);
            }
        }

        //Set positions
        for (Integer layer : sortedNodesPerLayer.keySet()) {
            LinkedList<CalmVertex> layerNodes = sortedNodesPerLayer.get(layer);
            int layerSpacing = horizontalSize / layerNodes.size();
            int nodeIdx = 0;
            boolean even = true;
            for (CalmVertex node : layerNodes) {
                locations.getUnchecked(node).setLocation(new Point(
                        layerSpacing * nodeIdx + layerSpacing / 2 + 100, distY * (layer + 1) + (even ? 10 : 0)));
                nodeIdx++;
                even = !even;
            }
        }
    }

    private void sortChildren(CalmVertex node, NavigableMap<Integer, Set<CalmVertex>> unplacedNodes,
                              NavigableMap<Integer, LinkedList<CalmVertex>> sortedNodesPerLayer) {
        //Filter only active view, then filter out brothers && already placed -- keeps only children
        Set<CalmVertex> children = new HashSet<>();
        for (CalmEdge e : graph.getInEdges(node)) {
            if (e.isActive()) {
                CalmVertex c = graph.getSource(e);
                if (c.getLayer() != node.getLayer() && unplacedNodes.get(c.getLayer()).remove(c)) {
                    children.add(c);
                }
            }
        }

        for (CalmVertex child : children)
            sortedNodesPerLayer.get(child.getLayer()).add(child);
        for (CalmVertex child : children)
            sortChildren(child, unplacedNodes, sortedNodesPerLayer);
    }

    @Override
    public Graph<CalmVertex, CalmEdge> getGraph() {
        return graph;
    }

    @Override
    public void setGraph(Graph<CalmVertex, CalmEdge> graph) {
        this.graph = graph;
        build();
    }

    @Override
    public void setSize(Dimension d) {
        throw new UnsupportedOperationException("Size of HierarchyLayout is set" +
                " by vertex spacing in constructor");
    }

    @Override
    public void setLocation(CalmVertex vertex, Point2D location) {
        locations.getUnchecked(vertex).setLocation(location);
    }

    @Override
    public Dimension getSize() {
        return dimension;
    }

    @NullableDecl
    @Override
    public Point2D apply(@NullableDecl CalmVertex input) {
        return locations.getUnchecked(input);
    }

    @Override
    public void reset() {
    }

    @Override
    public void lock(CalmVertex vertex, boolean state) {
    }

    @Override
    public boolean isLocked(CalmVertex vertex) {
        return false;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void setInitializer(Function<CalmVertex, Point2D> initializer) {
    }
}
