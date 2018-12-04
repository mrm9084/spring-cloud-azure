package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConfigController {
    @Value("${azure.config.color}")
    private String color;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("name", "world");
        model.addAttribute("backgroundcolor", color);
        return "index";
    }
}
