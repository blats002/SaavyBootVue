
package org.saavy.services;

import org.saavy.entity.Test;
import org.saavy.entity.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TestService implements JPAService<Test, Long> {

    @Autowired
    private TestRepository testRepository;

    @Override
    public List<Test> findAll() {
        return testRepository.findAll();
    }

    @Override
    public Optional<Test> findById(Long id) {
        return testRepository.findById(id);
    }

    @Override
    public Test save(Test entity) {
        return testRepository.save(entity);
    }

    @Override
    public Test update(Long id, Test entity) {
        entity.setId(id);
        return testRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        testRepository.deleteById(id);
    }
}
