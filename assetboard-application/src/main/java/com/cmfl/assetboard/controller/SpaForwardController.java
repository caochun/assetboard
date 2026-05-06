package com.cmfl.assetboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {"/login", "/alarms", "/customers", "/projects", "/contracts", "/settings/**"})
    public String forward() {
        return "forward:/index.html";
    }

    @GetMapping(value = {
        "/assets/{id:[0-9a-f\\-]{36}}",
        "/customers/{id:[0-9a-f\\-]{36}}",
        "/projects/{id:[0-9a-f\\-]{36}}",
        "/contracts/{id:[0-9a-f\\-]{36}}"
    })
    public String forwardEntityDetail() {
        return "forward:/index.html";
    }
}
