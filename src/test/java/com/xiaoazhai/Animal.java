package com.xiaoazhai;

import org.springframework.annotations.Autowired;
import org.springframework.annotations.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class Animal {
    @Autowired
    private Person person;

    public String sayHello() {
        return "animal";
    }

    @PostConstruct
    public void init(){
        System.out.println("Animal init");
    }
    @PreDestroy
    public void destroy(){
        System.out.println("Animal destroy");
    }

    public Person getPerson() {
        return person;
    }
}