package org.saavy.controllers;

import org.saavy.entity.Test;
import org.saavy.services.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
public class TestController {
    @Autowired
    private TestService testService;

    @GetMapping
    public List<Test> getAllTests(){
        return testService.findAll();
    }

    @GetMapping("/{id}")
    public Test getTestById(@PathVariable Long id){
        return testService.findById(id).orElse(null);
    }

    @PostMapping
    public Test save(@RequestBody Test test){
        return testService.save(test);
    }

    @DeleteMapping("/{id}")
    public void deleteTest(@PathVariable Long id){
        testService.deleteById(id);
    }
}
