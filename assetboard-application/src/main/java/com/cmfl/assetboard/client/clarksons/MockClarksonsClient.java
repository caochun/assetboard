package com.cmfl.assetboard.client.clarksons;

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
public class MockClarksonsClient implements ClarksonsClient {

    private static final Logger log = LoggerFactory.getLogger(MockClarksonsClient.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonNode getAssetValueHistory(String imo) {
        return getByImo("mock/clarksons/asset_value_history.json", imo);
    }

    @Override
    public JsonNode getAssetValuations(String imo) {
        return getByImo("mock/clarksons/asset_valuations.json", imo);
    }

    @Override
    public JsonNode getDemoPriceHistory(String imo) {
        return getByImo("mock/clarksons/demo_price_history.json", imo);
    }

    @Override
    public JsonNode getNewbuildPriceHistory(String imo) {
        return getByImo("mock/clarksons/newbuild_price_history.json", imo);
    }

    private JsonNode getByImo(String path, String imo) {
        JsonNode all = loadJson(path);
        JsonNode result = all.get(imo);
        if (result == null) {
            return mapper.createObjectNode().put("recordCount", 0).putArray("results");
        }
        return result;
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
