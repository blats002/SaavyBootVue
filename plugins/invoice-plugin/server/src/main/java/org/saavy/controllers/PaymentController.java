package org.saavy.controllers;

import org.saavy.entity.Payment;
import org.saavy.entity.PaymentDTO;
import org.saavy.services.JPAService;
import org.saavy.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class PaymentController extends BaseController<Payment, PaymentDTO, Long> {

    @Autowired
    private PaymentService paymentService;

    @Override
    protected JPAService<Payment, PaymentDTO, Long> getService() {
        return paymentService;
    }
}
