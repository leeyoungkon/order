package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "deliveryClient", url = "${services.delivery.url}")
public interface DeliveryClient {

    @GetMapping("/delivery")
    String delivery();
}
