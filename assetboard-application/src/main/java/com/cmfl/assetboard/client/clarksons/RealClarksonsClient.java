package com.cmfl.assetboard.client.clarksons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Profile("prod")
public class RealClarksonsClient implements ClarksonsClient {

    private static final Logger log = LoggerFactory.getLogger(RealClarksonsClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${collector.clarksons.api-url}")
    private String apiUrl;

    @Value("${collector.clarksons.username}")
    private String username;

    @Value("${collector.clarksons.password}")
    private String password;

    private volatile String token;
    private volatile long tokenExpiry;

    @Override
    public JsonNode getAssetValueHistory(String imo) {
        return callApi("/valuations/asset-value-history/" + imo);
    }

    @Override
    public JsonNode getAssetValuations(String imo) {
        return callApi("/valuations/asset-valuations/" + imo);
    }

    @Override
    public JsonNode getDemoPriceHistory(String imo) {
        return callApi("/valuations/demo-price-history/" + imo);
    }

    @Override
    public JsonNode getNewbuildPriceHistory(String imo) {
        return callApi("/valuations/newbuild-price-history/" + imo);
    }

    private synchronized void refreshToken() {
        if (token != null && System.currentTimeMillis() < tokenExpiry) return;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("username", username);
            headers.set("password", password);
            HttpEntity<Map<String, String>> req = new HttpEntity<>(
                    Map.of("username", username, "password", password), headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    apiUrl + "/user/ApiAuthentication/GenerateAuthenticationToken", req, String.class);
            JsonNode body = mapper.readTree(resp.getBody());
            if (body.path("success").asBoolean()) {
                token = body.get("token").asText();
                tokenExpiry = System.currentTimeMillis() + 3600_000;
            } else {
                log.error("Clarksons auth failed: {}", resp.getBody());
            }
        } catch (Exception e) {
            log.error("Clarksons auth error", e);
        }
    }

    private JsonNode callApi(String path) {
        refreshToken();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> req = new HttpEntity<>(headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    apiUrl + path, HttpMethod.GET, req, String.class);
            return mapper.readTree(resp.getBody());
        } catch (Exception e) {
            log.error("Clarksons API call failed: {}", path, e);
            return mapper.createObjectNode().put("recordCount", 0).putArray("results");
        }
    }
}
