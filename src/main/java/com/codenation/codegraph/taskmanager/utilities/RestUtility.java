package com.codenation.codegraph.taskmanager.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by pranet on 24/07/17.
 */
@Slf4j
public class RestUtility {

    private static final int NUMBER_OF_RETRIES = 5;

    private static final int INITIAL_WAIT_TIME_MILLIS = 1000;

    public static String constructURI(String hostname, String endpoint) {
        if (hostname.endsWith("/")) {
            return hostname + endpoint;
        } else {
            return hostname + "/" + endpoint;
        }
    }

    public static ResponseEntity<String> sendGetRequest(MultiValueMap<String, String> params, String endPoint) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endPoint)
                .queryParams(params);
        RestTemplate restTemplate = new RestTemplate();
        try {

            Callable<ResponseEntity<String>> getMethodCallable = () -> restTemplate.getForEntity(builder.toUriString(), String.class);

            return RetryUtility.doWithRetry(getMethodCallable, NUMBER_OF_RETRIES, INITIAL_WAIT_TIME_MILLIS, "GET METHOD WITH RETRIES");

        } catch (Exception e) { // RetryUtility.doWithRetry() throws top-level exception
            log.error("Get request failed: " + builder.toUriString());
            throw e;
        }
    }

    public static ResponseEntity<String> sendPostRequest(Object request, String url) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Unable to convert " + request.toString() + " to a valid json", e);
            throw e;
        }
        try {
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            Callable<ResponseEntity<String>> postMethodCallable = () -> restTemplate.postForEntity(url, entity, String.class);

            return RetryUtility.doWithRetry(postMethodCallable, NUMBER_OF_RETRIES, INITIAL_WAIT_TIME_MILLIS, "POST METHOD WITH RETRIES");

        } catch (Exception e) { // RetryUtility.doWithRetry() throws top-level exception
            log.error("Post request failed to url :" + url + ", body : " + request.toString(), e);
            throw e;
        }
    }

    public static void sendDeleteRequest(String url, Map<String, String> params) throws Exception {

        try {
            RestTemplate restTemplate = new RestTemplate();

            Callable<Void> deleteMethodCallable = () -> {
                restTemplate.delete(url, params);
                return null;
            };

            RetryUtility.doWithRetry(deleteMethodCallable, NUMBER_OF_RETRIES, INITIAL_WAIT_TIME_MILLIS, "DELETE METHOD WITH RETRIES");

        } catch (Exception e) { // RetryUtility.doWithRetry() throws top-level exception
            log.error("Delete request failed to url :" + url, e);
            throw e;
        }
    }

}
