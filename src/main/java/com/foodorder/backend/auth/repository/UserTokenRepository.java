package com.foodorder.backend.auth.repository;

import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.auth.entity.UserToken;
import com.foodorder.backend.auth.entity.UserTokenType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByTokenAndUsedFalseAndType(String token, UserTokenType type);

    @Modifying
    @Transactional
    @Query("UPDATE UserToken t SET t.used = true WHERE t.user = :user AND t.type = :type")
    void invalidateAllByUserAndType(@Param("user") User user, @Param("type") UserTokenType type);

    @Query("SELECT COUNT(t) FROM UserToken t WHERE t.user.email = :email AND t.type = :type AND t.createdAt >= :since")
    long countRecentTokens(@Param("email") String email, @Param("type") UserTokenType type, @Param("since") LocalDateTime since);
}

