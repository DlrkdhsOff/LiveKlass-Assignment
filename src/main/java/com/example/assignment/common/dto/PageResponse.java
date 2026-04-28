package com.example.assignment.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@Getter
@AllArgsConstructor
public class PageResponse<T> {

  private int pageNumber;  // 현재 페이지 번호
  private int totalPage;   // 총 페이지 수
  private Long totalData;  // 전체 데이터 개수
  private List<T> dataList; // 현재 페이지의 데이터 리스트
  /**
   * 리스트 데이터를 페이징 처리하여 PageResponse로 변환
   *
   * @param list       페이징할 리스트 데이터
   * @param pageNumber 요청한 페이지 번호
   * @return PageResponse 형태의 페이징 결과
   */
  public static <T> PageResponse<T> pagination(List<T> list, int pageNumber) {
    int pageSize = 10;  // 페이지 크기 설정
    Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
    int start = (int) pageable.getOffset();

    // start 인덱스가 데이터 리스트 크기를 초과하면 빈 페이지 반환
    if (start >= list.size()) {
      return new PageResponse<>(pageNumber, 0, 0L, List.of());
    }

    int end = Math.min(start + pageSize, list.size());
    List<T> pagedList = list.subList(start, end);

    // 페이징 객체 생성
    Page<T> page = new PageImpl<>(pagedList, pageable, list.size());

    // PageResponse 변환 및 반환
    return new PageResponse<>(
        page.getNumber() + 1,   // 페이지 번호는 1부터 시작
        page.getTotalPages(),   // 전체 페이지 수
        page.getTotalElements(),// 전체 데이터 개수
        page.getContent()       // 현재 페이지 데이터 리스트
    );
  }
}