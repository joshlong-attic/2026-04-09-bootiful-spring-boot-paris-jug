package com.example.adoptions.vet;

import com.example.adoptions.dogs.DogsAdoptedEvent;
import com.example.adoptions.dogs.validation.Validation;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class Dogtor {

    private final Validation validation ;

    Dogtor(Validation validation) {
        this.validation = validation;
    }

    @ApplicationModuleListener
    void checkup(DogsAdoptedEvent dogId) throws Exception {
        Thread.sleep(5_000);
        IO.println("checking up on " + dogId);
    }
}
