package com.sabry.muhammed.reposviewer.models;

import java.io.Serializable;

public class GitModel implements Serializable {

    private String name,userName,description,repoURL,ownerURL;
    boolean forkFlag;

    public GitModel(String name,String userName,String description,String repoURL,String ownerURL , boolean flag){
        this.name = name;
        this.userName = userName;
        this.description = description;
        this.repoURL = repoURL;
        this.ownerURL = ownerURL;
        this.forkFlag = flag;
    }

    public String getOwnerURL() {
        return ownerURL;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isForkFlag(){
        return forkFlag;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setForkFlag(boolean forkFlag) {
        this.forkFlag = forkFlag;
    }

    public void setOwnerURL(String ownerURL) {
        this.ownerURL = ownerURL;
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }
}
