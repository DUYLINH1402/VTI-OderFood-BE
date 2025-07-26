package com.foodorder.backend.zone.service.impl;

import com.foodorder.backend.zone.dto.response.DistrictResponse;
import com.foodorder.backend.zone.entity.District;
import com.foodorder.backend.zone.repository.DistrictRepository;
import com.foodorder.backend.zone.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;

    @Override
    public List<DistrictResponse> getAllDistricts() {
        List<District> districts = districtRepository.findAll();
        return districts.stream()
                .map(DistrictResponse::fromEntity)
                .collect(Collectors.toList());

    }
}
