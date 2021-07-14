package plumtree.viewer.layout;

import java.util.Objects;

public class CalmEdge {

    public enum Type {ACTIVE, PASSIVE, INCOMING, BLACKLIST}

    private Type type;
    private String info;
    private CalmVertex origin;
    private CalmVertex destiny;

    public CalmEdge(CalmVertex o, CalmVertex d, Type t, String info) {
        if (t == null) throw new AssertionError("Edge type is null");
        this.origin = o;
        this.destiny = d;
        this.type = t;
        this.info = info;
    }

    public boolean isActive() {
        return type == Type.ACTIVE;
    }

    public boolean isPassive() {
        return type == Type.PASSIVE;
    }

    public boolean isIncoming() {
        return type == Type.INCOMING;
    }

    public boolean isBlacklist() {
        return type == Type.BLACKLIST;
    }

    public CalmVertex getDestiny() {
        return destiny;
    }

    public CalmVertex getOrigin() {
        return origin;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "CalmEdge{" +
                "type=" + type +
                ", origin=" + origin +
                ", destiny=" + destiny +
                ", info= " + info +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalmEdge calmEdge = (CalmEdge) o;
        return type == calmEdge.type &&
                Objects.equals(origin, calmEdge.origin) &&
                Objects.equals(destiny, calmEdge.destiny);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, origin, destiny);
    }
}
