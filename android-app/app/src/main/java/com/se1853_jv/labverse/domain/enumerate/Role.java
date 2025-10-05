package com.se1853_jv.labverse.domain.enumerate;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    PRINCIPAL_INVESTIGATOR(List.of("PI")),
    LAB_HEAD(List.of("Lab Head")),
    RESEARCHER(List.of("Postdoc", "PhD")),
    INTERN(List.of("Student", "Intern"));

    private final List<String> value;

    public static List<String> toValue(Role role) {
        return role.getValue();
    }

    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.getValue()
                    .stream().map(String::toLowerCase)
                    .anyMatch(v -> v.equals(value.toLowerCase()))) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

}
