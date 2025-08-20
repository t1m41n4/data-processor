package com.example.studentprocessor.repository;

import com.example.studentprocessor.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentId(Long studentId);

    Page<Student> findByClassName(String className, Pageable pageable);

    boolean existsByStudentId(Long studentId);

    @Query("SELECT DISTINCT s.className FROM Student s ORDER BY s.className")
    List<String> findDistinctClassNames();

    @Query("SELECT s FROM Student s WHERE " +
           "(:studentId IS NULL OR s.studentId = :studentId) AND " +
           "(:className IS NULL OR s.className = :className)")
    Page<Student> findStudentsWithFilters(@Param("studentId") Long studentId,
                                         @Param("className") String className,
                                         Pageable pageable);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.className) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "CAST(s.studentId AS string) LIKE CONCAT('%', :searchTerm, '%') OR " +
           "CAST(s.score AS string) LIKE CONCAT('%', :searchTerm, '%')")
    Page<Student> searchStudentsByTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE " +
           "(:name IS NULL OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:minAge IS NULL OR EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM s.dob) >= :minAge) AND " +
           "(:maxAge IS NULL OR EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM s.dob) <= :maxAge) AND " +
           "(:minScore IS NULL OR s.score >= :minScore) AND " +
           "(:maxScore IS NULL OR s.score <= :maxScore) AND " +
           "(:course IS NULL OR LOWER(s.className) LIKE LOWER(CONCAT('%', :course, '%'))) AND " +
           "(:semester IS NULL OR LOWER(s.className) LIKE LOWER(CONCAT('%', :semester, '%')))")
    Page<Student> findStudentsWithAdvancedFilters(
            @Param("name") String name,
            @Param("email") String email,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            @Param("course") String course,
            @Param("semester") String semester,
            Pageable pageable);

    @Query("SELECT s FROM Student s WHERE " +
           "(:name IS NULL OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:minAge IS NULL OR EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM s.dob) >= :minAge) AND " +
           "(:maxAge IS NULL OR EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM s.dob) <= :maxAge) AND " +
           "(:minScore IS NULL OR s.score >= :minScore) AND " +
           "(:maxScore IS NULL OR s.score <= :maxScore) AND " +
           "(:course IS NULL OR LOWER(s.className) LIKE LOWER(CONCAT('%', :course, '%'))) AND " +
           "(:semester IS NULL OR LOWER(s.className) LIKE LOWER(CONCAT('%', :semester, '%')))")
    List<Student> findStudentsWithAdvancedFiltersForExport(
            @Param("name") String name,
            @Param("email") String email,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            @Param("course") String course,
            @Param("semester") String semester);

    // Additional methods for report statistics and enhanced search
    @Query("SELECT s FROM Student s WHERE " +
           "(:search IS NULL OR " +
           "LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.className) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(s.studentId AS string) LIKE CONCAT('%', :search, '%')) AND " +
           "(:minScore IS NULL OR s.score >= :minScore) AND " +
           "(:maxScore IS NULL OR s.score <= :maxScore) AND " +
           "(:className IS NULL OR s.className = :className)")
    Page<Student> findByAdvancedFilters(
            @Param("search") String search,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            @Param("className") String className,
            Pageable pageable);

    @Query("SELECT AVG(s.score) FROM Student s")
    Double findAverageScore();

    @Query("SELECT MAX(s.score) FROM Student s")
    Integer findMaxScore();

    @Query("SELECT MIN(s.score) FROM Student s")
    Integer findMinScore();

    Long countByScoreGreaterThanEqual(Integer score);

    Long countByScoreBetween(Integer minScore, Integer maxScore);

    // Get top performers (highest scores)
    @Query("SELECT s FROM Student s ORDER BY s.score DESC")
    List<Student> findTopPerformers(Pageable pageable);
}