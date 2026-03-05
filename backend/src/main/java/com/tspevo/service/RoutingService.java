package com.tspevo.service;

import com.tspevo.service.OsrmConnector.OsrmDistanceResponse;
import com.tspevo.service.OsrmConnector.OsrmGeometryResponse;
import com.tspevo.model.City;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoutingService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    
    private final OsrmConnector osrmConnector;
    private final ConcurrentHashMap<String, Double> distanceCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<double[]>> geometryCache = new ConcurrentHashMap<>();
    
    public RoutingService(OsrmConnector osrmConnector) {
        this.osrmConnector = osrmConnector;
    }
    
    private String getCacheKey(long fromId, long toId) {
        long min = Math.min(fromId, toId);
        long max = Math.max(fromId, toId);
        return min + "-" + max;
    }
    
    public double getDistanceKm(City from, City to) {
        long fromId = from.getId() != null ? from.getId() : from.getName().hashCode();
        long toId = to.getId() != null ? to.getId() : to.getName().hashCode();
        String cacheKey = getCacheKey(fromId, toId);
        
        Double cached = distanceCache.get(cacheKey);
        if (cached != null) {
            logger.debug("Using cached distance for {} -> {}: {} km", from.getName(), to.getName(), cached);
            return cached;
        }
        
        logger.info("Calling OSRM for distance: {} -> {}", from.getName(), to.getName());
        
        OsrmDistanceResponse response = osrmConnector.fetchDistance(
            from.getY(), from.getX(), to.getY(), to.getX());
        
        if (response.success()) {
            logger.info("OSRM distance {} -> {}: {} km", from.getName(), to.getName(), response.distance());
            distanceCache.put(cacheKey, response.distance());
            return response.distance();
        }
        
        logger.warn("OSRM failed, using Haversine fallback for {} -> {}", from.getName(), to.getName());
        double distance = haversineDistance(from, to);
        distanceCache.put(cacheKey, distance);
        return distance;
    }
    
    public List<double[]> getRouteGeometry(City from, City to) {
        long fromId = from.getId() != null ? from.getId() : from.getName().hashCode();
        long toId = to.getId() != null ? to.getId() : to.getName().hashCode();
        String cacheKey = getCacheKey(fromId, toId);
        
        List<double[]> cached = geometryCache.get(cacheKey);
        if (cached != null) {
            logger.debug("Using cached geometry for {} -> {}", from.getName(), to.getName());
            return cached;
        }
        
        logger.info("Calling OSRM for route geometry: {} -> {}", from.getName(), to.getName());
        
        OsrmGeometryResponse response = osrmConnector.fetchGeometry(
            from.getY(), from.getX(), to.getY(), to.getX());
        
        if (response.success() && response.coordinates() != null) {
            List<double[]> coordinates = new ArrayList<>();
            for (List<Double> coord : response.coordinates()) {
                coordinates.add(new double[]{coord.get(1), coord.get(0)});
            }
            logger.info("OSRM route {} -> {}: {} points", from.getName(), to.getName(), coordinates.size());
            geometryCache.put(cacheKey, coordinates);
            return coordinates;
        }
        
        logger.warn("OSRM failed, using straight line fallback for {} -> {}", from.getName(), to.getName());
        List<double[]> coords = new ArrayList<>();
        coords.add(new double[]{from.getX(), from.getY()});
        coords.add(new double[]{to.getX(), to.getY()});
        return coords;
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
    
    public void clearCache() {
        distanceCache.clear();
        geometryCache.clear();
        logger.info("Distance and geometry caches cleared");
    }
}
