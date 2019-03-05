package de.evoila.cf.broker.bean;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by reneschollmeyer, evoila on 29.10.18.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.credhub")
@ConditionalOnProperty(prefix = "spring.credhub", name = {"url", "bosh-director", "oauth2.client-id", "oauth2.client-secret", "oauth2.access-token-uri",
                                                          "certificate.ca", "certificate.certificate", "certificate.private-key"})
public class CredhubBean {

    private String url;

    private String boshDirector;

    private Oauth2 oauth2;

    private Certificate certificate;

    private String keystorePassword = "";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBoshDirector() {
        return boshDirector;
    }

    public void setBoshDirector(String boshDirector) {
        this.boshDirector = boshDirector;
    }

    public Oauth2 getOauth2() {
        return oauth2;
    }

    public void setOauth2(Oauth2 oauth2) {
        this.oauth2 = oauth2;
    }

    public static class Oauth2 {
        private String clientId;

        private String clientSecret;

        private String accessTokenUri;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getAccessTokenUri() {
            return accessTokenUri;
        }

        public void setAccessTokenUri(String accessTokenUri) {
            this.accessTokenUri = accessTokenUri;
        }
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public static class Certificate {

        private String ca;

        private String certificate;

        private String privateKey;

        public String getCa() {
            return ca;
        }

        public void setCa(String ca) {
            this.ca = ca;
        }

        public String getCertificate() {
            return certificate;
        }

        public void setCertificate(String certificate) {
            this.certificate = certificate;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }
}
