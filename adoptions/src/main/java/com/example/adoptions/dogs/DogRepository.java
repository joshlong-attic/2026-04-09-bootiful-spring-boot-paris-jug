package com.example.adoptions.dogs;

import org.springframework.data.repository.ListCrudRepository;

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}
