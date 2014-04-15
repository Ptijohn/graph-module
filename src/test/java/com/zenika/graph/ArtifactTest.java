package com.zenika.graph;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ptijohn on 11/04/14.
 */
public class ArtifactTest extends TestCase {

    @Test
    public void testIsIsolated() throws Exception {
        Artifact artifact = new Artifact("zenika", "RELEASE", "1.0.0", "A");
        assertTrue(artifact.hasDependencies());
        List<Artifact> nexts = new ArrayList<Artifact>();
        nexts.add(new Artifact("zenika", "RELEASE", "1.0.0", "B"));
        artifact.setDependencies(nexts);
        assertFalse(artifact.hasDependencies());
    }
}
