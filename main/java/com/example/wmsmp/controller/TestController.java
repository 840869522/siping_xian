package com.example.wmsmp.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class TestController {

    @GetMapping("/test")
    @CrossOrigin
    public void GetAGVMapByPage(String testStr) {
        System.out.println(testStr);
    }

}
