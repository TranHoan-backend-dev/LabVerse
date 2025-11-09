package com.se1853_jv.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "TeamMembers")
public class TeamMember {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", referencedColumnName = "id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private TeamRole role;

    @Column(name = "joined_date", nullable = false)
    private LocalDate joinedDate;

    public enum TeamRole {
        PI,           // Principal Investigator / Lab Head
        RESEARCHER,   // Postdoc / PhD
        STUDENT       // Student / Intern
    }

    public TeamMember() {
        this.id = UUID.randomUUID().toString();
        this.joinedDate = LocalDate.now();
    }

    public TeamMember(Team team, User user, TeamRole role) {
        this();
        this.team = team;
        this.user = user;
        this.role = role;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TeamRole getRole() {
        return role;
    }

    public void setRole(TeamRole role) {
        this.role = role;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }
}

