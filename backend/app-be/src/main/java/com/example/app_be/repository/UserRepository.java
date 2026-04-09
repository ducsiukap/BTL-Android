package com.example.app_be.repository;

import com.example.app_be.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByEmailNotIgnoreCase(String email);

    @Query("""
                SELECT u FROM User u 
                WHERE u.email <> :email 
                    AND (
                        :query IS NULL OR
                        u.fullName LIKE CONCAT('%', :query, '%')
                        OR 
                        u.email LIKE CONCAT('%', :query, '%')
                    )
            """)
    List<User> searchUserExcludeCurrentUser(
            @Param("email") String currentUserEmail,
            @Param("query") String query
    );
}
