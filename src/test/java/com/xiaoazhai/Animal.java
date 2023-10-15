package com.xiaoazhai;

import org.springframework.annotations.Autowired;
import org.springframework.annotations.Component;

@Component
public class Animal {
    @Autowired
    private Person person;

    public String sayHello() {
        return "animal";
    }

    public Person getPerson() {
        return person;
    }
}