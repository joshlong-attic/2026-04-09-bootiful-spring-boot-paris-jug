package com.example.adoptions.dogs;

import com.example.adoptions.dogs.validation.Validation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Transactional
@Controller
@ResponseBody
class AdoptionsController {

    private final Validation validation ;
    private final DogRepository repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    AdoptionsController(Validation validation, DogRepository repository, ApplicationEventPublisher applicationEventPublisher) {
        this.validation = validation;
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.repository.findById(dogId).ifPresent(dog -> {
            var updated = this.repository.save(
                    new Dog(dogId, dog.name(), owner, dog.description()));
            IO.println("adopted " + updated);
            this.applicationEventPublisher.publishEvent(new DogsAdoptedEvent(dogId));
        });
    }
}
