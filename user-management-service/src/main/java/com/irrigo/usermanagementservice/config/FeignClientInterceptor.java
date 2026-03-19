package com.irrigo.usermanagementservice.config;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String token = attributes.getRequest().getHeader("Authorization");
            System.out.println("DEBUG FEIGN: Token trouvé -> " + (token != null)); // Ajoute cette ligne
            if (token != null) {
                template.header("Authorization", token);
            }
        } else {
            System.out.println("DEBUG FEIGN: Aucun attribut de requête trouvé !");
        }
    }
}
