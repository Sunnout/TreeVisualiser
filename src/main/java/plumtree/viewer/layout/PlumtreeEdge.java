package plumtree.viewer.layout;

import java.util.Objects;

public class PlumtreeEdge {

    public enum Type {EAGER, LAZY, PENDING, CURRENT_PENDING}

    private Type type;
    private PlumtreeVertex origin;
    private PlumtreeVertex destiny;

    public PlumtreeEdge(PlumtreeVertex o, PlumtreeVertex d, Type t) {
        if (t == null) throw new AssertionError("Edge type is null");
        this.origin = o;
        this.destiny = d;
        this.type = t;
    }

    public PlumtreeVertex getDestiny() {
        return destiny;
    }

    public PlumtreeVertex getOrigin() {
        return origin;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PlumtreeEdge{" +
                "type=" + type +
                ", origin=" + origin +
                ", destiny=" + destiny +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlumtreeEdge plumtreeEdge = (PlumtreeEdge) o;
        return type == plumtreeEdge.type &&
                Objects.equals(origin, plumtreeEdge.origin) &&
                Objects.equals(destiny, plumtreeEdge.destiny);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, origin, destiny);
    }
}
