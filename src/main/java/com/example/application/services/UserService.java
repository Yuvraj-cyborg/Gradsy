package com.example.application.services;

import com.example.application.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private StudentProfileRepository studentProfileRepository;
    
    @Autowired
    private TeacherProfileRepository teacherProfileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    // User methods
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Transactional
    public User registerUser(String username, String rawPassword, String email, UserRole role, String firstName, String lastName, String subjectArea) throws Exception {
        if (userRepository.findByUsername(username) != null) {
            throw new Exception("Username already exists: " + username);
        }
        if (userRepository.findByEmail(email) != null) {
            throw new Exception("Email already registered: " + email);
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword)); // Encode password
        newUser.setEmail(email);
        newUser.setRole(role);
        // newUser.setCreatedAt is handled by default value

        User savedUser = userRepository.save(newUser);

        // Create corresponding profile
        if (role == UserRole.TEACHER) {
            TeacherProfile teacherProfile = new TeacherProfile();
            teacherProfile.setUser(savedUser);
            teacherProfile.setFirstName(firstName);
            teacherProfile.setLastName(lastName);
            teacherProfile.setSubjectArea(subjectArea);
            teacherProfileRepository.save(teacherProfile);
        } else if (role == UserRole.STUDENT) {
            StudentProfile studentProfile = new StudentProfile();
            studentProfile.setUser(savedUser);
            studentProfile.setFirstName(firstName);
            studentProfile.setLastName(lastName);
            // studentProfile.setGradeLevel(null); // Can be set later
            studentProfileRepository.save(studentProfile);
        } else {
             // Handle other roles or throw an error if only Teacher/Student are expected
             System.out.println("Warning: Registered user with unhandled role for profile creation: " + role);
        }

        return savedUser;
    }
    
    // Student methods
    public List<StudentProfile> findAllStudents() {
        return studentProfileRepository.findAll();
    }
    
    public StudentProfile findStudentById(Long id) {
        return studentProfileRepository.findById(id).orElse(null);
    }
    
    public StudentProfile findStudentByUser(User user) {
        return studentProfileRepository.findByUser(user);
    }
    
    public StudentProfile saveStudent(StudentProfile student) {
        return studentProfileRepository.save(student);
    }
    
    public StudentProfile createStudentProfile(User user, String firstName, String lastName, Integer gradeLevel) {
        StudentProfile student = new StudentProfile();
        student.setUser(user);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setGradeLevel(gradeLevel);
        return studentProfileRepository.save(student);
    }
    
    // Teacher methods
    public List<TeacherProfile> findAllTeachers() {
        return teacherProfileRepository.findAll();
    }
    
    public List<String> findAllDistinctSubjectAreas() {
        return teacherProfileRepository.findDistinctSubjectAreas();
    }
    
    public TeacherProfile findTeacherById(Long id) {
        return teacherProfileRepository.findById(id).orElse(null);
    }
    
    public TeacherProfile findTeacherByUser(User user) {
        return teacherProfileRepository.findByUser(user);
    }
    
    public TeacherProfile saveTeacher(TeacherProfile teacher) {
        return teacherProfileRepository.save(teacher);
    }
    
    public TeacherProfile createTeacherProfile(User user, String firstName, String lastName, String subjectArea) {
        TeacherProfile teacher = new TeacherProfile();
        teacher.setUser(user);
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setSubjectArea(subjectArea);
        return teacherProfileRepository.save(teacher);
    }
    
    // Helper method to determine if user is a student
    public boolean isStudent(User user) {
        return findStudentByUser(user) != null;
    }
    
    // Helper method to determine if user is a teacher
    public boolean isTeacher(User user) {
        return findTeacherByUser(user) != null;
    }
} 