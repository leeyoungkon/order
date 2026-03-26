package com.example.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StepApiController {

    @GetMapping("/stock")
    public String stock() {
        return "stock check done";
    }

    @GetMapping("/payment")
    public String payment() {
        return "payment done";
    }

    @GetMapping("/delivery")
    public String delivery() {
        return "delivery requested";
    }
}
