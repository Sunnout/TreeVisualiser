package plumtree.viewer.decorators;

import com.google.common.base.Function;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import plumtree.viewer.layout.CalmEdge;
import plumtree.viewer.layout.CalmVertex;

import java.awt.*;

public class VertexPainter implements Function<CalmVertex, Paint> {

    protected Paint pickedPaint;
    protected Paint activePaint;
    protected Paint passivePaint;
    protected Paint incomingPaint;
    protected Paint blacklistPaint;
    protected Paint neutralPaint;
    protected PickedInfo<CalmVertex> pi;
    protected Graph<CalmVertex, CalmEdge> graph;

    public VertexPainter(Graph<CalmVertex, CalmEdge> g, PickedInfo<CalmVertex> pi,
                         Paint pickedPaint, Paint activePaint, Paint incomingPaint,
                         Paint passivePaint, Paint blacklistPaint, Paint neutralPaint) {
        if (pi == null)
            throw new IllegalArgumentException("PickedInfo instance must be non-null");
        this.pi = pi;
        this.pickedPaint = pickedPaint;
        this.activePaint = activePaint;
        this.passivePaint = passivePaint;
        this.neutralPaint = neutralPaint;
        this.incomingPaint = incomingPaint;
        this.blacklistPaint = blacklistPaint;
        this.graph = g;
    }

    @NullableDecl
    @Override
    public Paint apply(@NullableDecl CalmVertex vertex) {
        if (pi.isPicked(vertex))
            return pickedPaint;
        else if (isInSelectedActive(vertex, pi))
            return activePaint;
        else if (isInSelectedIncoming(vertex, pi))
            return incomingPaint;
        else if (isInSelectedPassive(vertex, pi))
            return passivePaint;
        else if (isSelectedBlacklist(vertex, pi))
            return blacklistPaint;
        else
            return neutralPaint;
    }

    private boolean isInSelectedPassive(CalmVertex vertex, PickedInfo<CalmVertex> pi) {
        return graph.getInEdges(vertex).stream().filter(CalmEdge::isPassive).map(e -> graph.getSource(e)).anyMatch(pi::isPicked);
    }

    private boolean isInSelectedIncoming(CalmVertex vertex, PickedInfo<CalmVertex> pi) {
        return graph.getInEdges(vertex).stream().filter(CalmEdge::isIncoming).map(e -> graph.getSource(e)).anyMatch(pi::isPicked);
    }

    private boolean isInSelectedActive(CalmVertex vertex, PickedInfo<CalmVertex> pi) {
        return graph.getInEdges(vertex).stream().filter(CalmEdge::isActive).map(e -> graph.getSource(e)).anyMatch(pi::isPicked);
    }

    private boolean isSelectedBlacklist(CalmVertex vertex, PickedInfo<CalmVertex> pi) {
        return graph.getInEdges(vertex).stream().filter(CalmEdge::isBlacklist).map(e -> graph.getSource(e)).anyMatch(pi::isPicked);
    }
}
