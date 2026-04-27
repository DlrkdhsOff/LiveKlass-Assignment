package com.example.assignment.service;

import com.example.assignment.domain.dto.ResultResponse;
import java.time.LocalDate;

public interface AdminService {

  ResultResponse getSaleRecord(Long userId, LocalDate startAt, LocalDate endAt, int page);
}
