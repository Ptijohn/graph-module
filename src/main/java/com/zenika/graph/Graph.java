package com.zenika.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ptijohn on 11/04/14.
 */
public class Graph {
    String name;
    private List<Artifact> artifacts;

    public Graph(){
        artifacts = new ArrayList<Artifact>();
    }

    public Graph(String name){
        this();
        this.name = name;
    }

    /**
     * Calculates if there's any node that doesn't have any dependency and that is not part of any dependency
     * @return the list of isolated artifacts
     */
    public List<Artifact> getIsolatedNodes(){
        List<Artifact> isolatedArtifacts = new ArrayList<Artifact>();

        for(Artifact artifact : this.artifacts){
            if(artifact.hasDependencies()){

                boolean isADependency = false;
                for(Artifact artifactBis : this.artifacts){
                    if(!artifactBis.equals(artifact) && artifactBis.getDependencies() != null && !artifactBis.getDependencies().isEmpty()){
                        for(Artifact dependency : artifactBis.getDependencies()){
                            isADependency = isADependency || dependency.equals(artifact);
                        }
                    }
                }
                if (!isADependency){
                    isolatedArtifacts.add(artifact);
                }
            }
        }
        return isolatedArtifacts;
    }

    /**
     * Static method that inverts a graph (i.e. inverts every dependency
     * @param graph
     * @return the newly created inverted graph
     */
    public static Graph invertGraph(Graph graph){
        Graph invertedGraph = new Graph("Inverted Graph");

        if(graph != null){
            for(Artifact currentArtifact : graph.getArtifacts()){
                if(currentArtifact.getDependencies() != null){
                    for(Artifact dependency : currentArtifact.getDependencies()){
                        //If we find a dependency of currentArtifact in our invertedGraph, we alter it with a new dependency (currentArtifact).
                        //If if we don't have it yet, we add it to the graph
                        Artifact found = invertedGraph.isNodePresent(dependency);
                        Artifact dependencyToAdd = new Artifact(currentArtifact);
                        if(found == null){
                            Artifact toAdd = new Artifact(dependency);
                            toAdd.getDependencies().add(dependencyToAdd);
                            invertedGraph.getArtifacts().add(toAdd);
                        } else {
                            found.getDependencies().add(dependencyToAdd);
                        }
                    }
                }

                //Here we treat the case of currentArtifact in itself
                Artifact found = invertedGraph.isNodePresent(currentArtifact);
                if(found == null){
                    Artifact artifactToAdd = new Artifact(currentArtifact);
                    invertedGraph.getArtifacts().add(artifactToAdd);
                }
            }
        }

        return invertedGraph;
    }

    /**
     * Checks if a node is present in the graph instance
     * @param artifactToFind
     * @return the node if present, null if not
     */
    public Artifact isNodePresent(Artifact artifactToFind){
        Artifact present = null;

        for(Artifact artifact : this.getArtifacts()){
            if(artifact.equals(artifactToFind)){
                present = artifact;
                break;
            }
        }

        return present;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "name='" + name + '\'' +
                ", artifacts=" + artifacts +
                '}';
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
