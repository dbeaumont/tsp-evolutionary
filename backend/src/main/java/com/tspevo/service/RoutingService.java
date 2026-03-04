package com.tspevo.service;

import com.tspevo.model.City;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoutingService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    private static final String OSRM_API = "http://router.project-osrm.org/route/v1/driving/";
    
    private final RestTemplate restTemplate;
    
    public RoutingService() {
        this.restTemplate = new RestTemplate();
    }
    
    public double getDistanceKm(City from, City to) {
        try {
            String url = String.format("%s%s,%s;%s,%s?overview=false", 
                OSRM_API, from.getY(), from.getX(), to.getY(), to.getX());
            
            OsrmResponse response = restTemplate.getForObject(url, OsrmResponse.class);
            
            if (response != null && response.getRoutes() != null && !response.getRoutes().isEmpty()) {
                return response.getRoutes().get(0).getDistance() / 1000.0;
            }
        } catch (Exception e) {
            logger.warn("Failed to get distance from {} to {}: {}", from.getName(), to.getName(), e.getMessage());
        }
        return haversineDistance(from, to);
    }
    
    public List<double[]> getRouteGeometry(City from, City to) {
        List<double[]> coordinates = new ArrayList<>();
        try {
            String url = String.format("%s%s,%s;%s,%s?overview=full&geometries=geojson", 
                OSRM_API, from.getY(), from.getX(), to.getY(), to.getX());
            
            OsrmResponse response = restTemplate.getForObject(url, OsrmResponse.class);
            
            if (response != null && response.getRoutes() != null && !response.getRoutes().isEmpty()) {
                OsrmGeometry geometry = response.getRoutes().get(0).getGeometry();
                if (geometry != null && geometry.getCoordinates() != null) {
                    for (List<Double> coord : geometry.getCoordinates()) {
                        coordinates.add(new double[]{coord.get(1), coord.get(0)});
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get route from {} to {}: {}", from.getName(), to.getName(), e.getMessage());
        }
        return coordinates;
    }
    
    private double haversineDistance(City c1, City c2) {
        final double R = 6371.0;
        double lat1 = Math.toRadians(c1.getX());
        double lat2 = Math.toRadians(c2.getX());
        double lon1 = Math.toRadians(c1.getY());
        double lon2 = Math.toRadians(c2.getY());
        
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    static class OsrmResponse {
        private List<OsrmRoute> routes;
        
        public List<OsrmRoute> getRoutes() { return routes; }
        public void setRoutes(List<OsrmRoute> routes) { this.routes = routes; }
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
        private List<List<Double>> coordinates;
        
        public List<List<Double>> getCoordinates() { return coordinates; }
        public void setCoordinates(List<List<Double>> coordinates) { this.coordinates = coordinates; }
    }
}
