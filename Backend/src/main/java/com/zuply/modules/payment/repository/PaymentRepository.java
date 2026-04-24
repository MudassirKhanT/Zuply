package com.zuply.modules.payment.repository;

import com.zuply.modules.payment.model.Payment;
import com.zuply.modules.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    List<Payment> findByCustomerId(Long customerId);
    Optional<Payment> findByOrderId(Long orderId);
}