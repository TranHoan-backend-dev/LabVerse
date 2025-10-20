package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Users_Team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTeam {
    @Id
    private String id; // UUID for join row

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "role")
    private String role;
}