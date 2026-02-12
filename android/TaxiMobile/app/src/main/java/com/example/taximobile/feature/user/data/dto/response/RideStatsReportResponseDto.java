package com.example.taximobile.feature.user.data.dto.response;

import java.util.List;

public class RideStatsReportResponseDto {

    private String targetRole;
    private String scope;
    private Long targetUserId;

    private String from;
    private String to;
    private int days;

    private List<RideStatsPointDto> points;

    private RideStatsTotalsDto totals;
    private RideStatsAveragesDto averages;

    public RideStatsReportResponseDto() {}

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }

    public List<RideStatsPointDto> getPoints() { return points; }
    public void setPoints(List<RideStatsPointDto> points) { this.points = points; }

    public RideStatsTotalsDto getTotals() { return totals; }
    public void setTotals(RideStatsTotalsDto totals) { this.totals = totals; }

    public RideStatsAveragesDto getAverages() { return averages; }
    public void setAverages(RideStatsAveragesDto averages) { this.averages = averages; }
}
