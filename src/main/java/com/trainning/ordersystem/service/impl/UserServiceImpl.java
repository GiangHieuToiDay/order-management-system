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
import com.trainning.ordersystem.repository.RoleRepository;
import com.trainning.ordersystem.repository.UserRepository;
import com.trainning.ordersystem.service.UserService;
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
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final UserMapper userMapper;

    // ===================================================================
    // 1. XÁC THỰC & ĐĂNG KÝ
    // ===================================================================

    @Override
    @Transactional
    public UserProfileResponse registerCustomer(RegisterCustomerRequest request) {
        log.info("Khách hàng đăng ký tài khoản mới: username={}", request.getUsername());

        // Rule 1: Kiểm tra username/email đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS, "Tên đăng nhập '" + request.getUsername() + "' đã được sử dụng");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email '" + request.getEmail() + "' đã được sử dụng");
        }

        // Rule 1: Luôn luôn chỉ gán role CUSTOMER cho khách tự đăng ký (không cho đăng ký Staff/Admin)
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Không tìm thấy vai trò CUSTOMER"));

        // Tạo User ở trạng thái ACTIVE
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // TODO: Mã hóa bằng BCryptPasswordEncoder khi tích hợp Security
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(customerRole);
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        // Tạo hồ sơ Customer tương ứng với MembershipLevel = REGULAR
        Customer customer = new Customer();
        customer.setUser(savedUser);
        customer.setAddress(request.getAddress());
        customer.setMembershipLevel(MembershipLevel.REGULAR);
        Customer savedCustomer = customerRepository.save(customer);

        UserProfileResponse response = userMapper.toProfileResponse(savedUser);
        userMapper.enrichWithCustomer(savedCustomer, response);
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Người dùng đăng nhập: username={}", request.getUsername());

        // Rule 2: Kiểm tra tài khoản tồn tại
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        // Rule 2: Kiểm tra password
        if (!user.getPassword().equals(request.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Rule 2: Kiểm tra UserStatus (Nếu LOCKED → từ chối đăng nhập)
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        return AuthResponse.builder()
                .accessToken("MOCK_TOKEN_" + user.getUsername()) // TODO: Sinh JWT token thật ở bước Security
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().getName())
                .fullName(user.getFullName())
                .build();
    }

    // ===================================================================
    // 2. TRANG CÁ NHÂN (My Profile)
    // ===================================================================

    @Override
    public UserProfileResponse getMyProfile() {
        User currentUser = getCurrentUser();
        return toUserProfileWithCustomerInfo(currentUser);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();

        // Rule 3: Chỉ cho sửa full_name, phone, address. Tuyệt đối không sửa role, status, membership_level
        currentUser.setFullName(request.getFullName());
        currentUser.setPhone(request.getPhone());
        userRepository.save(currentUser);

        // Nếu là Customer thì cập nhật thêm địa chỉ nhận hàng
        customerRepository.findByUserId(currentUser.getId()).ifPresent(customer -> {
            if (request.getAddress() != null) {
                customer.setAddress(request.getAddress());
                customerRepository.save(customer);
            }
        });

        return toUserProfileWithCustomerInfo(currentUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User currentUser = getCurrentUser();

        // Xác thực mật khẩu cũ
        if (!currentUser.getPassword().equals(request.getOldPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Mật khẩu hiện tại không chính xác");
        }

        // Kiểm tra khớp xác nhận mật khẩu mới
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Mật khẩu xác nhận không khớp với mật khẩu mới");
        }

        currentUser.setPassword(request.getNewPassword());
        userRepository.save(currentUser);
        log.info("Người dùng '{}' đã đổi mật khẩu thành công", currentUser.getUsername());
    }

    // ===================================================================
    // 3. QUẢN TRỊ NGƯỜI DÙNG (Admin)
    // ===================================================================

    @Override
    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
        log.info("Admin tạo tài khoản mới: username={}, roleId={}", request.getUsername(), request.getRoleId());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS, "Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email '" + request.getEmail() + "' đã tồn tại");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Không tìm thấy vai trò với id = " + request.getRoleId()));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        // Rule 4: Nếu tạo Customer thì mới phát sinh hồ sơ Customer tương ứng
        UserProfileResponse response = userMapper.toProfileResponse(savedUser);
        if ("CUSTOMER".equalsIgnoreCase(role.getName())) {
            Customer customer = new Customer();
            customer.setUser(savedUser);
            customer.setMembershipLevel(MembershipLevel.REGULAR);
            Customer savedCustomer = customerRepository.save(customer);
            userMapper.enrichWithCustomer(savedCustomer, response);
        }

        return response;
    }

    @Override
    public UserProfileResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id = " + userId));
        return toUserProfileWithCustomerInfo(user);
    }

    @Override
    public PageResponse<UserProfileResponse> getUsers(int page, int size, String keyword, Long roleId, UserStatus status) {
        // Rule 5: Tra cứu người dùng kết hợp keyword, role, status và phân trang
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id = " + userId));

        // Rule 6: Không cho phép Admin tự khóa tài khoản của chính mình
        User currentUser = getCurrentUserSafe();
        if (currentUser != null && currentUser.getId().equals(userId) && status == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.CANNOT_LOCK_CURRENT_USER);
        }

        user.setStatus(status);
        userRepository.save(user);
        log.info("Admin đã cập nhật trạng thái người dùng '{}' thành {}", user.getUsername(), status);
    }

    // ===================================================================
    // 4. QUẢN LÝ KHÁCH HÀNG (Staff / Admin)
    // ===================================================================

    @Override
    public UserProfileResponse getCustomerById(Long customerId) {
        // Rule 7: Staff/Admin xem thông tin của một Customer cụ thể khi xử lý đơn hàng
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND, "Không tìm thấy khách hàng với id = " + customerId));
        UserProfileResponse response = userMapper.toProfileResponse(customer.getUser());
        userMapper.enrichWithCustomer(customer, response);
        return response;
    }

    @Override
    @Transactional
    public UserProfileResponse updateCustomerMembership(Long customerId, MembershipLevel membershipLevel) {
        // Rule 8: Staff/Admin có thể chủ động nâng/hạ hạng thành viên (REGULAR, SILVER, GOLD)
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND, "Không tìm thấy khách hàng với id = " + customerId));

        customer.setMembershipLevel(membershipLevel);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Đã cập nhật hạng thành viên của khách hàng '{}' thành {}", customer.getUser().getUsername(), membershipLevel);

        UserProfileResponse response = userMapper.toProfileResponse(savedCustomer.getUser());
        userMapper.enrichWithCustomer(savedCustomer, response);
        return response;
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    private UserProfileResponse toUserProfileWithCustomerInfo(User user) {
        UserProfileResponse response = userMapper.toProfileResponse(user);
        customerRepository.findByUserId(user.getId())
                .ifPresent(customer -> userMapper.enrichWithCustomer(customer, response));
        return response;
    }

    /**
     * Lấy người dùng hiện tại đang đăng nhập.
     * Khi chưa gắn JWT Security context, tạm thời lấy user đầu tiên trong DB để test luồng.
     */
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
