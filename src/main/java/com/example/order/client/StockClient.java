package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "stockClient", url = "${services.stock.url}")
public interface StockClient {

    @GetMapping("/stock")
    String stock();
}
