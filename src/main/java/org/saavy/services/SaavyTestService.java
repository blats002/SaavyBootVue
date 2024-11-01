package org.saavy.services;

import org.saavy.entity.SaavyTest;
import org.saavy.entity.SaavyTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SaavyTestService {
    @Autowired
    private SaavyTestRepository saavyTestRepository;

    public List<SaavyTest> findAll(){
        return saavyTestRepository.findAll();
    }

    public Optional<SaavyTest> findById(Long id){
        return saavyTestRepository.findById(id);
    }

    public SaavyTest save(SaavyTest st){
        return saavyTestRepository.save(st);
    }

    public void deleteById(Long id){
        saavyTestRepository.deleteById(id);
    }
}
