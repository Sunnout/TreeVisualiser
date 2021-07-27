package plumtree.viewer.decorators;

import plumtree.viewer.layout.PlumtreeVertex;
import com.google.common.base.Function;

public class VertexLabeler implements Function<PlumtreeVertex, String> {
    public String apply(PlumtreeVertex input) {
        return input.toString().split(":")[0].split("\\.")[3];
    }
}
