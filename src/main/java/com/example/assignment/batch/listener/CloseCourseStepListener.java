package com.example.assignment.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CloseCourseStepListener implements StepExecutionListener {

  @Override
  public void beforeStep(StepExecution stepExecution) {
    log.info("[강의 자동 마감 배치] Step 시작 - stepName: {}",
        stepExecution.getStepName());
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    log.info("[강의 자동 마감 배치] Step 완료" +
            " - 처리 건수: {}" +
            " / skip 건수: {}" +
            " / 상태: {}",
        stepExecution.getWriteCount(),
        stepExecution.getSkipCount(),
        stepExecution.getStatus()
    );

    stepExecution.getFailureExceptions()
        .forEach(e ->
            log.error("[강의 자동 마감 배치] 예외 발생 - 예외: {}, 메시지: {}",
                e.getClass().getSimpleName(), e.getMessage()));

    return stepExecution.getExitStatus();
  }
}