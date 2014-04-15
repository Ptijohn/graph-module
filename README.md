graph-module
============

A simple graph module. It scans a repository for files containing nodes with dependencies (JSON). It loads thoses dependencies in Java objects, and then it inverts all dependencies in the graph. 

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
    }

properties
============

There's a properties file in resources. You can edit there the directory in which you have stored node files.
There's a node directory committed in resources, so you can use that to test.
