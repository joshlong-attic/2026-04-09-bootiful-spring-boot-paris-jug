package com.example.adoptions.cats;

import org.springframework.web.service.annotation.GetExchange;

interface CatFactsClient {

    @GetExchange("https://www.catfacts.net/api")
    CatFacts facts();

}
