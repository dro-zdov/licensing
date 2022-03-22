package com.codesample.licensing.entity;

public class LicenseInfo {
    private long startDate;
    private long endDate;

    public LicenseInfo(long startDate, long endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }
}
