package com.cmfl.assetboard.client.shipxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("prod")
public class RealShipXyClient implements ShipXyClient {

    private static final Logger log = LoggerFactory.getLogger(RealShipXyClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${collector.shipxy.api-prefix}")
    private String apiPrefix;

    @Value("${collector.shipxy.api-key}")
    private String apiKey;

    @Value("${collector.shipxy.user-id}")
    private String userId;

    private String buildUrl(String path) {
        return apiPrefix + path + (path.contains("?") ? "&" : "?") + "k=" + apiKey + "&enc=1&u=" + userId;
    }

    @Override
    public JsonNode searchShipParticular(String imo) {
        String url = buildUrl("/commonApi/searchShipParticular?imo=" + imo);
        return call(url);
    }

    @Override
    public JsonNode getShipTrack(String imo, long btm, long etm) {
        String url = buildUrl("/commonApi/getShipTrack?imo=" + imo + "&btm=" + btm + "&etm=" + etm);
        return call(url);
    }

    @Override
    public JsonNode getPscHistory(String imo) {
        String url = buildUrl("/commonApi/getShipArchivePSCHistory?imo=" + imo);
        return call(url);
    }

    @Override
    public JsonNode getNavWarningList() {
        String url = buildUrl("/commonApi/getNavWarningList");
        return call(url);
    }

    @Override
    public JsonNode getWeatherByPoint(double lon, double lat) {
        String url = buildUrl("/commonApi/getWeatherByPoint?lon=" + lon + "&lat=" + lat);
        return call(url);
    }

    @Override
    public JsonNode getShipAlertList() {
        String url = buildUrl("/commonApi/getShipAlertList");
        return call(url);
    }

    private JsonNode call(String url) {
        try {
            String body = restTemplate.getForObject(url, String.class);
            return mapper.readTree(body);
        } catch (Exception e) {
            log.error("ShipXy API call failed: {}", url, e);
            return mapper.createObjectNode().put("code", 500).put("msg", e.getMessage());
        }
    }
}
