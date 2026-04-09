package com.example.adoptions.cats;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.env.Environment;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.service.registry.ImportHttpServices;

import javax.sql.DataSource;
import java.lang.annotation.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Import(MyBeanRegistrar.class)
@ImportHttpServices(CatFactsClient.class)
class CatFactsConfiguration {


}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@interface JugParisComponent {

    /**
     * Alias for {@link Component#value}.
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

}

class MyBeanRegistrar implements BeanRegistrar {

    // Sir Tony Hoare
    // project valhalla

    @Override
    public void register(@NonNull BeanRegistry registry, @NonNull Environment env) {

        registry.registerBean(MyRunner.class, spec -> spec.supplier(
                supplierContext -> new MyRunner(
                        supplierContext.bean(DataSource.class))));


//        for (var i = 0; i < 10; i++)
//            registry.registerBean(MyRunner.class);

    }
}

class MyRunner implements ApplicationRunner {

    private final DataSource dataSource;

    MyRunner(DataSource dataSource) {
        Assert.notNull(dataSource, "DataSource must not be null");
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IO.println("hi!");
    }
}

@Controller
@ResponseBody
class CatsController {

    private final CatFactsClient catFactsClient;

    private final AtomicInteger counter = new AtomicInteger(0);

    CatsController(CatFactsClient catFactsClient) {
        this.catFactsClient = catFactsClient;
    }

    @ConcurrencyLimit(10)
    @Retryable(maxRetries = 5, includes = IllegalStateException.class)
    @GetMapping("/cats")
    CatFacts facts() {

        if (this.counter.getAndIncrement() < 5) {
            IO.println("oops!");
            throw new IllegalStateException("oops!");
        }

        IO.println("yay!");
        return this.catFactsClient.facts();
    }
}
