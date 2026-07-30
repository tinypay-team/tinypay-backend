package com.tinypay.dify.repository;

import com.tinypay.dify.domain.AiRequest;
import com.tinypay.dify.domain.AiRequestApiItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiRequestApiItemRepository extends JpaRepository<AiRequestApiItem, Long> {
    List<AiRequestApiItem> findAllByRequestOrderByExecutionOrderAsc(AiRequest request);
    List<AiRequestApiItem> findAllByRequest_IdInOrderByRequest_IdAscExecutionOrderAsc(List<Long> requestIds);
}
