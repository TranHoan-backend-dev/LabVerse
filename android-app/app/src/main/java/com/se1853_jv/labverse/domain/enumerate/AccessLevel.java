package com.se1853_jv.labverse.domain.enumerate;

/**
 * Access levels for collection members
 * - READ_ONLY: Student/Intern - chỉ có thể xem, không thể thêm papers hoặc update status
 * - CONTRIBUTOR: Researcher - có thể thêm papers và update status của chính mình
 * - AUTHOR: PI/Lab Head - full access, có thể quản lý collection và assign priorities
 */
public enum AccessLevel {
    READ_ONLY,    // Student/Intern - chỉ xem
    CONTRIBUTOR,  // Researcher - có thể thêm papers và update status
    AUTHOR        // PI - full access

}

