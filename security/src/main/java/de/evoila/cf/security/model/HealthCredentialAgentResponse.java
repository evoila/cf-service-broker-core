package de.evoila.cf.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class HealthCredentialAgentResponse {
    @JsonProperty("status")
    private String status;

    @JsonProperty("credhub-info")
    private Map<String, String> credhubInfo;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getCredhubInfo() {
        return credhubInfo;
    }

    public void setCredhubInfo(Map<String, String> credhubInfo) {
        this.credhubInfo = credhubInfo;
    }

    @Override
    public String toString() {
        return "HealthCredentialAgentResponse{" +
                "status='" + status + '\'' +
                ", credhub-info=" + credhubInfo +
                '}';
    }
}
