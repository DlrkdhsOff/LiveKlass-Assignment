package com.example.assignment.creator.service;

import com.example.assignment.common.dto.ResultResponse;
import com.example.assignment.creator.dto.request.CourseSearchReq;

public interface CourseService {

  ResultResponse getCourses(CourseSearchReq searchReq, int page);

  ResultResponse getCourseDetail(Long courseId);
}
