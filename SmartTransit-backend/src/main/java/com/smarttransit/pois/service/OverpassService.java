package com.smarttransit.pois.service;

import com.smarttransit.pois.dto.PoiDTO;
import com.smarttransit.pois.model.PoiType;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OverpassService {

    private static final Logger log = LoggerFactory.getLogger(OverpassService.class);

    @Value("${overpass.api.url}")
    private String overpassApiUrl;

    private final OkHttpClient httpClient;

    public OverpassService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Busca POIs usando Overpass API
     */
    public List<PoiDTO> searchPOIs(double lat, double lon, int radius, List<PoiType> types) throws IOException {
        String query = buildOverpassQuery(lat, lon, radius, types);

        log.info("Ejecutando query Overpass: lat={}, lon={}, radius={}", lat, lon, radius);

        RequestBody body = RequestBody.create(query, MediaType.parse("text/plain"));
        Request request = new Request.Builder()
                .url(overpassApiUrl)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Overpass API error: " + response.code());
            }

            String responseBody = response.body().string();
            return parseOverpassResponse(responseBody);
        }
    }

    /**
     * Construye la query en Overpass QL
     */
    private String buildOverpassQuery(double lat, double lon, int radius, List<PoiType> types) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("[out:json][timeout:25];\n");
        queryBuilder.append("(\n");

        if (types == null || types.isEmpty()) {
            // Buscar amenities y shops generales
            queryBuilder.append(String.format("  node[\"amenity\"](around:%d,%.6f,%.6f);\n", radius, lat, lon));
            queryBuilder.append(String.format("  node[\"shop\"](around:%d,%.6f,%.6f);\n", radius, lat, lon));
        } else {
            // Buscar tipos específicos
            for (PoiType type : types) {
                String osmTag = poiTypeToOSMTag(type);
                if (osmTag != null) {
                    queryBuilder.append(String.format("  node%s(around:%d,%.6f,%.6f);\n",
                            osmTag, radius, lat, lon));
                }
            }
        }

        queryBuilder.append(");\n");
        queryBuilder.append("out body;\n");
        queryBuilder.append(">;\n");
        queryBuilder.append("out skel qt;");

        return queryBuilder.toString();
    }

    /**
     * Convierte POIType a tag de OSM
     */
    private String poiTypeToOSMTag(PoiType type) {
        return switch (type) {
            case RESTAURANT -> "[\"amenity\"=\"restaurant\"]";
            case CAFE -> "[\"amenity\"=\"cafe\"]";
            case FAST_FOOD -> "[\"amenity\"=\"fast_food\"]";
            case BAR -> "[\"amenity\"=\"bar\"]";
            case BANK -> "[\"amenity\"=\"bank\"]";
            case ATM -> "[\"amenity\"=\"atm\"]";
            case HOSPITAL -> "[\"amenity\"=\"hospital\"]";
            case CLINIC -> "[\"amenity\"=\"clinic\"]";
            case PHARMACY -> "[\"amenity\"=\"pharmacy\"]";
            case SUPERMARKET -> "[\"shop\"=\"supermarket\"]";
            case MALL -> "[\"shop\"=\"mall\"]";
            case SHOP -> "[\"shop\"]";
            case PARKING -> "[\"amenity\"=\"parking\"]";
            case FUEL -> "[\"amenity\"=\"fuel\"]";
            case BUS_STOP -> "[\"highway\"=\"bus_stop\"]";
            case SCHOOL -> "[\"amenity\"=\"school\"]";
            case UNIVERSITY -> "[\"amenity\"=\"university\"]";
            case LIBRARY -> "[\"amenity\"=\"library\"]";
            case CINEMA -> "[\"amenity\"=\"cinema\"]";
            case THEATRE -> "[\"amenity\"=\"theatre\"]";
            case PARK -> "[\"leisure\"=\"park\"]";
            case POLICE -> "[\"amenity\"=\"police\"]";
            case FIRE_STATION -> "[\"amenity\"=\"fire_station\"]";
            case POST_OFFICE -> "[\"amenity\"=\"post_office\"]";
            case HOTEL -> "[\"tourism\"=\"hotel\"]";
            case CHURCH -> "[\"amenity\"=\"place_of_worship\"]";
            default -> null;
        };
    }

    /**
     * Parsea la respuesta de Overpass API
     */
    private List<PoiDTO> parseOverpassResponse(String responseBody) {
        List<PoiDTO> pois = new ArrayList<>();

        JSONObject json = new JSONObject(responseBody);
        JSONArray elements = json.getJSONArray("elements");

        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);

            if (!element.has("tags")) continue;

            JSONObject tags = element.getJSONObject("tags");
            PoiType poiType = osmTagsToPoiType(tags);

            if (poiType == null) continue;

            // Extraer datos
            String name = tags.optString("name",
                         tags.optString("brand", "Sin nombre"));

            PoiDTO poi = new PoiDTO();
            poi.setId("poi-" + element.getLong("id"));
            poi.setType(poiType);
            poi.setName(name);
            poi.setLatitude(element.getDouble("lat"));
            poi.setLongitude(element.getDouble("lon"));
            poi.setAddress(tags.optString("addr:street", null));
            poi.setPhone(tags.optString("phone", null));
            poi.setWebsite(tags.optString("website", null));
            poi.setOpeningHours(tags.optString("opening_hours", null));

            // Guardar todos los tags
            Map<String, String> tagMap = new HashMap<>();
            for (String key : tags.keySet()) {
                tagMap.put(key, tags.getString(key));
            }
            poi.setTags(tagMap);

            pois.add(poi);
        }

        log.info("Encontrados {} POIs", pois.size());
        return pois;
    }

    /**
     * Convierte tags de OSM a POIType
     */
    private PoiType osmTagsToPoiType(JSONObject tags) {
        if (tags.has("amenity")) {
            String amenity = tags.getString("amenity");
            return switch (amenity) {
                case "restaurant" -> PoiType.RESTAURANT;
                case "cafe" -> PoiType.CAFE;
                case "fast_food" -> PoiType.FAST_FOOD;
                case "bar", "pub" -> PoiType.BAR;
                case "bank" -> PoiType.BANK;
                case "atm" -> PoiType.ATM;
                case "hospital" -> PoiType.HOSPITAL;
                case "clinic", "doctors" -> PoiType.CLINIC;
                case "pharmacy" -> PoiType.PHARMACY;
                case "parking" -> PoiType.PARKING;
                case "fuel" -> PoiType.FUEL;
                case "school" -> PoiType.SCHOOL;
                case "university", "college" -> PoiType.UNIVERSITY;
                case "library" -> PoiType.LIBRARY;
                case "cinema" -> PoiType.CINEMA;
                case "theatre" -> PoiType.THEATRE;
                case "police" -> PoiType.POLICE;
                case "fire_station" -> PoiType.FIRE_STATION;
                case "post_office" -> PoiType.POST_OFFICE;
                case "place_of_worship" -> PoiType.CHURCH;
                default -> null;
            };
        }

        if (tags.has("shop")) {
            String shop = tags.getString("shop");
            return switch (shop) {
                case "supermarket" -> PoiType.SUPERMARKET;
                case "mall" -> PoiType.MALL;
                default -> PoiType.SHOP;
            };
        }

        if (tags.has("tourism") && tags.getString("tourism").equals("hotel")) {
            return PoiType.HOTEL;
        }

        if (tags.has("leisure") && tags.getString("leisure").equals("park")) {
            return PoiType.PARK;
        }

        if (tags.has("highway") && tags.getString("highway").equals("bus_stop")) {
            return PoiType.BUS_STOP;
        }

        return null;
    }
}