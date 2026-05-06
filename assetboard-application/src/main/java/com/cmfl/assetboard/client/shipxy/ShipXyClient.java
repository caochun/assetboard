package com.cmfl.assetboard.client.shipxy;

import com.fasterxml.jackson.databind.JsonNode;

public interface ShipXyClient {
    JsonNode searchShipParticular(String imo);
    JsonNode getShipTrack(String imo, long btm, long etm);
    JsonNode getPscHistory(String imo);
    JsonNode getNavWarningList();
    JsonNode getWeatherByPoint(double lon, double lat);
    JsonNode getShipAlertList();
}
