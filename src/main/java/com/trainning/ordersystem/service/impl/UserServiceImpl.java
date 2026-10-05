package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.auth.LoginRequest;
import com.trainning.ordersystem.dto.request.auth.RegisterCustomerRequest;
import com.trainning.ordersystem.dto.request.user.ChangePasswordRequest;
import com.trainning.ordersystem.dto.request.user.CreateUserRequest;
import com.trainning.ordersystem.dto.request.user.UpdateProfileRequest;
import com.trainning.ordersystem.dto.response.auth.AuthResponse;
import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.entity.Customer;
import com.trainning.ordersystem.entity.Role;
import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.entity.enums.MembershipLevel;
import com.trainning.ordersystem.entity.enums.UserStatus;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.mapper.UserMapper;
import com.trainning.ordersystem.repository.CustomerRepository;
import com.trainning.ordersystem.repository.UserRepository;
import com.trainning.ordersystem.service.UserService;
import com.trainning.ordersystem.specification.CustomerSpecification;
import com.trainning.ordersystem.specification.RoleSpecification;
import com.trainning.ordersystem.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final UserSpecification userSpecification;
    private final RoleSpecification roleSpecification;
    private final CustomerSpecification customerSpecification;
    private final UserMapper userMapper;


    @Override
    @Transactional
    public UserProfileResponse registerCustomer(RegisterCustomerRequest request) {
        log.info("Khách hàng đăng ký tài khoản mới: email={}", request.getEmail());

        userSpecification.validateRegistration(request);
        Role role = roleSpecification.findRoleByName("CUSTOMER");

        User user = userMapper.toEntity(request);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        Customer savedCustomer = customerRepository.save(
                customerSpecification.tranferCustomerFromUser(savedUser, request.getAddress())
        );

        UserProfileResponse response = userMapper.toProfileResponse(savedUser);
        userMapper.enrichWithCustomer(savedCustomer, response);
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Người dùng đăng nhập: email={}", request.getEmail());

        User user = userSpecification.getUserByEmail(request.getEmail());
        userSpecification.validateLoginCredentials(user, request.getPassword());

        return AuthResponse.builder()
                .accessToken("MOCK_TOKEN_" + user.getEmail()) // TODO: Sinh JWT token thật ở bước Security
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .fullName(user.getFullName())
                .build();
    }


    @Override
    public UserProfileResponse getMyProfile() {
        User currentUser = getCurrentUser();
        return toUserProfileWithCustomerInfo(currentUser);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();

        currentUser.setFullName(request.getFullName());
        currentUser.setPhone(request.getPhone());
        userRepository.save(currentUser);

        customerSpecification.updateAddressIfExists(currentUser.getId(), request.getAddress());

        return toUserProfileWithCustomerInfo(currentUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User currentUser = getCurrentUser();

        userSpecification.validatePasswordChange(
                currentUser,
                request.getOldPassword(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        currentUser.setPassword(request.getNewPassword());
        userRepository.save(currentUser);
        log.info("Người dùng '{}' đã đổi mật khẩu thành công", currentUser.getEmail());
    }


    @Override
    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
        log.info("Admin tạo tài khoản mới: email={}, roleId={}", request.getEmail(), request.getRoleId());

        userSpecification.validateEmailNotExists(request.getEmail());
        Role role = roleSpecification.findRoleById(request.getRoleId());

        User user = userMapper.toEntity(request);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        UserProfileResponse response = userMapper.toProfileResponse(savedUser);
        if ("CUSTOMER".equalsIgnoreCase(role.getName())) {
            Customer savedCustomer = customerRepository.save(
                    customerSpecification.tranferCustomerFromUser(savedUser, null)
            );
            userMapper.enrichWithCustomer(savedCustomer, response);
        }

        return response;
    }

    @Override
    public UserProfileResponse getUserById(Long userId) {
        User user = userSpecification.getUserById(userId);
        return toUserProfileWithCustomerInfo(user);
    }

    @Override
    public PageResponse<UserProfileResponse> getUsers(int page, int size, String keyword, Long roleId, UserStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> usersPage = userRepository.searchUsers(keyword, roleId, status, pageable);

        List<UserProfileResponse> responses = usersPage.getContent().stream()
                .map(this::toUserProfileWithCustomerInfo)
                .toList();

        return PageResponse.<UserProfileResponse>builder()
                .content(responses)
                .pageNumber(usersPage.getNumber())
                .pageSize(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .isFirst(usersPage.isFirst())
                .isLast(usersPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        User user = userSpecification.getUserById(userId);
        userSpecification.validateStatusUpdate(user, getCurrentUserSafe(), status);

        user.setStatus(status);
        userRepository.save(user);
        log.info("Admin đã cập nhật trạng thái người dùng '{}' thành {}", user.getEmail(), status);
    }


    @Override
    public UserProfileResponse getCustomerById(Long customerId) {
        Customer customer = customerSpecification.findCustomerById(customerId);
        UserProfileResponse response = userMapper.toProfileResponse(customer.getUser());
        userMapper.enrichWithCustomer(customer, response);
        return response;
    }

    @Override
    @Transactional
    public UserProfileResponse updateCustomerMembership(Long customerId, MembershipLevel membershipLevel) {
        Customer customer = customerSpecification.findCustomerById(customerId);
        customer.setMembershipLevel(membershipLevel);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Đã cập nhật hạng thành viên của khách hàng '{}' thành {}", customer.getUser().getEmail(), membershipLevel);

        UserProfileResponse response = userMapper.toProfileResponse(savedCustomer.getUser());
        userMapper.enrichWithCustomer(savedCustomer, response);
        return response;
    }

    private UserProfileResponse toUserProfileWithCustomerInfo(User user) {
        UserProfileResponse response = userMapper.toProfileResponse(user);
        customerSpecification.findCustomerByUserId(user.getId())
                .ifPresent(customer -> userMapper.enrichWithCustomer(customer, response));
        return response;
    }

    private User getCurrentUser() {
        User user = getCurrentUserSafe();
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy phiên đăng nhập của người dùng");
        }
        return user;
    }

    private User getCurrentUserSafe() {
        return userRepository.findAll().stream().findFirst().orElse(null);
    }
}
