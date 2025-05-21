package com.foodorder.backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

@Component
public class EnvLoader {
    static {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./") // Thư mục chứa file .env
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())

            );
            
        } catch (Exception e) {
            System.out.println("ENV_VARIABLE_MISSING" + e.getMessage());
        }
    }
}
