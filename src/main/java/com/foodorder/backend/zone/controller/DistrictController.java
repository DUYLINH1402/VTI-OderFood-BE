package com.foodorder.backend.zone.controller;

import com.foodorder.backend.zone.dto.response.DistrictResponse;
import com.foodorder.backend.zone.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping
    public ResponseEntity<List<DistrictResponse>> getAllDistricts() {
        List<DistrictResponse> districts = districtService.getAllDistricts();
        return ResponseEntity.ok(districts);
    }
}
