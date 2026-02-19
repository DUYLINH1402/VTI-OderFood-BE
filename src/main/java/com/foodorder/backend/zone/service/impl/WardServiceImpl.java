package com.foodorder.backend.zone.service.impl;

import com.foodorder.backend.config.CacheConfig;
import com.foodorder.backend.zone.dto.response.WardResponse;
import com.foodorder.backend.zone.entity.Ward;
import com.foodorder.backend.zone.repository.WardRepository;
import com.foodorder.backend.zone.service.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WardServiceImpl implements WardService {

    private final WardRepository wardRepository;

    @Override
    @Cacheable(value = CacheConfig.WARDS_BY_DISTRICT_CACHE, key = "#districtId")
    public List<WardResponse> getWardsByDistrict(Long districtId) {
        List<Ward> wards = wardRepository.findByDistrictId(districtId);
        return wards.stream()
                .map(ward -> new WardResponse(ward.getId(), ward.getName(), ward.getDistrict().getId()))
                .collect(Collectors.toList());
    }
}
