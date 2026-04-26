package com.example.assignment.batch.step;

import com.example.assignment.batch.listener.CloseCourseStepListener;
import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.repository.CourseRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.PlatformTransactionManager;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class CloseCourseStepConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final CourseRepository courseRepository;
  private final CloseCourseStepListener closeCourseStepListener;

  @Bean
  public Step closeCourseStep() {
    return new StepBuilder("closeCourseStep", jobRepository)
        .<Course, Course>chunk(100, transactionManager)
        .reader(closeCourseReader())
        .processor(closeCourseProcessor())
        .writer(closeCourseWriter())
        .listener(closeCourseStepListener)
        .faultTolerant()

        // 데이터 자체의 문제 → 해당 건 skip 후 다음 건 처리
        .skip(DataIntegrityViolationException.class)    // DB 제약 조건 위반
        .skip(InvalidDataAccessApiUsageException.class) // 잘못된 JPQL 또는 파라미터 사용
        .skipLimit(10)                                  // 최대 10건까지 skip 허용

        // 일시적인 문제 → 최대 3번 재시도 후 실패 시 skip 으로 처리
        .retry(TransientDataAccessException.class)       // DB 일시적 오류
        .retry(QueryTimeoutException.class)              // 쿼리 실행 시간 초과
        .retry(DataAccessResourceFailureException.class) // DB 서버 연결 실패
        .retry(JpaSystemException.class)                 // JPA 내부 시스템 오류
        .retryLimit(3)

        // 재시도 횟수(3번) 모두 소진 시 → skip 으로 처리
        .skip(TransientDataAccessException.class)
        .skip(QueryTimeoutException.class)
        .skip(DataAccessResourceFailureException.class)
        .skip(JpaSystemException.class)

        .build();
  }

  /**
   * 강의 시작 하루 전까지 OPEN 또는 DRAFT 상태인 강의 조회
   */
  @Bean
  public ItemReader<Course> closeCourseReader() {
    LocalDate threeDaysLater = LocalDate.now().plusDays(3);
    List<Course> list = courseRepository.findAllOpenOrDraftCoursesBeforeStart(threeDaysLater);
    return new ListItemReader<>(list);
  }

  /**
   * 강의 자동 마감 처리
   * - OPEN, DRAFT 상태인 강의를 CLOSED 로 변경
   */
  @Bean
  public ItemProcessor<Course, Course> closeCourseProcessor() {
    return course -> {
      course.closeCourse();
      return course;
    };
  }

  /**
   * 처리된 강의 저장
   */
  @Bean
  public ItemWriter<Course> closeCourseWriter() {
    return chunk -> courseRepository.saveAll(chunk.getItems());
  }
}
