package com.smarttransit.route.service;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GPXParserService {

    /**
     * Parsea un archivo GPX y extrae los puntos del recorrido
     */
    public List<List<Double>> parseGPXFile(MultipartFile file) throws IOException {
        // Crear archivo temporal para leer con JPX
        Path tempFile = Files.createTempFile("gpx-", ".gpx");
        try {
            // Copiar contenido del MultipartFile al archivo temporal
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Leer GPX desde el archivo temporal
            GPX gpx = GPX.read(tempFile);
            List<List<Double>> polyline = new ArrayList<>();

            // Extraer puntos de tracks
            for (Track track : gpx.getTracks()) {
                for (TrackSegment segment : track.getSegments()) {
                    List<List<Double>> points = segment.getPoints().stream()
                        .map(wayPoint -> List.of(
                            wayPoint.getLatitude().doubleValue(),
                            wayPoint.getLongitude().doubleValue()
                        ))
                        .collect(Collectors.toList());
                    polyline.addAll(points);
                }
            }

            // Si no hay tracks, intentar con waypoints de ruta
            if (polyline.isEmpty()) {
                List<List<Double>> routePoints = gpx.getRoutes().stream()
                    .flatMap(route -> route.getPoints().stream())
                    .map(wayPoint -> List.of(
                        wayPoint.getLatitude().doubleValue(),
                        wayPoint.getLongitude().doubleValue()
                    ))
                    .collect(Collectors.toList());
                polyline.addAll(routePoints);
            }

            // Si tampoco hay rutas, usar waypoints directos
            if (polyline.isEmpty()) {
                List<List<Double>> waypoints = gpx.getWayPoints().stream()
                    .map(wayPoint -> List.of(
                        wayPoint.getLatitude().doubleValue(),
                        wayPoint.getLongitude().doubleValue()
                    ))
                    .collect(Collectors.toList());
                polyline.addAll(waypoints);
            }

            return polyline;

        } catch (Exception e) {
            throw new IOException("Error al parsear archivo GPX: " + e.getMessage(), e);
        } finally {
            // Limpiar archivo temporal
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // Ignorar errores al eliminar archivo temporal
            }
        }
    }

    /**
     * Extrae paradas del archivo GPX (waypoints con nombres)
     */
    public List<WayPoint> extractStops(MultipartFile file) throws IOException {
        // Crear archivo temporal para leer con JPX
        Path tempFile = Files.createTempFile("gpx-stops-", ".gpx");
        try {
            // Copiar contenido del MultipartFile al archivo temporal
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Leer GPX desde el archivo temporal
            GPX gpx = GPX.read(tempFile);
            return gpx.getWayPoints().stream()
                .filter(wp -> wp.getName().isPresent())
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IOException("Error al extraer paradas del GPX: " + e.getMessage(), e);
        } finally {
            // Limpiar archivo temporal
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // Ignorar errores al eliminar archivo temporal
            }
        }
    }
}

