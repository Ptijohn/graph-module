package com.zenika.graph;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ptijohn on 11/04/14.
 */
public class NodeTest extends TestCase {

    @Test
    public void testIsIsolated() throws Exception {
        Node node = new Node("zenika", "RELEASE", "1.0.0", "A");
        assertTrue(node.isIsolated());
        List<Node> nexts = new ArrayList<Node>();
        nexts.add(new Node("zenika", "RELEASE", "1.0.0", "B"));
        node.setDependencies(nexts);
        assertFalse(node.isIsolated());
    }
}
