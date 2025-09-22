package com.foodorder.backend.user.repository;

import com.foodorder.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Tìm User theo ID với Role được fetch sẵn để tránh lỗi lazy loading
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.id = :userId")
    Optional<User> findUserWithRoleById(@Param("userId") Long userId);

    /**
     * Tìm User theo username với Role được fetch sẵn để tránh lỗi lazy loading
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username")
    Optional<User> findUserWithRoleByUsername(@Param("username") String username);

    /**
     * Tìm tất cả users theo role code để gửi thông báo
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE r.code = :roleCode")
    List<User> findByRole(@Param("roleCode") String roleCode);

    /**
     * Lấy danh sách tất cả nhân viên (ROLE_STAFF) đang hoạt động
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE r.code = 'ROLE_STAFF' AND u.isActive = true")
    List<User> findActiveStaffUsers();

    /**
     * Lấy danh sách tất cả admin (ROLE_ADMIN) đang hoạt động
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE r.code = 'ROLE_ADMIN' AND u.isActive = true")
    List<User> findActiveAdminUsers();
}
