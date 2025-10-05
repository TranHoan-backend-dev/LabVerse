package com.se1853_jv.labverse.domain.infrastructure;

import androidx.room.TypeConverter;

import com.se1853_jv.labverse.domain.enumerate.Role;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Converter {
    @TypeConverter
    public static String fromRole(Role role) {
        return role == null ? null : role.name();
    }

    @TypeConverter
    public static Role toRole(String name) {
        return name == null ? null : Role.valueOf(name);
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long millis) {
        return millis == null ? null : new Date(millis);
    }

    @TypeConverter
    public static String fromList(List<String> keywords) {
        if (keywords == null) return null;
        return String.join(",", keywords);
    }

    @TypeConverter
    public static List<String> toList(String data) {
        if (data == null || data.isEmpty()) return Collections.emptyList();
        return Arrays.stream(data.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
