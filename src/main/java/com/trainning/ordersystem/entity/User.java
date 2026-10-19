package com.trainning.ordersystem.entity;

import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@SqlResultSetMapping(
        name = "UserProfileResponseMapping",
        classes = @ConstructorResult(
                targetClass = UserProfileResponse.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "fullName", type = String.class),
                        @ColumnResult(name = "email", type = String.class),
                        @ColumnResult(name = "phone", type = String.class),
                        @ColumnResult(name = "role", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "createdAt", type = LocalDateTime.class),
                        @ColumnResult(name = "customerId", type = Long.class),
                        @ColumnResult(name = "address", type = String.class),
                        @ColumnResult(name = "membershipLevel", type = String.class)
                }
        )
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false, length = 255)
    private String password;

    @Nationalized
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Customer customer;
}
