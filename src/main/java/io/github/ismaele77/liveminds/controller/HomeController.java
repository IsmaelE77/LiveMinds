package io.github.ismaele77.liveminds.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    @GetMapping
    public String home() {
        // Redirect to the Swagger UI page
        return "redirect:/swagger-ui/index.html";
    }
}
