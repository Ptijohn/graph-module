package com.zenika.graph;

import junit.framework.TestCase;

/**
 * Created by ptijohn on 11/04/14.
 */
public class GraphTest extends TestCase {
    public void testGetIsolatedNodes() throws Exception {
        Graph graph = new Graph("GraphTest");

        assertTrue(graph.getIsolatedNodes().isEmpty());
        Artifact artifact1 = new Artifact("zenika", "RELEASE", "1.0.0", "A");
        graph.getArtifacts().add(artifact1);

        assertEquals(graph.getIsolatedNodes().size(), 1);

        Artifact artifact2 = new Artifact("zenika", "RELEASE", "1.0.0", "B");
        artifact2.getDependencies().add(artifact1);
        graph.getArtifacts().add(artifact2);

        assertEquals(graph.getIsolatedNodes().size(), 0);
    }
}
