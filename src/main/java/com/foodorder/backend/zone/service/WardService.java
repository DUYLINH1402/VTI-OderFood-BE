package com.foodorder.backend.zone.service;

import com.foodorder.backend.zone.dto.response.WardResponse;

import java.util.List;

public interface WardService {
    List<WardResponse> getWardsByDistrict(Long districtId);
}