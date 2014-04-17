package com.zenika.graph;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by ptijohn on 14/04/14.
 */
public class Main {
    public static void main(String[] args){
        Properties prop = getProperties();
        //Graph graph = null;
        List<Artifact> artifacts;
        List<Artifact> artifactsToMerge;
        Map<String, Long> nodes = new HashMap<String, Long>();
        List<Long> relationshipList = new ArrayList<Long>();

        GraphDatabaseService graphDb;
        Relationship relationship;

        graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(prop.getProperty(GraphConstants.DB_PATH)).setConfig(GraphDatabaseSettings.cache_type,"strong").newGraphDatabase();

        registerShutdownHook(graphDb);

        if(prop == null){
            artifacts = ParserUtil.scanDirectory("src/main/resources/Nodes");
            artifactsToMerge = ParserUtil.scanDirectory("src/main/resources/NodesToMerge");
        } else {
            artifacts = ParserUtil.scanDirectory(prop.getProperty(GraphConstants.ARTIFACTS_DIRECTORY));
            artifactsToMerge = ParserUtil.scanDirectory(prop.getProperty(GraphConstants.ARTIFCATS_MERGE_DIRECTORY));
        }

        //Transaction to create nodes
        try ( Transaction tx = graphDb.beginTx() )
        {
            //We go through the artifact list to create all nodes
            for(Artifact artifact : artifacts){
                Node node = graphDb.createNode();
                node.setProperty("org", artifact.getOrg());
                node.setProperty("name", artifact.getName());
                node.setProperty("status", artifact.getStatus());
                node.setProperty("version", artifact.getVersion());
                nodes.put(artifact.getName(), node.getId());

                System.out.println(artifact.getName()+" "+node.getId());
            }
            tx.success();
        }

        //Transaction to create relationships
        try ( Transaction tx = graphDb.beginTx() )
        {
            //We go through it again to get all relationships
            for(Artifact artifact : artifacts){
                if(artifact.getDependencies() != null && !artifact.getDependencies().isEmpty()){
                    Node node = graphDb.getNodeById(nodes.get(artifact.getName()));
                    for(Artifact dependency : artifact.getDependencies()){
                        relationshipList.add(node.createRelationshipTo(graphDb.getNodeById(nodes.get(dependency.getName())), RelTypes.DEPENDS_OF).getId());
                    }
                }
            }

            tx.success();
        }

        //Transaction to display graph result
        try ( Transaction tx = graphDb.beginTx() )
        {
            System.out.println(GraphUtil.displayGraphAsNeo4J(graphDb, nodes, Direction.OUTGOING, "C", 4));

            System.out.println(GraphUtil.displayGraphCustom(graphDb, nodes, Direction.OUTGOING, "C", 4));

            System.out.println("Isolated nodes size : "+GraphUtil.findIsolatedNodes(graphDb).size());

            tx.success();
        }



        System.out.println("Merging...");
        GraphUtil.mergeNode(graphDb, artifactsToMerge, nodes, relationshipList);
        System.out.println("Merging DONE");

        //Transaction to display graph result
        try ( Transaction tx = graphDb.beginTx() )
        {
            System.out.println(GraphUtil.displayGraphAsNeo4J(graphDb, nodes, Direction.OUTGOING, "C", 4));

            System.out.println(GraphUtil.displayGraphCustom(graphDb, nodes, Direction.OUTGOING, "C", 4));

            System.out.println("Isolated nodes size : "+GraphUtil.findIsolatedNodes(graphDb).size());

            tx.success();
        }



        //Cleaning DB, so that we don't keep in DB previous nodes created
        cleanDB(graphDb);

        graphDb.shutdown();
    }

    public static Properties getProperties(){
        Properties prop = new Properties();
        InputStream input = null;

        try {

            String filename = "graph.properties";
            input = Main.class.getClassLoader().getResourceAsStream(filename);
            if(input==null){
                System.out.println("Sorry, unable to find " + filename);
                return null;
            }

            //load a properties file from class path, inside static method
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return prop;
    }

    /**
     * Make the shutdown of DB cleaner
     * @param graphDb
     */
    private static void registerShutdownHook( final GraphDatabaseService graphDb )
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

    /**
     * Cleans DB of every nodes/relationships
     * @param graphDb
     */
    private static void cleanDB(GraphDatabaseService graphDb){
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


}