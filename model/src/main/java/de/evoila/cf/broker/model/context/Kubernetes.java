package de.evoila.cf.broker.model.context;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Kubernetes extends Context {

    @JsonProperty("namespace")
    private String namespace;

    @JsonProperty("clusterid")
    private String clusterId;

    public Kubernetes() {
        // Jackson does not set property platform as it is for it's inheritance model. Therefor setting it manually here.
        super("kubernetes");
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
