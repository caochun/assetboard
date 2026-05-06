package com.cmfl.assetboard.client.clarksons;

import com.fasterxml.jackson.databind.JsonNode;

public interface ClarksonsClient {
    JsonNode getAssetValueHistory(String imo);
    JsonNode getAssetValuations(String imo);
    JsonNode getDemoPriceHistory(String imo);
    JsonNode getNewbuildPriceHistory(String imo);
}
