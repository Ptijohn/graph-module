package com.zenika.graph;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public static List<Artifact> scanDirectory(String directoryName){
        File directory = new File(directoryName);

        //Graph graph = new Graph("Graph");
        List<Artifact> artifacts = new ArrayList<Artifact>();

        if(!directory.isDirectory()){
            System.out.println("Not a directory");
            return null;
        }

        File[] files = directory.listFiles();

        for(File file : files){
            artifacts.add(parseNode(file.getAbsolutePath()));
        }
        return artifacts;
    }
}