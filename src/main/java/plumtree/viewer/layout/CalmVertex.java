package plumtree.viewer.layout;

import network.data.Host;

import java.util.Objects;

public class CalmVertex {
    Host node;
    int layer;
    String info;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalmVertex that = (CalmVertex) o;
        return layer == that.layer && node.equals(that.node);
    }

    @Override
    public String toString() {
        //return "[" + node + ':' + layer + ']';
        //return "[" + node.getPort() + ":" + info + ']';
        String address = node.getAddress().getHostAddress();
        return address.substring(address.lastIndexOf('.') + 1) + " L" + layer + ":" + info;
    }

    public String prettyString() {
        String[] split = info.split(",");
        StringBuilder infoParsed = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            infoParsed.append(split[i]);
            if (i == 2) infoParsed.append("<br>");
        }
        String address = node.getAddress().getHostAddress();
        return "<html><center>" + address.substring(address.lastIndexOf('.') + 1)
                + " L" + layer + "<br>" + infoParsed + "</center>";
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, layer);
    }

    public CalmVertex(Host node, int layer, String info) {
        this.layer = layer;
        this.node = node;
        this.info = info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Host getNode() {
        return node;
    }

    public int getLayer() {
        return layer;
    }

    public String getInfo() {
        return info;
    }

}
