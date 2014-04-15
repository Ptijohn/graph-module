package com.zenika.graph;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by ptijohn on 14/04/14.
 */
public class Main {
    public static void main(String[] args){
        Properties prop = getProperties();
        Graph graph = null;
        Map<String, Node> nodes = new HashMap<String, Node>();
        List<Relationship> relationshipList = new ArrayList<Relationship>();

        GraphDatabaseService graphDb;
        Relationship relationship;

        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(prop.getProperty(GraphConstants.DB_PATH));
        registerShutdownHook(graphDb);

        if(prop == null){
            graph = ParserUtil.scanDirectory("/home/ptijohn/Documents/Nodes");
        } else {
            graph = ParserUtil.scanDirectory(prop.getProperty(GraphConstants.NODES_DIRECTORY));
        }

        try ( Transaction tx = graphDb.beginTx() )
        {
            //We go through the artifact list to create all nodes
            for(Artifact artifact : graph.getArtifacts()){
                Node node = graphDb.createNode();
                node.setProperty("org", artifact.getOrg());
                node.setProperty("name", artifact.getName());
                node.setProperty("status", artifact.getStatus());
                node.setProperty("version", artifact.getVersion());
                nodes.put(artifact.getName(), node);

                System.out.println(artifact.getName()+" "+node.getId());
            }

            //We go through it again to get all relationships
            for(Artifact artifact : graph.getArtifacts()){
                if(artifact.getDependencies() != null && !artifact.getDependencies().isEmpty()){
                    Node node = nodes.get(artifact.getName());
                    for(Artifact dependency : artifact.getDependencies()){
                        relationshipList.add(node.createRelationshipTo(nodes.get(dependency.getName()), RelTypes.DEPENDS_OF));
                    }
                }
            }

            String output = "";
            for ( Path position : graphDb.traversalDescription()
                    .depthFirst()
                    .relationships( RelTypes.DEPENDS_OF, Direction.INCOMING ) //We go up the tree
                    /*.relationships( RelTypes.DEPENDS_OF, Direction.OUTGOING )*/ //We go down the tree
                    /*.relationships( RelTypes.DEPENDS_OF )*/ //We go in two directions
                    .traverse( nodes.get("B") ) )
            {
                output += position
                        + "\n";
            }

            System.out.println(output);
            tx.success();
        }

        graphDb.shutdown();



        System.out.println(graph);

        Graph invertedGraph = null;

        invertedGraph = Graph.invertGraph(graph);

        System.out.println(invertedGraph);

        System.out.println("Isolated nodes: "+invertedGraph.getIsolatedNodes());
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

    private static enum RelTypes implements RelationshipType
    {
        DEPENDS_OF
    }
}