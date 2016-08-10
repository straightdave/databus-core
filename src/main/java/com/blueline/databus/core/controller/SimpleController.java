package com.blueline.databus.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class SimpleController {

    @RequestMapping(value = "/hi", method = GET)
    public String sayHi() {
        return "Hi~";
    }
}
