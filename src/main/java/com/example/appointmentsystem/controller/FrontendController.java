package com.example.appointmentsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    @RequestMapping(value = {"/admin/**", "/home/**", "/login/**"})
    public String forward() {
        // Przekierowanie wszystkich niewspieranych ścieżek do Angulara
        return "forward:/index.html";
    }
}
