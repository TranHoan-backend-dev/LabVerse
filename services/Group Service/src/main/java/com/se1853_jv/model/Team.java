package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Team")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Team {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    // mappedBy in UserTeam
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTeam> members = new ArrayList<>();
}
