package com.example.adoptions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Map;

@EnableResilientMethods
@SpringBootApplication
public class AdoptionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdoptionsApplication.class, args);
    }


}

@Configuration
class SecurityConfiguration {

    @Bean
    Customizer<HttpSecurity> httpSecurityCustomizer() {
        return http -> http
                .webAuthn(w -> w
                        .rpName("bootiful")
                        .rpId("localhost")
                        .allowedOrigins("http://localhost:8080")
                )
                .oneTimeTokenLogin(c -> c.tokenGenerationSuccessHandler((request, response, oneTimeToken) -> {
                    response.getWriter().println("you've got console mail!");
                    response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                    IO.println("please go to http://localhost:8080/login/ott?token=" +
                            oneTimeToken.getTokenValue());
                }));
    }

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        var u = new JdbcUserDetailsManager(dataSource);
        u.setEnableUpdatePassword(true);
        return u;
    }

}


@Controller
@ResponseBody
class MeController {

    @GetMapping("/")
    Map<String, String> me(Principal principal) {
        return Map.of("name", principal.getName());
    }
}

@Controller
@ResponseBody
class VirtualThreadsController {

    private final RestClient http;

    VirtualThreadsController(RestClient.Builder http) {
        this.http = http.build();
    }

    @GetMapping("/salut")
    String hello() {
        var msg = Thread.currentThread() + ":";
        var response = this.http
                .get()
                .uri("http://localhost:9000/delay/5")
                .retrieve()
                .body(String.class);
        msg += Thread.currentThread();
        IO.println(msg);
        return response;
    }

}



