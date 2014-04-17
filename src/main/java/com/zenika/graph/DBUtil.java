package com.zenika.graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Iterator;

/**
 * Created by ptijohn on 17/04/14.
 */
public class DBUtil {
    /**
     * Cleans DB of every nodes/relationships
     * @param graphDb
     */
    public static void cleanDB(GraphDatabaseService graphDb){
        try ( Transaction tx = graphDb.beginTx() )
        {
            Iterator<Node> it = GlobalGraphOperations.at(graphDb).getAllNodes().iterator();
            while(it.hasNext()){
                Node n = it.next();
                for(Relationship r : n.getRelationships()){
                    r.delete();
                }
                n.delete();
            }
            tx.success();
        }
    }

    /**
     * Make the shutdown of DB cleaner
     * @param graphDb
     */
    public static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
}
