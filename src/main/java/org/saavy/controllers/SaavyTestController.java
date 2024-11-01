package org.saavy.controllers;

import org.saavy.entity.SaavyTest;
import org.saavy.services.SaavyTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saavytests")
public class SaavyTestController {
    @Autowired
    private SaavyTestService saavyTestService;

    @GetMapping
    public List<SaavyTest> getAllSaavyTests(){
        return saavyTestService.findAll();
    }

    @GetMapping("/{id}")
    public SaavyTest getSaavyTestByIs(@PathVariable Long id){
        return saavyTestService.findById(id).orElse(null);
    }

    @PostMapping
    public SaavyTest createSaavyTest(@RequestBody SaavyTest saavyTest){
        return saavyTestService.save(saavyTest);
    }

    @DeleteMapping("/{id}")
    public void deleteSaavyTest(@PathVariable Long id){
        saavyTestService.deleteById(id);
    }
}
