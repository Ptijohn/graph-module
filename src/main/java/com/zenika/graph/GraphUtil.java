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

    /**
     * Display a graph the default way of Neo4j. Displays only node ids and their relations
     * @param graphDb
     * @param nodes
     * @return
     */
    public static String displayGraphAsNeo4J(GraphDatabaseService graphDb, Map<String, Node> nodes, Direction direction, String nodeName){
        String output = "";
        for ( Path position : graphDb.traversalDescription()
                .depthFirst()
                .relationships( RelTypes.DEPENDS_OF, direction ) //We go up the tree
                    /*.relationships( RelTypes.DEPENDS_OF, Direction.OUTGOING )*/ //We go down the tree
                    /*.relationships( RelTypes.DEPENDS_OF )*/ //We go in two directions
                .evaluator( Evaluators.toDepth(2) ) //Manage depth of traversal
                .traverse(nodes.get(nodeName)) )
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
    public static String displayGraphCustom(GraphDatabaseService graphDb, Map<String, Node> nodes, Direction direction, String nodeName){
        String output = "";
        for ( Path position : graphDb.traversalDescription()
                .depthFirst()
                .relationships(RelTypes.DEPENDS_OF, direction) //We go up the tree
                    /*.relationships( RelTypes.DEPENDS_OF, Direction.OUTGOING )*/ //We go down the tree
                    /*.relationships( RelTypes.DEPENDS_OF )*/ //We go in two directions
                .evaluator(Evaluators.toDepth(2)) //Manage depth of traversal
                .traverse(nodes.get(nodeName)) )
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
