package org.delcom.ifs25050springbootcashflow;

import org.delcom.ifs25050springbootcashflow.config.DotEnv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Ifs25050P5Pbo2026SpringbootCashflowApplication {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(Ifs25050P5Pbo2026SpringbootCashflowApplication.class, args);
    }
}