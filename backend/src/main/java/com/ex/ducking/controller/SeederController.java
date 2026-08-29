package com.ex.ducking.controller;


import com.ex.ducking.service.DataSeederService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/seed")
public class SeederController {

    @Autowired
    private DataSeederService dataSeederService;

    @PostMapping("/candidates")
    public String seedCandidates(@RequestParam(defaultValue = "10") int count) {
        return dataSeederService.seedCandidates(count);
    }
}