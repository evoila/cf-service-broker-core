package de.evoila.cf.security.credentials.agent;


import com.fasterxml.jackson.annotation.JsonProperty;
import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.model.catalog.ServerAddress;
import de.evoila.cf.security.model.CreateCredentialAgentResponse;
import de.evoila.cf.security.model.HealthCredentialAgentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;


public class CredentialAgentClientHandler {

    private final WebClient client;

    private static final Logger log = LoggerFactory.getLogger(CredentialAgentClientHandler.class);

    private static final String PATH_CREDENTIALS = "/credentials";
    private static final String PATH_HEALTH = "/health";
    private static final String PATH_SHUTDOWN = "/shutdown";

    public CredentialAgentClientHandler(ServerAddress address) throws ServiceBrokerException {
        this("http://" + address.getIp() + ':' + 8050);
    }

    CredentialAgentClientHandler(String url) throws ServiceBrokerException {
        this.client = WebClient.create(url);
        HealthCredentialAgentResponse health = checkHealth();

        if (!health.getStatus().equals("ready")) {
            throw new ServiceBrokerException("credential agent is not ready");
        }

        log.info(health.toString());

    }

    public CreateCredentialAgentResponse putCredentials(String bindingId) {
        return client.put()
                .uri(PATH_CREDENTIALS)
                .body(new CredentialAgentClientHandler.MetaData(bindingId), CredentialAgentClientHandler.MetaData.class)
                .retrieve().bodyToMono(CreateCredentialAgentResponse.class).block();
    }

    public void deleteCredentials(String bindingId) {
        client.delete()
                .uri(PATH_CREDENTIALS + "?binding-id=" + bindingId)
                .exchange()
                .block();
    }

    public HealthCredentialAgentResponse checkHealth() {
        return client.get()
                .uri(PATH_HEALTH)
                .retrieve()
                .bodyToMono(HealthCredentialAgentResponse.class)
                .block();
    }

    public void shutdown() {
        client.post().
                uri(PATH_SHUTDOWN)
                .exchange();
    }

    static class MetaData {
        MetaData(String bindingId) {
            this.bindingId = bindingId;
        }

        @JsonProperty(value = "binding-id")
        private String bindingId;

        public String getBindingId() {
            return bindingId;
        }

        public void setBindingId(String bindingId) {
            this.bindingId = bindingId;
        }
    }
}
