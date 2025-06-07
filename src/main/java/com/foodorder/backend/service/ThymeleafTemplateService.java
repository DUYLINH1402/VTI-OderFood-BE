
package com.foodorder.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class ThymeleafTemplateService {

    @Autowired
    private TemplateEngine templateEngine;

    public String buildVerificationEmail(String fullName, String token) {
        String verificationLink = "http://dongxanhfoodorder.shop/api/auth/verify?token=" + token;

        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("verificationLink", verificationLink);

        return templateEngine.process("verification_email.html", context);
    }
}
