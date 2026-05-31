package com.tinypay.dify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


// Dify /v1/workflows/run (blocking) 실제 HTTP 응답 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record DifyWorkflowResponse(

        @JsonProperty("workflow_run_id") String workflowRunId,
        @JsonProperty("task_id") String taskId,
        DifyWorkflowData data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DifyWorkflowData(
            String status,
            ChatAnalysisResponse.Data outputs,
            String error
    ) {
    }

    public boolean isSucceeded() {
        return data != null && "succeeded".equals(data.status());
    }

    public ChatAnalysisResponse.Data getOutputs() {
        return data != null ? data.outputs() : null;
    }
}
