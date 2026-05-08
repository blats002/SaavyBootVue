package org.saavy.controllers;

import org.saavy.entity.Test;
import org.saavy.services.JPAService;
import org.saavy.services.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tests")
public class TestController extends BaseController<Test, Long> {

    @Autowired
    private TestService testService;

    @Override
    protected JPAService<Test, Long> getService() {
        return testService;
    }
}