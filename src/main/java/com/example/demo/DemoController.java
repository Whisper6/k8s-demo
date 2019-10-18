package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author wangjun
 */
@Controller
@RequestMapping("/")
@ResponseBody
public class DemoController {
    @GetMapping("/hello")
    public String hello() {
        return "hello world!";
    }
}
