package com.example.adoptions.dogs;

import org.springframework.data.annotation.Id;

// look mom, no Lombok!!!
record Dog(@Id int id, String name, String owner, String description) {
}
