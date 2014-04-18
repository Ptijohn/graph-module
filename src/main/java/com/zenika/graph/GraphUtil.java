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
     * Merges an existing (or not) node into our graph
     * @param graphDb
     * @param artifacts
     * @param nodes
     */
    public static void mergeNode(GraphDatabaseService graphDb, List<Artifact> artifacts, Map<String, Long> nodes){
        // For each new artifact, we verify if it's in the graph or not, and add it or not if necessary

        try ( Transaction tx = graphDb.beginTx() )
        {
            //We go through the artifact list to create all necessary nodes
            for(Artifact artifact : artifacts){
                if(nodes.get(artifact.getName()) == null){
                    createNodeFromArtifact(artifact, graphDb, nodes);
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

                //If our new node have dependencies or not
                if(artifact.getDependencies() != null && !artifact.getDependencies().isEmpty()){
                    // If our node doesn't have any relationship, we just add the new relationships
                    if(!node.hasRelationship(Direction.OUTGOING, RelTypes.DEPENDS_OF)){
                        addDependenciesListToGraph(graphDb, nodes, artifact.getDependencies(), node);
                    } else {
                        // In case of old and new node having relationships, we have to check
                        // if new node has existing relationships or not
                        updateRelationships(node, artifact, graphDb, nodes);
                    }
                } else {
                    //If new artifact doesn't have any relationship, but old node has, we delete this relationships
                    if(node.hasRelationship(Direction.OUTGOING, RelTypes.DEPENDS_OF)){
                        deleteDependenciesListFromGraph(node.getRelationships(Direction.OUTGOING, RelTypes.DEPENDS_OF));
                    }
                }
            }

            tx.success();
        }
    }

    /**
     * Method that
     * @param node
     * @param artifact
     * @param graphDb
     * @param nodes
     */
    private static void updateRelationships(Node node, Artifact artifact, GraphDatabaseService graphDb, Map<String, Long> nodes){
        List<Artifact> dependenciesToAdd = new ArrayList<Artifact>();
        List<Artifact> dependenciesAlreadyPresent = new ArrayList<Artifact>();
        //We delete unnecessary relationships and store dependencies to add before really adding them.
        for(Relationship relationship : node.getRelationships(Direction.OUTGOING, RelTypes.DEPENDS_OF)){
            boolean relationshipFound = false;
            for(Artifact dependency : artifact.getDependencies()){
                relationshipFound = relationshipFound || dependency.getName().equals(relationship.getEndNode().getProperty("name"));
                if(!dependency.getName().equals(relationship.getEndNode().getProperty("name")) && !dependenciesToAdd.contains(dependency) && !dependenciesAlreadyPresent.contains(dependency)){
                    dependenciesToAdd.add(dependency);
                } else if(dependency.getName().equals(relationship.getEndNode().getProperty("name")) && dependenciesToAdd.contains(dependency)){
                    dependenciesToAdd.remove(dependency);
                } else if(dependency.getName().equals(relationship.getEndNode().getProperty("name")) && !dependenciesAlreadyPresent.contains(dependency)){
                    dependenciesAlreadyPresent.add(dependency);
                }
            }
            //If we didn't find the relationship in the node's dependencies, we delete it
            if(!relationshipFound){
                relationship.delete();
            }
        }
        //We then add those dependencies spotted during deleting process
        addDependenciesListToGraph(graphDb, nodes, dependenciesToAdd, node);
    }

    /**
     * Add a list of artifacts to the graph as relationships to the node in entrance
     * @param graphDb
     * @param nodes
     * @param dependenciesToAdd
     * @param node
     */
    public static void addDependenciesListToGraph(GraphDatabaseService graphDb, Map<String, Long> nodes, List<Artifact> dependenciesToAdd, Node node){
        for(Artifact dependency : dependenciesToAdd){
            node.createRelationshipTo(graphDb.getNodeById(nodes.get(dependency.getName())), RelTypes.DEPENDS_OF).getId();
        }
    }

    /**
     * Delete a list (iterable) of relationships from the graph
     * @param relationshipsToDelete
     */
    private static void deleteDependenciesListFromGraph(Iterable<Relationship> relationshipsToDelete){
        for(Relationship relationship : relationshipsToDelete){
            relationship.delete();
        }
    }

    /**
     * Create a node from an artifact object
     * @param artifact
     * @param graphDb
     * @param nodes
     */
    public static void createNodeFromArtifact(Artifact artifact, GraphDatabaseService graphDb, Map<String, Long> nodes){
        Node node = graphDb.createNode();
        node.setProperty("org", artifact.getOrg());
        node.setProperty("name", artifact.getName());
        node.setProperty("status", artifact.getStatus());
        node.setProperty("version", artifact.getVersion());
        nodes.put(artifact.getName(), node.getId());
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
            output += position + "\n";
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
        if(position.length() > 0){
            Iterator<Node> it = position.nodes().iterator();

            Node n = null;
            Node previousNode = null;
            boolean firstOccurence = true;
            //We iterate over the nodes accessible at this position
            while(it.hasNext()){
                n = it.next();

                Relationship relationFound = null;

                //When we first go through the loop, we set previousNode as the startNode of our position
                //It will be used to find next relationship when we go deeper into the graph
                if(previousNode == null) {
                    previousNode = position.startNode();
                }

                //We look for the relationship between our current node and the previous node
                for(Relationship relationship :previousNode.getRelationships()){
                    if(relationship.getEndNode().equals(n)){
                        relationFound = relationship;
                    }
                }
                //If this is our first loop, we add the first node name
                if(firstOccurence) {
                    output += "(" + previousNode.getProperty("name") + ")";
                }

                //Depending on the direction in which we wish to traverse the graph, the output will be different
                if(Direction.INCOMING.equals(direction) && n!=position.endNode()){

                    output+="<--["+relationFound.getType().name()+","+relationFound.getId()+"]--("+relationFound.getStartNode().getProperty("name")+")";

                    //We set the previous node as the end node of our position (because of INCOMING direction)
                    previousNode = position.endNode();
                } else if(Direction.OUTGOING.equals(direction) && n!=position.startNode()) {

                    output+="--["+relationFound.getType().name()+","+relationFound.getId()+"]-->("+n.getProperty("name")+")";

                    previousNode = n;
                }
                firstOccurence = false;
            }

        } else {
            output = "("+position.startNode().getProperty("name")+")";
        }

        return output;
    }

    /**
     * Load nodes from DB if it is not empty. The nodes are actually loaded in our map name->id for easier retrieval
     * @param graphDb
     * @param nodes
     * @return
     */
    public static boolean getNodesFromDB(GraphDatabaseService graphDb, Map<String, Long> nodes){
        Iterator<Node> it = null;
        try ( Transaction tx = graphDb.beginTx() ) {
            it = GlobalGraphOperations.at(graphDb).getAllNodes().iterator();

            if(it == null || !it.hasNext()){
                return false;
            }

            while(it.hasNext()){
                Node n = it.next();

                nodes.put((String)n.getProperty("name"), n.getId());
            }
            tx.success();
        }
        return true;
    }

    /**
     * Checks if DB is empty or not
     * @param graphDb
     * @return true if empty
     */
    public static boolean isDBEmpty(GraphDatabaseService graphDb){
        Iterator<Node> it = null;
        boolean emptyDB = false;
        try ( Transaction tx = graphDb.beginTx() ) {
            it = GlobalGraphOperations.at(graphDb).getAllNodes().iterator();

            emptyDB = (it == null || !it.hasNext());
            tx.success();
        }
        return emptyDB;
    }
}
