package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
 
@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    TeacherProfile findByUser(User user);

    @Query("SELECT DISTINCT t.subjectArea FROM TeacherProfile t WHERE t.subjectArea IS NOT NULL AND t.subjectArea <> ''")
    List<String> findDistinctSubjectAreas();
} 