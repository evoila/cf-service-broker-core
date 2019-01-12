package de.evoila.cf.broker.model.catalog.plan;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Marco Di Martino.
 */
public class Schemas {
	
    @JsonProperty(value = "service_instance", required = false)
	private SchemaServiceInstance serviceInstance;

	@JsonProperty(value = "service_binding", required = false)
	private SchemaServiceBinding serviceBinding;

	public Schemas() {}

	public Schemas(SchemaServiceInstance serviceInstance, SchemaServiceBinding serviceBinding) {
		this.serviceInstance = serviceInstance;
		this.serviceBinding = serviceBinding;
	}

	public SchemaServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(SchemaServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

	public SchemaServiceBinding getServiceBinding() {
		return serviceBinding;
	}

	public void setServiceBinding(SchemaServiceBinding serviceBinding) {
		this.serviceBinding = serviceBinding;
	}

}
