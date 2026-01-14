package com.devoops.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/notification")
public class TestController {

    @GetMapping("test")
    public String test() {
        return "Notification Service is up and running!";
    }
}