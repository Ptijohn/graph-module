package com.zenika.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ptijohn on 14/04/14.
 */
public class Main {
    public static void main(String[] args){
        Properties prop = getProperties();
        Graph graph = null;

        if(prop == null){
            graph = ParserUtil.scanDirectory("/home/ptijohn/Documents/Nodes");
        } else {
            graph = ParserUtil.scanDirectory(prop.getProperty(GraphConstants.NODES_DIRECTORY));
        }

        System.out.println(graph);

        Graph invertedGraph = null;

        invertedGraph = Graph.invertGraph(graph);

        System.out.println(invertedGraph);

    }


    /**
     * Gets properties from properties file
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