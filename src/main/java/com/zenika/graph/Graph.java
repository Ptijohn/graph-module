package com.zenika.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ptijohn on 11/04/14.
 */
public class Graph {
    String name;
    private List<Node> nodes;

    public Graph(){
        nodes = new ArrayList<Node>();
    }

    public Graph(String name){
        this();
        this.name = name;
    }

    /**
     * Calculates if there's any node that doesn't have any dependency and that is not part of any dependency
     * @return the list of isolated nodes
     */
    public List<Node> getIsolatedNodes(){
        List<Node> isolatedNodes = new ArrayList<Node>();

        for(Node node : this.nodes){
            if(node.isIsolated()){

                boolean isADependency = false;
                for(Node nodeBis : this.nodes){
                    if(!nodeBis.equals(node) && nodeBis.getDependencies() != null && !nodeBis.getDependencies().isEmpty()){
                        for(Node dependency : nodeBis.getDependencies()){
                            isADependency = isADependency || dependency.equals(node);
                        }
                    }
                }
                if (!isADependency){
                    isolatedNodes.add(node);
                }
            }
        }
        return isolatedNodes;
    }

    /**
     * Static method that inverts a graph (i.e. inverts every dependency
     * @param graph
     * @return the newly created inverted graph
     */
    public static Graph invertGraph(Graph graph){
        Graph invertedGraph = new Graph("Inverted Graph");

        if(graph != null){
            for(Node currentNode : graph.getNodes()){
                if(currentNode.getDependencies() != null){
                    for(Node dependency : currentNode.getDependencies()){
                        //If we find a dependency of currentNode in our invertedGraph, we alter it with a new dependency (currentNode).
                        //If if we don't have it yet, we add it to the graph
                        Node found = invertedGraph.isNodePresent(dependency);
                        Node dependencyToAdd = new Node(currentNode);
                        if(found == null){
                            Node toAdd = new Node(dependency);
                            toAdd.getDependencies().add(dependencyToAdd);
                            invertedGraph.getNodes().add(toAdd);
                        } else {
                            found.getDependencies().add(dependencyToAdd);
                        }
                    }
                }

                //Here we treat the case of currentNode in itself
                Node found = invertedGraph.isNodePresent(currentNode);
                if(found == null){
                    Node nodeToAdd = new Node(currentNode);
                    invertedGraph.getNodes().add(nodeToAdd);
                }
            }
        }

        return invertedGraph;
    }

    /**
     * Checks if a node is present in the graph instance
     * @param nodeToFind
     * @return the node if present, null if not
     */
    public Node isNodePresent(Node nodeToFind){
        Node present = null;

        for(Node node : this.getNodes()){
            if(node.equals(nodeToFind)){
                present = node;
                break;
            }
        }

        return present;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "name='" + name + '\'' +
                ", nodes=" + nodes +
                '}';
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
