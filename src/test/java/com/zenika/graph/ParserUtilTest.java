package com.zenika.graph;

import junit.framework.TestCase;

/**
 * Created by ptijohn on 11/04/14.
 */
public class ParserUtilTest extends TestCase {
    public void testParseNode(){
        Artifact artifact = ParserUtil.parseNode("src/main/resources/Nodes/nodeC");
        assertNotNull(artifact);
        assertEquals(artifact.getName(), "C");
        assertFalse(artifact.getDependencies().isEmpty());
    }
}
