package com.zenika.graph;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by ptijohn on 14/04/14.
 */
public class Main {
    public static void main(String[] args){
        Properties prop = getProperties();
        List<Artifact> artifacts = null;
        List<Artifact> artifactsToMerge = null;
        Map<String, Long> nodes = new HashMap<String, Long>();
        GraphDatabaseService graphDb;

        //Two variables that you can change to alter the result displayed
        String nodeToTraverse = "A";
        Direction directionToTraverse = Direction.INCOMING;

        graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(prop.getProperty(GraphConstants.DB_PATH)).setConfig(GraphDatabaseSettings.cache_type,"strong").newGraphDatabase();

        DBUtil.registerShutdownHook(graphDb);
        String answer = "";
        if(!GraphUtil.isDBEmpty(graphDb)){

            Scanner scanInput  = new Scanner(System.in);

            do {
                System.out.print("Nodes detected in DB, do you want to load nodes from DB (if not, DB will be cleaned nodes loaded from files)? (O/n): ");

                answer = scanInput.nextLine();
            }//end do
            while(!"O".equals(answer) && !"n".equals(answer));

        }

        if("O".equals(answer)){
            GraphUtil.getNodesFromDB(graphDb, nodes);
        } else {
            DBUtil.cleanDB(graphDb);

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
                    GraphUtil.createNodeFromArtifact(artifact, graphDb, nodes);
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
                        GraphUtil.addDependenciesListToGraph(graphDb, nodes, artifact.getDependencies(), node);
                    }
                }

                tx.success();
            }
        }

        //Transaction to display graph result
        try ( Transaction tx = graphDb.beginTx() )
        {
            //System.out.println(GraphUtil.displayGraphAsNeo4J(graphDb, nodes, directionToTraverse, nodeToTraverse, 4));

            System.out.println(GraphUtil.displayGraphCustom(graphDb, nodes, directionToTraverse, nodeToTraverse, 4));

            System.out.println("Isolated nodes size : "+GraphUtil.findIsolatedNodes(graphDb).size());

            tx.success();
        }

        if(artifactsToMerge != null) {
            System.out.println("Merging...");
            GraphUtil.mergeNode(graphDb, artifactsToMerge, nodes);
            System.out.println("Merging DONE");


            //Transaction to display graph result
            try (Transaction tx = graphDb.beginTx()) {
                //System.out.println(GraphUtil.displayGraphAsNeo4J(graphDb, nodes, directionToTraverse, nodeToTraverse, 4));

                System.out.println(GraphUtil.displayGraphCustom(graphDb, nodes, directionToTraverse, nodeToTraverse, 4));

                System.out.println("Isolated nodes size : " + GraphUtil.findIsolatedNodes(graphDb).size());

                tx.success();
            }
        }

        //Cleaning DB, so that we don't keep in DB previous nodes created
        //If you clean it, next startup will get nodes from files
        //If you don't, we'll get nodes from DB or files, that will be your choice
        //DBUtil.cleanDB(graphDb);

        graphDb.shutdown();
    }

    /**
     * Gets properties object extracted from graph.properties file
     * @return
     */
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
}