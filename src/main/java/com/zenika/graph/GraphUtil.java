package com.zenika.graph;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ptijohn on 16/04/14.
 */
public class GraphUtil {
    /**
     * Creates a list of all isolated nodes in the graph
     * @param graphDb
     * @return
     */
    public static List<Node> findIsolatedNodes(GraphDatabaseService graphDb){
        //We iterate to look for nodes that doesn't have any relationship
        List<Node> isolatedNodes = new ArrayList<Node>();
        Iterator<Node> it = GlobalGraphOperations.at(graphDb).getAllNodes().iterator();
        while(it.hasNext()){
            Node n = it.next();
            if(!n.hasRelationship()){
                isolatedNodes.add(n);
            }
        }
        return isolatedNodes;
    }

    public static void mergeNode(GraphDatabaseService graphDb, List<Artifact> artifacts, Map<String, Long> nodes, List<Long> relationshipList){
        // For each new artifact, we verify if it's in the graph or not, and add it or not if necessary

        try ( Transaction tx = graphDb.beginTx() )
        {
            //We go through the artifact list to create all necessary nodes
            for(Artifact artifact : artifacts){
                if(nodes.get(artifact.getName()) == null){
                    Node node = graphDb.createNode();
                    node.setProperty("org", artifact.getOrg());
                    node.setProperty("name", artifact.getName());
                    node.setProperty("status", artifact.getStatus());
                    node.setProperty("version", artifact.getVersion());
                    nodes.put(artifact.getName(), node.getId());

                    System.out.println(artifact.getName()+" "+node.getId());
                }
            }
            tx.success();
        }


        // We go through the new artifacts to create necessary relationships
        try ( Transaction tx = graphDb.beginTx() )
        {
            //We go through it again to get all relationships
            for(Artifact artifact : artifacts){
                Node node = graphDb.getNodeById(nodes.get(artifact.getName()));

                if(artifact.getDependencies() != null && !artifact.getDependencies().isEmpty()){
                    // If our node doesn't have any relationship, we just add the new relationships
                    if(!node.hasRelationship(Direction.OUTGOING, RelTypes.DEPENDS_OF)){
                        for(Artifact dependency : artifact.getDependencies()){
                            relationshipList.add(node.createRelationshipTo(graphDb.getNodeById(nodes.get(dependency.getName())), RelTypes.DEPENDS_OF).getId());
                        }
                    } else { // Here we update current relationships
                        //TODO Well, this part of code seems really heavy, should be easy to lighten this a bit
                        //First, we add new dependencies
                        for(Artifact dependency : artifact.getDependencies()){
                            Relationship relationshipFound = null;
                            for(Relationship relationship : node.getRelationships(Direction.OUTGOING, RelTypes.DEPENDS_OF)){
                                if(dependency.getName().equals(relationship.getEndNode().getProperty("name"))){
                                    relationshipFound = relationship;
                                }
                            }
                            if(relationshipFound == null){
                                relationshipList.add(node.createRelationshipTo(graphDb.getNodeById(nodes.get(dependency.getName())), RelTypes.DEPENDS_OF).getId());
                            }
                        }
                        //And then, we delete unnecessary relationships
                        for(Relationship relationship : node.getRelationships(Direction.OUTGOING, RelTypes.DEPENDS_OF)){
                            boolean relationshipFound = false;
                            for(Artifact dependency : artifact.getDependencies()){
                                relationshipFound = relationshipFound || dependency.getName().equals(relationship.getEndNode().getProperty("name"));
                            }
                            //If we didn't find
                            if(!relationshipFound){
                                relationship.delete();
                            }
                        }
                    }
                } else {
                    //If new artifact doesn't have any relationship, but old node has, we delete this relationships
                    if(node.hasRelationship(Direction.OUTGOING, RelTypes.DEPENDS_OF)){
                        for(Relationship relationship : node.getRelationships(Direction.OUTGOING, RelTypes.DEPENDS_OF)){
                            relationshipList.remove(relationship.getId());
                            relationship.delete();
                        }
                    }
                }
            }

            tx.success();
        }
    }

    /**
     * Display a graph the default way of Neo4j. Displays only node ids and their relations
     * @param graphDb
     * @param nodes
     * @return
     */
    public static String displayGraphAsNeo4J(GraphDatabaseService graphDb, Map<String, Long> nodes, Direction direction, String nodeName, int depth){
        String output = "";
        for ( Path position : graphDb.traversalDescription()
                .depthFirst()
                .relationships( RelTypes.DEPENDS_OF, direction ) //We go up the tree
                    /*.relationships( RelTypes.DEPENDS_OF, Direction.OUTGOING )*/ //We go down the tree
                    /*.relationships( RelTypes.DEPENDS_OF )*/ //We go in two directions
                .evaluator( Evaluators.toDepth(depth) ) //Manage depth of traversal
                .traverse(graphDb.getNodeById(nodes.get(nodeName))) )
        {
            output += position
                    + "\n";
        }
        return output;
    }

    /**
     * Display a custom graph. Trying to have the same result as default one, but with nodes names, instead of ids
     * @param graphDb
     * @param nodes
     * @return
     */
    public static String displayGraphCustom(GraphDatabaseService graphDb, Map<String, Long> nodes, Direction direction, String nodeName, int depth){
        String output = "";
        for ( Path position : graphDb.traversalDescription()
                .depthFirst()
                .relationships(RelTypes.DEPENDS_OF, direction) //We go up the tree
                    /*.relationships( RelTypes.DEPENDS_OF, Direction.OUTGOING )*/ //We go down the tree
                    /*.relationships( RelTypes.DEPENDS_OF )*/ //We go in two directions
                .evaluator(Evaluators.toDepth(depth)) //Manage depth of traversal
                .traverse(graphDb.getNodeById(nodes.get(nodeName))) )
        {
            output += buildRelationsGraph(position, direction) +"\n";
        }
        return output;
    }

    /**
     * Creates a string output that represents a node and its relations
     * @param position
     * @param direction
     * @return
     */
    private static String buildRelationsGraph(Path position, Direction direction){
        String output = "";
        //output += "("+node.getProperty("name")+")";
        for(Node node : position.nodes()){
            for(Relationship r : node.getRelationships(direction)){
                if(Direction.INCOMING.equals(direction)){
                    output+="("+node.getProperty("name")+")"+"<--["+r.getType().name()+"]--("+r.getStartNode().getProperty("name")+")";
                } else {
                    output+="("+node.getProperty("name")+")"+"--["+r.getType().name()+"]-->("+r.getEndNode().getProperty("name")+")";
                }
            }
        }

        return output;
    }
}
