package com.example.hremp;

public class HrLeaveModel {
    private String leaveId;
    private String status;
    private String leavetype;
    private String startDate;
    private String endDate;
    private String reason;
    private String file;

    public HrLeaveModel(String leaveId, String status, String leavetype, String startDate, String endDate, String reason, String file) {
        this.leaveId = leaveId;
        this.status = status;
        this.leavetype = leavetype;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.file = file;
    }

    // Add getters and setters if needed
}
