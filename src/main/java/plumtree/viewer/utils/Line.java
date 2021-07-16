package plumtree.viewer.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Line {

    private Host node;
    private Timestamp ts;
    private String content;
    private String cause;
    private Map<String, Host> toRemove;
    private Map<String, Host> toAdd;
    private Host hello;
    private Host goodbye;

    public Line(Host node, Timestamp ts, String content) throws UnknownHostException {
        this.node = node;
        this.ts = ts;
        this.content = content;
        this.toRemove = new HashMap<>();
        this.toAdd = new HashMap<>();
        computeChanges();
    }

    public Host getNode() {
        return node;
    }

    public Timestamp getTs() {
        return ts;
    }

    public String getContent() {
        return content;
    }

    public String getCause() {
        return cause;
    }

    public Map<String, Host> getToRemove() {
        return toRemove;
    }

    public Map<String, Host> getToAdd() {
        return toAdd;
    }

    public Host getHello() {
        return hello;
    }

    public Host getGoodbye() {
        return goodbye;
    }

    @Override
    public String toString() {
        return "Line{" +
                "node=" + node +
                ", ts=" + ts +
                ", content='" + content + '\'' +
                '}';
    }

    private void computeChanges() throws UnknownHostException {
        if (content.contains("Hello")) {
            this.hello = node;
        } else if (content.contains("Goodbye")) {
            this.goodbye = node;
        } else {
            String[] c = content.split(":", 2);
            this.cause = c[0];
            String[] h = c[1].split(";");

            String[] actions = new String[h.length - 1];
            for (int i = 0; i < h.length - 1; i++) {
                actions[i] = h[i].trim();
            }

            for (String action : actions) {
                String[] parts = action.split(" ");
                String[] host = parts[1].split(":");
                if (parts[0].equals("Removed"))
                    toRemove.put(parts[3], new Host(InetAddress.getByName(host[0]), Integer.parseInt(host[1])));
                else if (parts[0].equals("Added"))
                    toAdd.put(parts[3], new Host(InetAddress.getByName(host[0]), Integer.parseInt(host[1])));
            }
        }
    }

}
