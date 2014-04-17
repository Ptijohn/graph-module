graph-module
============

A simple graph module. It scans a repository for files containing artifacts with dependencies (JSON). It loads thoses dependencies in Java objects, inserts those artifacts(nodes) and dependencies(relationships) into an embedded Neo4J DB.
It then performs a few operations on the graph -> traversing it in one way or the other, merging an updated node into the graph, ...

json-format
============

Each node is, for now, stored in separate files. It contains some information about himself, and about his dependencies.
In fact, each dependency is a complete node, except that it does not contain its own dependencies.

You can see here the JSON format of a node:

    {
        "org": "zenika",
        "name": "D",
        "status": "RELEASE",
        "version": "1.0.0",
        "dependencies": [
            {
                "org": "zenika",
                "name": "C",
                "version": "1.0.0",
                "status": "RELEASE"
            }
        ]
        
    }

properties
============

There's a properties file in resources. You can edit there the directory in which you have stored node files.
There's a node directory committed in resources, so you can use that to test.

program
============

You can have a look at Main.java to see how this program works.
For more details, please have a look at Artifact, Graph and ParserUtil classes.

neo4j
============

Now including neo4j to manage our nodes. You can have a look at Main.java to see how we can go through our graph from one node or another, in whatever direction we want. (See commented code)

merge
============
Since I didn't find any neo4J built-in method to merge a node, I created my own method.
I don't think it's really well written (far too many loops to my taste), i'll try to better it shortly.
But, despite its construction, the function does what we want it to do, it merges a node in the graph if the node already exists, meaning updating its relationships (adding/deleting new/old dependencies). It also adds the node if it didn't exist in the graph before.

next
============
Two things to do on the roadmap :
* Create a service that, if the databse is already populated, gets the nodes from there and not from the files present in our directories
