package com.tspevo.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OsrmConnector {

    private static final Logger logger = LoggerFactory.getLogger(OsrmConnector.class);
    private static final String OSRM_API = "http://router.project-osrm.org/route/v1/driving/";

    private final RestTemplate restTemplate;

    public OsrmConnector() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @Retry(name = "osrm", fallbackMethod = "fetchDistanceFallback")
    public OsrmDistanceResponse fetchDistance(double fromY, double fromX, double toY, double toX) {
        String url = String.format("%s%s,%s;%s,%s?overview=false", 
            OSRM_API, fromY, fromX, toY, toX);
        
        logger.info("OSRM fetchDistance: {} -> {}", url);
        
        OsrmResponse response = restTemplate.getForObject(url, OsrmResponse.class);
        
        if (response != null && response.getRoutes() != null && !response.getRoutes().isEmpty()) {
            double distance = response.getRoutes().get(0).getDistance() / 1000.0;
            logger.info("OSRM distance result: {} km", distance);
            return new OsrmDistanceResponse(distance, true);
        }
        
        logger.warn("OSRM returned empty routes");
        return new OsrmDistanceResponse(0, false);
    }

    @Retry(name = "osrm", fallbackMethod = "fetchGeometryFallback")
    public OsrmGeometryResponse fetchGeometry(double fromY, double fromX, double toY, double toX) {
        String url = String.format("%s%s,%s;%s,%s?overview=full&geometries=geojson", 
            OSRM_API, fromY, fromX, toY, toX);
        
        logger.info("OSRM fetchGeometry: {} -> {}", url);
        
        OsrmResponse response = restTemplate.getForObject(url, OsrmResponse.class);
        
        if (response != null && response.getRoutes() != null && !response.getRoutes().isEmpty()) {
            OsrmGeometry geometry = response.getRoutes().get(0).getGeometry();
            if (geometry != null && geometry.getCoordinates() != null) {
                logger.info("OSRM geometry result: {} points", geometry.getCoordinates().size());
                return new OsrmGeometryResponse(geometry.getCoordinates(), true);
            }
        }
        
        logger.warn("OSRM returned empty geometry");
        return new OsrmGeometryResponse(null, false);
    }

    private OsrmDistanceResponse fetchDistanceFallback(double fromY, double fromX, double toY, double toX, Exception e) {
        logger.warn("FALLBACK fetchDistance: {}", e.getMessage());
        return new OsrmDistanceResponse(0, false);
    }

    private OsrmGeometryResponse fetchGeometryFallback(double fromY, double fromX, double toY, double toX, Exception e) {
        logger.warn("FALLBACK fetchGeometry: {}", e.getMessage());
        return new OsrmGeometryResponse(null, false);
    }

    public record OsrmDistanceResponse(double distance, boolean success) {}

    public record OsrmGeometryResponse(java.util.List<java.util.List<Double>> coordinates, boolean success) {}

    static class OsrmResponse {
        private java.util.List<OsrmRoute> routes;
        
        public java.util.List<OsrmRoute> getRoutes() { return routes; }
        public void setRoutes(java.util.List<OsrmRoute> routes) { this.routes = routes; }
    }
    
    static class OsrmRoute {
        private double distance;
        private OsrmGeometry geometry;
        
        public double getDistance() { return distance; }
        public void setDistance(double distance) { this.distance = distance; }
        public OsrmGeometry getGeometry() { return geometry; }
        public void setGeometry(OsrmGeometry geometry) { this.geometry = geometry; }
    }
    
    static class OsrmGeometry {
        private java.util.List<java.util.List<Double>> coordinates;
        
        public java.util.List<java.util.List<Double>> getCoordinates() { return coordinates; }
        public void setCoordinates(java.util.List<java.util.List<Double>> coordinates) { this.coordinates = coordinates; }
    }
}
