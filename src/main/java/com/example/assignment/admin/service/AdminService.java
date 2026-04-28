package com.example.assignment.admin.service;

import com.example.assignment.common.dto.ResultResponse;
import java.time.LocalDate;

public interface AdminService {

  ResultResponse getSaleRecord(Long userId, LocalDate startAt, LocalDate endAt, int page);
}
