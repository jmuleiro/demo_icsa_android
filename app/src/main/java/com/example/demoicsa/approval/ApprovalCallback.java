package com.example.demoicsa.approval;

import java.util.List;

public interface ApprovalCallback {
    void run(List<Approval> approvals);
}
