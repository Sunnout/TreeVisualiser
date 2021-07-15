package plumtree.viewer.utils;

import java.sql.Timestamp;
import java.util.Comparator;

public class LineByTimestamp implements Comparator<Line> {

    @Override
    public int compare(Line l1, Line l2) {
        Timestamp ts1 = l1.getTs();
        Timestamp ts2 = l2.getTs();
        if (ts1.before(ts2))
            return -1;
        else if (ts1.after(ts2))
            return 1;
        else
            return 0;
    }
}
