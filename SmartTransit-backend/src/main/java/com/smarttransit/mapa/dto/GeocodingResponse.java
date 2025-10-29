package com.smarttransit.mapa.dto;

import lombok.Builder;
import lombok.Data;
import org.json.JSONObject;

@Data
@Builder
public class GeocodingResponse {
    private Double latitude;
    private Double longitude;
    private String displayName;
    private String city;
    private String country;
    private String postcode;
    private String street;

    public static GeocodingResponse fromJSON(JSONObject json) {
        if (json == null) {
            return null;
        }
        JSONObject address = json.optJSONObject("address");

        return GeocodingResponse.builder()
                .latitude(json.optDouble("lat"))
                .longitude(json.optDouble("lon"))
                .displayName(json.optString("display_name", ""))
                .city(address != null ? address.optString("city", "") : "")
                .country(address != null ? address.optString("country", "") : "")
                .postcode(address != null ? address.optString("postcode", "") : "")
                .street(address != null ? address.optString("road", "") : "").build();
    }
}