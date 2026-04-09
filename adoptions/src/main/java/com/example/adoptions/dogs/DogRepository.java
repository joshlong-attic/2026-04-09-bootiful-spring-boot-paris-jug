package com.example.adoptions.dogs;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Collection;

interface DogRepository extends ListCrudRepository<Dog, Integer> {

    Collection <Dog> findByName (String name);
}
