package com.example.demoicsa;

import android.app.Application;

import com.example.demoicsa.approval.Approval;

import java.util.List;

public class GlobalApplication extends Application {
    private String username = "";

    public int approvalIdToSave = -1;

    private List<Approval> directionApprovals = null;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void eraseUsername(){
        this.username = null;
    }

    public List<Approval> getDirectionApprovals() {
        return directionApprovals;
    }

    public void setDirectionApprovals(List<Approval> directionApprovals) {
        this.directionApprovals = directionApprovals;
    }

    public void eraseDirectionApprovals(){ this.directionApprovals = null; }
}
