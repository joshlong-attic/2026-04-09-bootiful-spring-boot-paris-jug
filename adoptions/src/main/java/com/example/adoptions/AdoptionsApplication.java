package com.example.adoptions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;

@EnableResilientMethods
@SpringBootApplication
public class AdoptionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdoptionsApplication.class, args);
    }

    @Bean
    JdbcPostgresDialect jdbcPostgresDialect() {
        return JdbcPostgresDialect.INSTANCE;
    }

    @Bean
    MyBeanFactoryInitializationAotProcessor myBeanFactoryInitializationAotProcessor() {
        return new MyBeanFactoryInitializationAotProcessor();
    }

}

// 10%
class MyBeanFactoryInitializationAotProcessor implements BeanFactoryInitializationAotProcessor {

    @Override
    public @Nullable BeanFactoryInitializationAotContribution processAheadOfTime(
            ConfigurableListableBeanFactory beanFactory) {

        var serializable = new HashSet<Class<?>>();
        for (var beanName : beanFactory.getBeanDefinitionNames()) {
            IO.println(beanName);
            var type = beanFactory.getType(beanName);
            IO.println(type.getName());

            if (Serializable.class.isAssignableFrom(type)) {
                serializable.add(type);
                IO.println("is " + type.getName() + " serializable?");
            }
        }

        return (generationContext, _) -> {

            for (var s : serializable) {
                var hints = generationContext.getRuntimeHints();
                hints.serialization().registerType(TypeReference.of(s));
            }
        };
    }
}


@Component
class ShoppingCart implements Serializable {
    //
}

// 0. ingest (xml, component scanning, java config, beanRegistrar)
// 1. BeanDefinitions
// 2. beans


@ImportRuntimeHints(SecurityConfiguration.ParisJugRuntimeHintsRegistrar.class)
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

    // 90%
    static class ParisJugRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
            hints.resources().registerResource(RESOURCE);
            hints.reflection().registerType(Cat.class, MemberCategory.values());
        }
    }

    private static final ClassPathResource RESOURCE = new ClassPathResource("/message");

    @Bean
    ApplicationRunner jacksonRunner(JsonMapper jsonMapper) {
        return a -> {
            var garfield = new Cat(1, "Garfield");
            var obj = jsonMapper.writeValueAsString(garfield);
            IO.println(obj);
        };
    }

    record Cat(int id, String name) {
    }

    @Bean
    ApplicationRunner resourceRunner() {
        return _ -> IO.println(
                RESOURCE.getContentAsString(Charset.defaultCharset()));
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