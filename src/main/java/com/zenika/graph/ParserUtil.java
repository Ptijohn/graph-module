package com.zenika.graph;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by ptijohn on 11/04/14.
 */
public class ParserUtil {
    /*public static void main(String[] args){
        parseNode("/home/ptijohn/Documents/jsonData");
    }*/

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