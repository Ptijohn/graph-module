package com.zenika.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a node
 * Created by ptijohn on 11/04/14.
 */
public class Artifact implements Serializable{
    private String org;
    private String status;
    private String version;
    private String name;
    private List<Artifact> dependencies;

    public Artifact(){
        dependencies = new ArrayList<Artifact>();
    }

    public Artifact(String org, String status, String version, String name){
        this();
        this.org = org;
        this.status = status;
        this.version = version;
        this.name = name;
    }

    public Artifact(Artifact artifact){
        this(artifact.getOrg(), artifact.getStatus(), artifact.getVersion(), artifact.getName());
    }

    public boolean hasDependencies(){
        return (this.dependencies == null || this.dependencies.isEmpty());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Artifact> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Artifact> dependencies) {
        this.dependencies = dependencies;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artifact artifact = (Artifact) o;

        if (name != null ? !name.equals(artifact.name) : artifact.name != null) return false;
        if (org != null ? !org.equals(artifact.org) : artifact.org != null) return false;
        if (status != null ? !status.equals(artifact.status) : artifact.status != null) return false;
        if (version != null ? !version.equals(artifact.version) : artifact.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = org != null ? org.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "org='" + org + '\'' +
                ", status='" + status + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", dependencies=" + dependencies +
                '}';
    }
}
