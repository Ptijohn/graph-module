package com.zenika.graph;

import junit.framework.TestCase;

/**
 * Created by ptijohn on 11/04/14.
 */
public class GraphTest extends TestCase {
    public void testGetIsolatedNodes() throws Exception {
        Graph graph = new Graph("GraphTest");

        assertTrue(graph.getIsolatedNodes().isEmpty());
        Node node1 = new Node("zenika", "RELEASE", "1.0.0", "A");
        graph.getNodes().add(node1);

        assertEquals(graph.getIsolatedNodes().size(), 1);

        Node node2 = new Node("zenika", "RELEASE", "1.0.0", "B");
        node2.getDependencies().add(node1);
        graph.getNodes().add(node2);

        assertEquals(graph.getIsolatedNodes().size(), 0);
    }
}
