package com.xiaoazhai;

import org.springframework.annotations.Autowired;
import org.springframework.annotations.Component;
import org.springframework.annotations.Value;

@Component
public class Person {

   @Value(value = "${abc}")
   private String abc;
    @Autowired
    private Animal animal;
    public String sayHello(){
        System.out.println("Hello World!,I'm Inject abc="+abc);
        return abc;
    }

    public String getAbc() {
        return abc;
    }

    public Animal getAnimal() {
        return animal;
    }
}
