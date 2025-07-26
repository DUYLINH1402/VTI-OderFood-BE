package com.foodorder.backend.zone.controller;

import com.foodorder.backend.zone.dto.response.WardResponse;
import com.foodorder.backend.zone.service.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    @GetMapping("/by-district/{districtId}")
    public ResponseEntity<List<WardResponse>> getWardsByDistrict(@PathVariable Long districtId) {
        List<WardResponse> wards = wardService.getWardsByDistrict(districtId);
        return ResponseEntity.ok(wards);
    }
}
