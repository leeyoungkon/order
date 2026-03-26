package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "paymentClient", url = "${services.payment.url}")
public interface PaymentClient {

    @GetMapping("/payment")
    String payment();
}
