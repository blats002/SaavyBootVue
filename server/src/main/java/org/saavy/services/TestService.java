package org.saavy.services;

import org.saavy.entity.Test;
import org.saavy.entity.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TestService {
    @Autowired
    private TestRepository testRepository;

    public List<Test> findAll(){
        return testRepository.findAll();
    }

    public Optional<Test> findById(Long id){
        return testRepository.findById(id);
    }

    public Test save(Test st){
        return testRepository.save(st);
    }

    public void deleteById(Long id){
        testRepository.deleteById(id);
    }
}
