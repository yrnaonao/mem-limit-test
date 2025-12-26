package com.example.memlimit;

import com.example.memlimit.service.JvmInfoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class MemLimitTestApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(MemLimitTestApplication.class, args);
        JvmInfoService jvmInfoService = context.getBean(JvmInfoService.class);
        jvmInfoService.printStartupInfo();
    }
}
