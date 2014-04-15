package com.zenika.graph;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Created by ptijohn on 11/04/14.
 */
public class ParserUtil {

    /**
     * Parses a node file and returns the node object corresponding
     * @param fileName
     * @return
     */
    public static Artifact parseNode(String fileName){
        Artifact artifact = null;

        //2. Convert JSON to Java object
        try{
            ObjectMapper mapper = new ObjectMapper();
            artifact = mapper.readValue(new File(fileName), Artifact.class);
        } catch(IOException e){
            e.printStackTrace();
        }

        return artifact;
    }


    /**
     * Scans a directory composed only of node files.
     * @param directoryName
     * @return
     */
    public static Graph scanDirectory(String directoryName){
        File directory = new File(directoryName);

        Graph graph = new Graph("Graph");

        if(!directory.isDirectory()){
            System.out.println("Not a directory");
            return null;
        }

        File[] files = directory.listFiles();

        for(File file : files){
            graph.getArtifacts().add(parseNode(file.getAbsolutePath()));
        }
        return graph;
    }
}