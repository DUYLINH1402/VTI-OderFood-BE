package com.foodorder.backend.user.repository;

import com.foodorder.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Đếm số user theo role code
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.code = :roleCode")
    long countByRoleCode(@Param("roleCode") String roleCode);

    /**
     * Đếm số user mới đăng ký trong khoảng thời gian
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    long countNewUsersByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Đếm số user mới theo role trong khoảng thời gian
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.code = :roleCode AND u.createdAt >= :startDate AND u.createdAt <= :endDate")
    long countNewUsersByRoleAndDateRange(@Param("roleCode") String roleCode, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Lấy danh sách user mới đăng ký gần đây
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE r.code = :roleCode ORDER BY u.createdAt DESC")
    List<User> findRecentUsersByRole(@Param("roleCode") String roleCode, org.springframework.data.domain.Pageable pageable);

    /**
     * Tìm kiếm users với filter cho admin
     * Hỗ trợ tìm theo keyword (username, email, fullName, phoneNumber), roleCode, isActive
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phoneNumber LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:roleCode IS NULL OR :roleCode = '' OR r.code = :roleCode) AND " +
            "(:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> findAllUsersWithFilters(
            @Param("keyword") String keyword,
            @Param("roleCode") String roleCode,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * Đếm số users với filter
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phoneNumber LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:roleCode IS NULL OR :roleCode = '' OR r.code = :roleCode) AND " +
            "(:isActive IS NULL OR u.isActive = :isActive)")
    long countUsersWithFilters(
            @Param("keyword") String keyword,
            @Param("roleCode") String roleCode,
            @Param("isActive") Boolean isActive);
}
