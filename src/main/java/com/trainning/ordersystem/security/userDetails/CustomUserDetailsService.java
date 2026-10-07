package com.trainning.ordersystem.security.userDetails;


import com.trainning.ordersystem.entity.Customer;
import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.repository.CustomerRepository;
import com.trainning.ordersystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tìm User trong DB theo email kèm eager load Role
        User user = userRepository.findByEmailWithRole(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        Long customerId = customerRepository.findByUserId(user.getId())
                .map(Customer::getId)
                .orElse(null);

        // Bọc User và customerId tìm được vào CustomUserDetails
        return new CustomUserDetails(user, customerId);
    }
}
