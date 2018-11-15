package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.stream.StreamSupport;

@Controller
public class ConfigController {
    @Autowired
    private ConfigurableEnvironment env;

    @Value("${azure.config.color}")
    private String color;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("name", "world");
        model.addAttribute("backgroundcolor", color);
        return "index";
    }

    @GetMapping("/props")
    @ResponseBody
    public String getAllConfigs() {
        MutablePropertySources sources = env.getPropertySources();

        StringBuilder propsBuilder = new StringBuilder();
        StreamSupport.stream(sources.spliterator(), false)
                .filter(s -> s instanceof EnumerablePropertySource)
                .filter(s -> s.getName().startsWith("bootstrap") || s.getName().startsWith("applicationConfig"))
                .map(s -> ((EnumerablePropertySource)s).getPropertyNames())
                .flatMap(Arrays::stream)
                .forEach(propName -> propsBuilder.append(propName + "=" + env.getProperty(propName) + "<br>"));

        return propsBuilder.toString();
    }
}
