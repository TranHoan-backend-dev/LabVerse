package com.se1853_jv.dto.response;

public class OverviewStatisticsResponse {
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long totalPapers;
    private Long papersThisMonth;
    private Long totalTeams;
    private Long publicTeams;
    private Long privateTeams;
    private Long totalCollections;
    private Long totalReadingLists;

    public OverviewStatisticsResponse() {
    }

    public OverviewStatisticsResponse(Long totalUsers, Long activeUsers, Long inactiveUsers,
                                     Long totalPapers, Long papersThisMonth,
                                     Long totalTeams, Long publicTeams, Long privateTeams,
                                     Long totalCollections, Long totalReadingLists) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
        this.totalPapers = totalPapers;
        this.papersThisMonth = papersThisMonth;
        this.totalTeams = totalTeams;
        this.publicTeams = publicTeams;
        this.privateTeams = privateTeams;
        this.totalCollections = totalCollections;
        this.totalReadingLists = totalReadingLists;
    }

    // Getters and Setters
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(Long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public Long getTotalPapers() {
        return totalPapers;
    }

    public void setTotalPapers(Long totalPapers) {
        this.totalPapers = totalPapers;
    }

    public Long getPapersThisMonth() {
        return papersThisMonth;
    }

    public void setPapersThisMonth(Long papersThisMonth) {
        this.papersThisMonth = papersThisMonth;
    }

    public Long getTotalTeams() {
        return totalTeams;
    }

    public void setTotalTeams(Long totalTeams) {
        this.totalTeams = totalTeams;
    }

    public Long getPublicTeams() {
        return publicTeams;
    }

    public void setPublicTeams(Long publicTeams) {
        this.publicTeams = publicTeams;
    }

    public Long getPrivateTeams() {
        return privateTeams;
    }

    public void setPrivateTeams(Long privateTeams) {
        this.privateTeams = privateTeams;
    }

    public Long getTotalCollections() {
        return totalCollections;
    }

    public void setTotalCollections(Long totalCollections) {
        this.totalCollections = totalCollections;
    }

    public Long getTotalReadingLists() {
        return totalReadingLists;
    }

    public void setTotalReadingLists(Long totalReadingLists) {
        this.totalReadingLists = totalReadingLists;
    }
}

