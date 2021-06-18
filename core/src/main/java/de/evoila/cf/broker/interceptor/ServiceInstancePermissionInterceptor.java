package de.evoila.cf.broker.interceptor;


import de.evoila.cf.broker.bean.CloudFoundryApplicationProperties;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ServiceInstancePermissionInterceptor
 * Interceptor which is checking the users permissions against the specified service instance id.
 *
 * @author latzinger
 */
public class ServiceInstancePermissionInterceptor implements HandlerInterceptor {

    private static final String USER_ID = "user_id";
    private static final String SUB = "sub";
    private static final String READ = "read";
    private static final String MANAGE = "manage";

    private HashMap<String, Pair<Date, HashMap<String, Map<Object, Object>>>> cachedPermissions = new HashMap<>();
    private final static int CACHE_VALID = 1; //TIME IN MINUTES

    private final CloudFoundryApplicationProperties cloudFoundryApplicationProperties;

    private static final String CF_PERMISSIONS_ENDPOINT = "/v2/service_instances/:guid/permissions";

    public ServiceInstancePermissionInterceptor(CloudFoundryApplicationProperties cloudFoundryApplicationProperties) {
        this.cloudFoundryApplicationProperties = cloudFoundryApplicationProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) request.getUserPrincipal();
        Map<Object, Object> attributes = (Map<Object, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String serviceInstanceId = (String) attributes.get("serviceInstanceId");

        if (!cannAccessServiceInstance(serviceInstanceId)) {
            throw new AuthenticationServiceException("User is not authorised to access" + serviceInstanceId + ". Please contact your System Administrator.");
        }

        return true;
    }

    private boolean cannAccessServiceInstance(String serviceInstanceId) {

        final String userId = getUserId();
        final Pair<Date, HashMap<String, Map<Object, Object>>> userEntry = cachedPermissions.get(userId);

        if (userEntry != null) {

            if (userEntry.getFirst().after(new Date())) { //A: User exists AND permissions are NOT expired. Check if permissions contains entry for this serviceInstanceId.

                if(!userEntry.getSecond().containsKey(serviceInstanceId)){ //Set Permissions for this serviceInstanceId.
                    ResponseEntity<Map> permissions = fetchPermissions(serviceInstanceId);
                    this.cachedPermissions.get(userId).getSecond().put(serviceInstanceId, permissions.getBody());
                }

                return (boolean) this.cachedPermissions.get(userId).getSecond().get(serviceInstanceId).get(READ);

            } else { //B User exists BUT permissions are expired. Remove user and request current permissions.
                cachedPermissions.remove(userId);
                return cannAccessServiceInstance(serviceInstanceId);
            }

        } else { //C: User doesnt exists. Create new entry and request permissions.

            ResponseEntity<Map> permissions = fetchPermissions(serviceInstanceId);
            HashMap<String, Map<Object, Object>> permissionEntry = new HashMap<>();
            permissionEntry.put(serviceInstanceId, permissions.getBody());
            Pair<Date, HashMap<String, Map<Object, Object>>> timeEntry = Pair.of(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CACHE_VALID)), permissionEntry);
            this.cachedPermissions.put(userId, timeEntry);

            return (boolean) this.cachedPermissions.get(userId).getSecond().get(serviceInstanceId).get(READ);
        }
    }

    private ResponseEntity<Map> fetchPermissions(String serviceInstanceId) {
        HttpEntity<String> httpEntity = new HttpEntity<>(getHttpHeaders());
        String uri = CF_PERMISSIONS_ENDPOINT.replace(":guid", serviceInstanceId);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(cloudFoundryApplicationProperties.getCfApi()));

        try{
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, Map.class);
            if (response.getStatusCode().isError()) {
                throw new AuthenticationServiceException("Failed to request permissions for " + serviceInstanceId + ".");
            }

            return response;
        } catch (HttpClientErrorException e ){
            throw new AuthenticationServiceException("Failed to request permissions for " + serviceInstanceId + ". Service Instance was not found!");
        }

    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + getUserToken());
        return headers;
    }

    private String getUserId() {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return jwtAuthenticationToken.getToken().containsClaim(USER_ID) ? jwtAuthenticationToken.getToken().getClaimAsString(USER_ID) : jwtAuthenticationToken.getToken().getClaim(SUB);
    }

    private String getUserToken() {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return jwtAuthenticationToken.getToken().getTokenValue();
    }


}