package com.mentorlink.modules.dashboard.service;

import com.mentorlink.common.exception.ApiException;
import com.mentorlink.modules.dashboard.dto.*;
import com.mentorlink.modules.faculty.entity.FacultyProfile;
import com.mentorlink.modules.faculty.repository.FacultyProfileRepository;
import com.mentorlink.modules.groups.entity.Group;
import com.mentorlink.modules.groups.repository.GroupRepository;
import com.mentorlink.modules.projects.entity.Project;
import com.mentorlink.modules.students.entity.StudentProfile;
import com.mentorlink.modules.students.repository.StudentProfileRepository;
import com.mentorlink.modules.users.UserRepository;
import com.mentorlink.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final GroupRepository groupRepository;

    public StudentProfileDto getStudentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
        if (!user.getRoles().contains("STUDENT")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not a student");
        }
        StudentProfile sp = studentProfileRepository.findByUser_Id(user.getId()).orElse(null);
        return toStudentProfileDto(user, sp);
    }

    public FacultyProfileDto getFacultyProfile(String email) {
        FacultyProfile fp = facultyProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Faculty not found"));
        User user = fp.getUser();
        return FacultyProfileDto.builder()
                .facultyId(fp.getId())
                .userId(user != null ? user.getId() : null)
                .fullName(fp.getName() != null ? fp.getName() : (user != null ? user.getFullName() : ""))
                .email(fp.getEmail())
                .department(fp.getDepartment())
                .expertise(fp.getExpertise())
                .phoneNumber(fp.getPhoneNumber())
                .bio(fp.getBio())
                .profilePictureUrl(fp.getProfilePictureUrl() != null ? fp.getProfilePictureUrl() : (user != null ? user.getProfilePictureUrl() : null))
                .currentLoad(fp.getCurrentLoad())
                .maxGroups(fp.getMaxGroups())
                .available(fp.getCurrentLoad() < fp.getMaxGroups())
                .build();
    }

    public AdminProfileDto getAdminProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
        if (!user.getRoles().contains("ADMIN")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not an admin");
        }
        return AdminProfileDto.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .role("ADMIN")
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    public StudentProfileDto updateStudentProfile(String email, ProfileUpdateDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
        if (!user.getRoles().contains("STUDENT")) throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not a student");

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getContactNumber() != null) user.setContactNumber(dto.getContactNumber());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getSkills() != null) user.setSkills(new java.util.HashSet<>(dto.getSkills()));
        if (dto.getInterests() != null) user.setInterests(new java.util.HashSet<>(dto.getInterests()));
        user = userRepository.save(user);

        StudentProfile sp = studentProfileRepository.findByUser_Id(user.getId()).orElse(StudentProfile.builder().user(user).build());
        if (dto.getRollNumber() != null) sp.setRollNumber(dto.getRollNumber());
        if (dto.getDepartment() != null) sp.setDepartment(dto.getDepartment());
        if (dto.getYearOfStudy() != null) sp.setYearOfStudy(dto.getYearOfStudy());
        if (dto.getContactNumber() != null) sp.setContactNumber(dto.getContactNumber());
        if (dto.getBio() != null) sp.setBio(dto.getBio());
        sp = studentProfileRepository.save(sp);

        return toStudentProfileDto(user, sp);
    }

    public FacultyProfileDto updateFacultyProfile(String email, ProfileUpdateDto dto) {
        FacultyProfile fp = facultyProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Faculty not found"));
        User user = fp.getUser();
        if (user != null) {
            if (dto.getFullName() != null) user.setFullName(dto.getFullName());
            if (dto.getContactNumber() != null) user.setContactNumber(dto.getContactNumber());
            if (dto.getBio() != null) user.setBio(dto.getBio());
            userRepository.save(user);
        }
        if (dto.getFullName() != null) fp.setName(dto.getFullName());
        if (dto.getDepartment() != null) fp.setDepartment(dto.getDepartment());
        if (dto.getExpertise() != null) fp.setExpertise(dto.getExpertise());
        if (dto.getPhoneNumber() != null) fp.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getBio() != null) fp.setBio(dto.getBio());
        fp = facultyProfileRepository.save(fp);
        return getFacultyProfile(email);
    }

    public AdminProfileDto updateAdminProfile(String email, ProfileUpdateDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
        if (!user.getRoles().contains("ADMIN")) throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not an admin");
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getContactNumber() != null) user.setContactNumber(dto.getContactNumber());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        user = userRepository.save(user);
        return getAdminProfile(email);
    }

    public void setProfilePictureUrl(String email, String url) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        if (user.getRoles().contains("STUDENT")) {
            studentProfileRepository.findByUser_Id(user.getId()).ifPresent(sp -> {
                sp.setProfilePictureUrl(url);
                studentProfileRepository.save(sp);
            });
        }
        if (user.getFacultyProfile() != null) {
            user.getFacultyProfile().setProfilePictureUrl(url);
            facultyProfileRepository.save(user.getFacultyProfile());
        }
    }

    private StudentProfileDto toStudentProfileDto(User user, StudentProfile sp) {
        MentorSummaryDto mentor = null;
        GroupSummaryDto groupSummary = null;
        List<Group> groups = groupRepository.findByMembersContaining(user);
        if (!groups.isEmpty()) {
            Group g = groups.get(0);
            Project p = g.getProject();
            if (p != null && p.getMentor() != null) {
                FacultyProfile m = p.getMentor();
                mentor = MentorSummaryDto.builder()
                        .facultyId(m.getId())
                        .name(m.getName())
                        .email(m.getEmail())
                        .department(m.getDepartment())
                        .expertise(m.getExpertise())
                        .build();
            }
            List<MemberSummaryDto> members = g.getMembers().stream()
                    .map(m -> MemberSummaryDto.builder()
                            .userId(m.getId())
                            .fullName(m.getFullName())
                            .email(m.getEmail())
                            .isLeader(g.getLeader() != null && g.getLeader().getId().equals(m.getId()))
                            .build())
                    .collect(Collectors.toList());
            groupSummary = GroupSummaryDto.builder()
                    .groupId(g.getId())
                    .name(g.getName())
                    .projectId(p != null ? p.getId() : null)
                    .projectTitle(p != null ? p.getTitle() : null)
                    .leaderId(g.getLeader() != null ? g.getLeader().getId() : null)
                    .leaderName(g.getLeader() != null ? g.getLeader().getFullName() : null)
                    .members(members)
                    .memberCount(members.size())
                    .build();
        }
        String picUrl = (sp != null && sp.getProfilePictureUrl() != null) ? sp.getProfilePictureUrl() : user.getProfilePictureUrl();
        String contact = (sp != null && sp.getContactNumber() != null) ? sp.getContactNumber() : user.getContactNumber();
        String bio = (sp != null && sp.getBio() != null) ? sp.getBio() : user.getBio();
        return StudentProfileDto.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .rollNumber(sp != null ? sp.getRollNumber() : null)
                .department(sp != null ? sp.getDepartment() : null)
                .yearOfStudy(sp != null ? sp.getYearOfStudy() : null)
                .contactNumber(contact)
                .bio(bio)
                .profilePictureUrl(picUrl)
                .skills(user.getSkills() != null ? new ArrayList<>(user.getSkills()) : List.of())
                .interests(user.getInterests() != null ? new ArrayList<>(user.getInterests()) : List.of())
                .assignedMentor(mentor)
                .group(groupSummary)
                .build();
    }
}
