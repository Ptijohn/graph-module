package com.zenika.graph;

import junit.framework.TestCase;

/**
 * Created by ptijohn on 11/04/14.
 */
public class ParserUtilTest extends TestCase {
    public void testParseNode(){
        Node node = ParserUtil.parseNode("/home/ptijohn/Documents/jsonDataTest");
        assertNotNull(node);
        assertEquals(node.getName(), "C");
        assertFalse(node.getDependencies().isEmpty());
    }
}
