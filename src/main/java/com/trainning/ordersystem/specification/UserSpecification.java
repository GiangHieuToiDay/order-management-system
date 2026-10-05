package com.trainning.ordersystem.specification;

import com.trainning.ordersystem.dto.request.auth.RegisterCustomerRequest;
import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.entity.enums.UserStatus;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserSpecification {

    private final UserRepository userRepository;

    public void validateRegistration(RegisterCustomerRequest request) {
        validateEmailNotExists(request.getEmail());
    }

    public void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AppException(
                    ErrorCode.EMAIL_ALREADY_EXISTS,
                    "Email '" + email + "' đã được sử dụng"
            );
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(
                        ErrorCode.INVALID_CREDENTIALS,
                        "Email hoặc mật khẩu không chính xác"
                ));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng với id = " + userId
                ));
    }

    public void validateLoginCredentials(User user, String rawPassword) {
        if (!user.getPassword().equals(rawPassword)) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Email hoặc mật khẩu không chính xác");
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    public void validatePasswordChange(User user, String oldPassword, String newPassword, String confirmPassword) {
        if (!user.getPassword().equals(oldPassword)) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Mật khẩu hiện tại không chính xác");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Mật khẩu xác nhận không khớp với mật khẩu mới");
        }
    }

    public void validateStatusUpdate(User targetUser, User currentUser, UserStatus newStatus) {
        if (currentUser != null && currentUser.getId().equals(targetUser.getId()) && newStatus == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.CANNOT_LOCK_CURRENT_USER);
        }
    }
}