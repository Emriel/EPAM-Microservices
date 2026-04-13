package com.epam.springCoreTask.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.epam.springCoreTask.dto.integration.TrainerWorkloadRequest;

@FeignClient(name = "trainer-workload-service")
public interface TrainerWorkloadClient {

    @PostMapping("/api/workloads/report")
    void reportWorkload(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("X-Transaction-Id") String transactionId,
            @RequestBody TrainerWorkloadRequest request);
}
