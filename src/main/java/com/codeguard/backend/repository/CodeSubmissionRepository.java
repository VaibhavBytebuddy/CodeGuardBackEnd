package com.codeguard.backend.repository;

import com.codeguard.backend.model.CodeSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CodeSubmissionRepository extends MongoRepository<CodeSubmission, String> {
    
    List<CodeSubmission> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<CodeSubmission> findByLanguageOrderByCreatedAtDesc(CodeSubmission.Language language);
    
    @Query("{'createdAt': {$gte: ?0}}")
    List<CodeSubmission> findRecentSubmissions(LocalDateTime since);
    
    long countByUserId(String userId);
    
    List<CodeSubmission> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
}