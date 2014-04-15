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
    public static Node parseNode(String fileName){
        Node node = null;

        //2. Convert JSON to Java object
        try{
            ObjectMapper mapper = new ObjectMapper();
            node = mapper.readValue(new File(fileName), Node.class);
        } catch(IOException e){
            e.printStackTrace();
        }

        return node;
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
            graph.getNodes().add(parseNode(file.getAbsolutePath()));
        }
        return graph;
    }
}