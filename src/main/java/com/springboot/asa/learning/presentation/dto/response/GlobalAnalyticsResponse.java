package com.springboot.asa.learning.presentation.dto.response;

import java.util.List;

public record GlobalAnalyticsResponse(
        long totalStudents,
        long activePrograms,
        double completionRate,
        long assessmentsCompleted,
        List<ProgramEnrollmentStat> programStats
) {
    public record ProgramEnrollmentStat(
            String programId,
            String programName,
            long enrollmentCount
    ) {}
}
