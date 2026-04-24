package com.example.assignment.service;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.request.CourseReq;
import jakarta.validation.Valid;

public interface CourseService {

  ResultResponse register(@Valid CourseReq courseReq);
}
