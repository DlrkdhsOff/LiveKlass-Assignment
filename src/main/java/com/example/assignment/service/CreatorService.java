package com.example.assignment.service;

import com.example.assignment.domain.dto.ResultResponse;

public interface CreatorService {

  ResultResponse getSaleRecord(Long userId, String yearMonth);
}
