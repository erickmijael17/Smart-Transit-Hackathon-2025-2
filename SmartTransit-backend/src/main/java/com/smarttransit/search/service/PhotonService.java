package com.smarttransit.search.service;

import com.smarttransit.mapa.dto.GeocodingResponse;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PhotonService {

    private static final Logger log = LoggerFactory.getLogger(PhotonService.class);

    @Value("${photon.api.url}")
    private String photonApiUrl;

    private final OkHttpClient httpClient;

    public PhotonService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Búsqueda con autocompletado usando Photon.
     * @param query Texto a buscar (ej: "pizza", "banco", "hospital cerca")
     * @param lat Latitud del usuario (opcional, para priorizar resultados cercanos)
     * @param lon Longitud del usuario (opcional)
     * @param limit Número máximo de resultados
     */
    @Cacheable(value = "photon-search", key = "#query + '_' + #lat + '_' + #lon")
    public List<GeocodingResponse> search(String query, Double lat, Double lon, int limit) throws IOException {
        log.info("Photon search: query='{}', lat={}, lon={}", query, lat, lon);

        // Construir URL con parámetros de forma segura
        HttpUrl.Builder urlBuilder = HttpUrl.parse(photonApiUrl).newBuilder()
                .addQueryParameter("q", query)
                .addQueryParameter("limit", String.valueOf(limit));

        // Agregar coordenadas si están disponibles (prioriza resultados cercanos)
        if (lat != null && lon != null) {
            urlBuilder.addQueryParameter("lat", lat.toString());
            urlBuilder.addQueryParameter("lon", lon.toString());
        }

        // Filtrar por idioma español (prioriza nombres en español)
        urlBuilder.addQueryParameter("lang", "es");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Photon API error: " + response.code());
            }

            String responseBody = response.body().string();
            return parsePhotonResponse(responseBody);
        }
    }

    /**
     * Parsea la respuesta de Photon (que está en formato GeoJSON).
     */
    private List<GeocodingResponse> parsePhotonResponse(String responseBody) {
        List<GeocodingResponse> results = new ArrayList<>();
        JSONObject json = new JSONObject(responseBody);
        JSONArray features = json.getJSONArray("features");

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject geometry = feature.getJSONObject("geometry");
            JSONObject properties = feature.getJSONObject("properties");

            // Extraer coordenadas [longitud, latitud]
            JSONArray coordinates = geometry.getJSONArray("coordinates");
            double lon = coordinates.getDouble(0);
            double lat = coordinates.getDouble(1);

            // Construir un nombre descriptivo y legible
            String displayName = buildDisplayName(properties);

            // Usamos el mismo DTO de respuesta que Geocoding para mantener la consistencia
            GeocodingResponse response = GeocodingResponse.builder()
                    .latitude(lat)
                    .longitude(lon)
                    .displayName(displayName)
                    .city(properties.optString("city", null))
                    .country(properties.optString("country", null))
                    .postcode(properties.optString("postcode", null))
                    .street(properties.optString("street", null))
                    .build();

            results.add(response);
        }

        log.info("Photon returned {} results", results.size());
        return results;
    }

    /**
     * Construye un nombre completo y legible para el lugar desde las propiedades de Photon.
     */
    private String buildDisplayName(JSONObject properties) {
        StringBuilder nameBuilder = new StringBuilder();

        if (properties.has("name")) {
            nameBuilder.append(properties.getString("name"));
        }

        // Añadir detalles de la dirección si están disponibles
        String street = properties.optString("street", null);
        String city = properties.optString("city", properties.optString("locality", null));
        String country = properties.optString("country", null);

        if (street != null) {
            if (!nameBuilder.isEmpty()) nameBuilder.append(", ");
            nameBuilder.append(street);
        }
        if (city != null && !nameBuilder.toString().contains(city)) {
            if (!nameBuilder.isEmpty()) nameBuilder.append(", ");
            nameBuilder.append(city);
        }
        if (country != null && !nameBuilder.toString().contains(country)) {
            if (!nameBuilder.isEmpty()) nameBuilder.append(", ");
            nameBuilder.append(country);
        }

        return nameBuilder.toString();
    }
}