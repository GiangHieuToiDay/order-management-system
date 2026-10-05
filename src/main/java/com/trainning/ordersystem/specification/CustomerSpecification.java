package com.trainning.ordersystem.specification;

import com.trainning.ordersystem.entity.Customer;
import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.entity.enums.MembershipLevel;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CustomerSpecification {

    private final CustomerRepository customerRepository;

    public Customer tranferCustomerFromUser(User user, String address) {
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setAddress(address);
        customer.setMembershipLevel(MembershipLevel.REGULAR);
        return customer;
    }

    public Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND, "Không tìm thấy khách hàng với id = " + customerId));
    }

    public Optional<Customer> findCustomerByUserId(Long userId) {
        return customerRepository.findByUserId(userId);
    }

    public void updateAddressIfExists(Long userId, String address) {
        if (address != null) {
            customerRepository.findByUserId(userId)
                    .ifPresent(customer -> customer.setAddress(address));
        }
    }
}
