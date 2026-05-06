package com.cmfl.assetboard.client.shipxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@Profile("!prod")
public class MockShipXyClient implements ShipXyClient {

    private static final Logger log = LoggerFactory.getLogger(MockShipXyClient.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonNode searchShipParticular(String imo) {
        JsonNode all = loadJson("mock/shipxy/ship_particular.json");
        JsonNode result = all.get(imo);
        if (result == null) {
            log.warn("Mock: no ship particular data for IMO {}", imo);
            return mapper.createObjectNode().put("code", 200).put("msg", "成功!").putArray("data");
        }
        return result;
    }

    @Override
    public JsonNode getShipTrack(String imo, long btm, long etm) {
        JsonNode all = loadJson("mock/shipxy/ship_track.json");
        JsonNode result = all.get(imo);
        if (result == null) {
            return mapper.createObjectNode().put("code", 200).put("msg", "成功!").putArray("data");
        }
        return result;
    }

    @Override
    public JsonNode getPscHistory(String imo) {
        JsonNode all = loadJson("mock/shipxy/psc_history.json");
        JsonNode result = all.get(imo);
        if (result == null) {
            return mapper.createObjectNode().put("code", 200).put("msg", "成功!").putArray("data");
        }
        return result;
    }

    @Override
    public JsonNode getNavWarningList() {
        return loadJson("mock/shipxy/nav_warnings.json");
    }

    @Override
    public JsonNode getWeatherByPoint(double lon, double lat) {
        return loadJson("mock/shipxy/weather.json");
    }

    @Override
    public JsonNode getShipAlertList() {
        return loadJson("mock/shipxy/ship_alerts.json");
    }

    private JsonNode loadJson(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return mapper.readTree(is);
        } catch (IOException e) {
            log.error("Failed to load mock data from {}", path, e);
            return mapper.createObjectNode();
        }
    }
}
