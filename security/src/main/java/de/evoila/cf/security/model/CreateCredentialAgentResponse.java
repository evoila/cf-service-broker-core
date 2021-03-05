package de.evoila.cf.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateCredentialAgentResponse {

    @JsonProperty("credhub-ref")
    private String credhubRef;

    public String getCredhubRef() {
        return credhubRef;
    }

    public void setCredhubRef(String credhubRef) {
        this.credhubRef = credhubRef;
    }
}
