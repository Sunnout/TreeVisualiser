package plumtree.viewer.layout;

import network.data.Host;

import java.awt.geom.Point2D;
import java.util.Objects;

public class PlumtreeVertex {

    Host node;
    Point2D coord;

    public PlumtreeVertex(Host node, Point2D coord) {
        this.node = node;
        this.coord = coord;
    }

    public Point2D getCoord() {
        return coord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlumtreeVertex that = (PlumtreeVertex) o;
        return node.equals(that.node);
    }

    @Override
    public String toString() {
        return node.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }

    public Host getNode() {
        return node;
    }
}
