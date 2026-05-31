package com.mentorlink.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.*;

@Component
public class ExcelProcessor {

    /**
     * Parse student Excel: columns [Email, FullName, RollNumber, Department, YearOfStudy, Skills, Password]
     * Skills: comma/semicolon separated, used for recommender similarity grouping.
     * Password is optional; if empty, a random password is generated (user must reset).
     */
    public List<StudentUploadRow> parseStudents(MultipartFile file) throws Exception {
        List<StudentUploadRow> rows = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String email = getCellString(row, 0);
                if (email == null || email.isBlank()) continue;
                rows.add(StudentUploadRow.builder()
                        .email(email.trim())
                        .fullName(getCellString(row, 1))
                        .rollNumber(getCellString(row, 2))
                        .department(getCellString(row, 3))
                        .yearOfStudy(getCellInt(row, 4))
                        .skills(parseSkills(getCellString(row, 5)))
                        .password(getCellString(row, 6))
                        .build());
            }
        }
        return rows;
    }

    /** Parse comma/semicolon separated skills into a Set (used for recommender similarity). */
    private static java.util.Set<String> parseSkills(String raw) {
        if (raw == null || raw.isBlank()) return new java.util.HashSet<>();
        return new java.util.HashSet<>(Arrays.asList(raw.split("[,;]+")).stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .toList());
    }

    /**
     * Parse leftover students Excel for auto-group allocation.
     * Columns: [Email] or [Email, RollNumber]. Either column can identify the student.
     * Used after group formation deadline: admin uploads list of students who haven't formed groups.
     */
    public List<com.mentorlink.util.LeftoverStudentRow> parseLeftoverStudents(MultipartFile file) throws Exception {
        List<com.mentorlink.util.LeftoverStudentRow> rows = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String email = getCellString(row, 0);
                String rollNumber = getCellString(row, 1);
                if ((email == null || email.isBlank()) && (rollNumber == null || rollNumber.isBlank())) continue;
                rows.add(com.mentorlink.util.LeftoverStudentRow.builder()
                        .email(email != null ? email.trim() : null)
                        .rollNumber(rollNumber != null ? rollNumber.trim() : null)
                        .build());
            }
        }
        return rows;
    }

    /**
     * Parse faculty Excel: columns [Email, FullName, Department, Expertise, MaxGroups, Password]
     * Password is optional; if empty, a random password is generated (user must reset).
     */
    public List<FacultyUploadRow> parseFaculty(MultipartFile file) throws Exception {
        List<FacultyUploadRow> rows = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String email = getCellString(row, 0);
                if (email == null || email.isBlank()) continue;
                rows.add(FacultyUploadRow.builder()
                        .email(email.trim())
                        .fullName(getCellString(row, 1))
                        .department(getCellString(row, 2))
                        .expertise(getCellString(row, 3))
                        .maxGroups(Optional.ofNullable(getCellInt(row, 4)).filter(v -> v > 0).orElse(3))
                        .password(getCellString(row, 5))
                        .build());
            }
        }
        return rows;
    }

    private String getCellString(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null) return null;
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue();
            case NUMERIC -> String.valueOf((long) c.getNumericCellValue());
            default -> null;
        };
    }

    /**
     * Generate Excel for leftover students. Columns: Email, RollNumber, FullName, Department, YearOfStudy, Skills
     * Cols 0,1 match parseLeftoverStudents for re-upload.
     */
    public byte[] generateLeftoverStudentsExcel(List<Map<String, Object>> students) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Leftover Students");
            Row header = sheet.createRow(0);
            String[] headers = {"Email", "RollNumber", "FullName", "Department", "YearOfStudy", "Skills"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (Map<String, Object> s : students) {
                Row row = sheet.createRow(rowNum++);
                setCell(row, 0, s.get("email"));
                setCell(row, 1, s.get("rollNumber"));
                setCell(row, 2, s.get("fullName"));
                setCell(row, 3, s.get("department"));
                setCell(row, 4, s.get("yearOfStudy"));
                Object skills = s.get("skills");
                String skillsStr = null;
                if (skills instanceof Collection<?> c) {
                    skillsStr = c.stream().map(Object::toString).collect(java.util.stream.Collectors.joining(", "));
                } else if (skills != null) {
                    skillsStr = skills.toString();
                }
                setCell(row, 5, skillsStr);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Generate Excel for faculty with available slots. Columns: Email, FullName, Department, Expertise, MaxGroups, CurrentLoad, AvailableSlots
     */
    public byte[] generateFacultyExcel(List<Map<String, Object>> faculty) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Faculty");
            Row header = sheet.createRow(0);
            String[] headers = {"Email", "FullName", "Department", "Expertise", "MaxGroups", "CurrentLoad", "AvailableSlots"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (Map<String, Object> f : faculty) {
                Row row = sheet.createRow(rowNum++);
                setCell(row, 0, f.get("email"));
                setCell(row, 1, f.get("fullName"));
                setCell(row, 2, f.get("department"));
                setCell(row, 3, f.get("expertise"));
                setCell(row, 4, f.get("maxGroups"));
                setCell(row, 5, f.get("currentLoad"));
                Object avail = f.get("availableSlots");
                setCell(row, 6, avail);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Generate Excel for matrix-factorization auto-group output.
     * Columns: GroupID, Student1, Student2, Student3, FacultyName, SimilarityScore, AlgorithmUsed
     */
    public byte[] generateAutoAllocationGroupsExcel(List<Map<String, Object>> groups) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Auto Allocation Groups");
            Row header = sheet.createRow(0);
            String[] headers = {"GroupID", "Student1", "Student2", "Student3", "FacultyName", "SimilarityScore", "AlgorithmUsed"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (Map<String, Object> group : groups) {
                Row row = sheet.createRow(rowNum++);
                setCell(row, 0, group.get("groupId"));
                setCell(row, 1, group.get("student1"));
                setCell(row, 2, group.get("student2"));
                setCell(row, 3, group.get("student3"));
                setCell(row, 4, group.get("facultyName"));
                setCell(row, 5, group.get("similarityScore"));
                setCell(row, 6, group.get("algorithmUsed"));
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void setCell(Row row, int col, Object value) {
        Cell c = row.createCell(col);
        if (value == null) return;
        if (value instanceof Number) {
            c.setCellValue(((Number) value).doubleValue());
        } else {
            c.setCellValue(value.toString());
        }
    }

    private Integer getCellInt(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null) return null;
        return switch (c.getCellType()) {
            case NUMERIC -> (int) c.getNumericCellValue();
            case STRING -> {
                try {
                    yield Integer.parseInt(c.getStringCellValue());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    @lombok.Data
    @lombok.Builder
    public static class StudentUploadRow {
        private String email;
        private String fullName;
        private String rollNumber;
        private String department;
        private Integer yearOfStudy;
        private java.util.Set<String> skills;  // for recommender similarity; comma/semicolon in Excel
        private String password;  // optional; if provided, user can login directly
    }

    @lombok.Data
    @lombok.Builder
    public static class FacultyUploadRow {
        private String email;
        private String fullName;
        private String department;
        private String expertise;
        private int maxGroups;
        private String password;  // optional; if provided, user can login directly
    }
}
