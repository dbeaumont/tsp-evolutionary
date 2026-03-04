package com.tspevo.service;

import com.tspevo.model.City;
import com.tspevo.model.TspResult;
import com.tspevo.repository.CityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class TspService {
    
    private static final Logger logger = LoggerFactory.getLogger(TspService.class);
    private static final String CSV_HEADER =
        "record_type,city_ref,city_name,latitude,longitude,from_city_ref,from_city_name,to_city_ref,to_city_name,distance_km";
    private static final List<RealCity> EUROPEAN_REFERENCE_CITIES = List.of(
        new RealCity("Paris", 48.8566, 2.3522),
        new RealCity("London", 51.5074, -0.1278),
        new RealCity("Berlin", 52.5200, 13.4050),
        new RealCity("Madrid", 40.4168, -3.7038),
        new RealCity("Rome", 41.9028, 12.4964),
        new RealCity("Amsterdam", 52.3676, 4.9041),
        new RealCity("Vienna", 48.2082, 16.3738),
        new RealCity("Brussels", 50.8503, 4.3517),
        new RealCity("Munich", 48.1351, 11.5820),
        new RealCity("Milan", 45.4642, 9.1900),
        new RealCity("Barcelona", 41.3851, 2.1734),
        new RealCity("Prague", 50.0755, 14.4378),
        new RealCity("Dublin", 53.3498, -6.2603),
        new RealCity("Lisbon", 38.7223, -9.1393),
        new RealCity("Copenhagen", 55.6761, 12.5683),
        new RealCity("Stockholm", 59.3293, 18.0686),
        new RealCity("Oslo", 59.9139, 10.7522),
        new RealCity("Helsinki", 60.1699, 24.9384),
        new RealCity("Warsaw", 52.2297, 21.0122),
        new RealCity("Budapest", 47.4979, 19.0402),
        new RealCity("Athens", 37.9838, 23.7275),
        new RealCity("Zurich", 47.3769, 8.5417),
        new RealCity("Geneva", 46.2044, 6.1432),
        new RealCity("Manchester", 53.4808, -2.2426),
        new RealCity("Birmingham", 52.4862, -1.8904),
        new RealCity("Glasgow", 55.8642, -4.2518),
        new RealCity("Frankfurt", 50.1109, 8.6821),
        new RealCity("Cologne", 50.9375, 6.9603),
        new RealCity("Hamburg", 53.5511, 9.9937),
        new RealCity("Marseille", 43.2965, 5.3698),
        new RealCity("Lyon", 45.7640, 4.8357),
        new RealCity("Toulouse", 43.6047, 1.4442),
        new RealCity("Nice", 43.7102, 7.2620),
        new RealCity("Nantes", 47.2184, -1.5536),
        new RealCity("Strasbourg", 48.5734, 7.7521),
        new RealCity("Bordeaux", 44.8378, -0.5792),
        new RealCity("Lille", 50.6292, 3.0573),
        new RealCity("Florence", 43.7696, 11.2558),
        new RealCity("Venice", 45.4408, 12.3155),
        new RealCity("Naples", 40.8518, 14.2681),
        new RealCity("Turin", 45.0703, 7.6869),
        new RealCity("Palermo", 38.1157, 13.3615),
        new RealCity("Rotterdam", 51.9225, 4.4792),
        new RealCity("The Hague", 52.0705, 4.3007),
        new RealCity("Utrecht", 52.0907, 5.1214),
        new RealCity("Edinburgh", 55.9533, -3.1883),
        new RealCity("Bristol", 51.4545, -2.5879),
        new RealCity("Leeds", 53.8008, -1.5491),
        new RealCity("Liverpool", 53.4084, -2.9916),
        new RealCity("Stuttgart", 48.7758, 9.1829)
    );
    
    @Autowired
    private CityRepository cityRepository;
    
    @Autowired
    private EvolutionaryAlgorithm evolutionaryAlgorithm;
    
    @Autowired
    private RoutingService routingService;
    
    private final Map<String, Double> distanceCache = new ConcurrentHashMap<>();
    
    public City addCity(City city) {
        return cityRepository.save(city);
    }
    
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
    
    public void deleteAllCities() {
        cityRepository.deleteAll();
        distanceCache.clear();
    }
    
    public double getDistance(City from, City to) {
        if (from.getId() == null || to.getId() == null) {
            return routingService.getDistanceKm(from, to);
        }
        String key = from.getId() + "-" + to.getId();
        return distanceCache.computeIfAbsent(key, k -> {
            String reverseKey = to.getId() + "-" + from.getId();
            if (distanceCache.containsKey(reverseKey)) {
                return distanceCache.get(reverseKey);
            }
            return routingService.getDistanceKm(from, to);
        });
    }
    
    public List<double[]> getRouteGeometry(City from, City to) {
        return routingService.getRouteGeometry(from, to);
    }
    
    public TspResult optimize() {
        List<City> cities = cityRepository.findAll();
        return evolutionaryAlgorithm.solve(cities, this::getDistance);
    }
    
    public TspResult optimizeWithProgress(Consumer<TspProgress> progressCallback) {
        List<City> cities = cityRepository.findAll();
        return evolutionaryAlgorithm.solve(cities, this::getDistance, progressCallback);
    }

    public byte[] buildGeneratedCitiesCsv(int cityCount) {
        if (cityCount < 2) {
            throw new IllegalArgumentException("Le nombre de villes doit etre au moins 2.");
        }
        if (cityCount > EUROPEAN_REFERENCE_CITIES.size()) {
            throw new IllegalArgumentException(
                "Le nombre de villes maximum est " + EUROPEAN_REFERENCE_CITIES.size() + ".");
        }

        List<City> cities = generateEuropeanCities(cityCount);

        StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER).append('\n');

        long cityRef = 1L;
        for (City city : cities) {
            csv.append("CITY,")
                .append(cityRef).append(',')
                .append(csvValue(city.getName())).append(',')
                .append(String.format(Locale.US, "%.6f", city.getX())).append(',')
                .append(String.format(Locale.US, "%.6f", city.getY()))
                .append(",,,,,")
                .append('\n');
            cityRef++;
        }

        for (int i = 0; i < cities.size(); i++) {
            City from = cities.get(i);
            for (int j = i + 1; j < cities.size(); j++) {
                City to = cities.get(j);
                double distanceKm = routingService.getDistanceKm(from, to);
                csv.append("DISTANCE,,,,,")
                    .append(i + 1L).append(',')
                    .append(csvValue(from.getName())).append(',')
                    .append(j + 1L).append(',')
                    .append(csvValue(to.getName())).append(',')
                    .append(String.format(Locale.US, "%.3f", distanceKm))
                    .append('\n');
            }
        }

        logger.info("Generated CSV for {} real european cities", cities.size());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public Map<String, Object> importCitiesAndDistancesCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier CSV est vide ou absent.");
        }

        List<CsvCityRow> cityRows = new java.util.ArrayList<>();
        Map<String, Double> providedDistancesByPair = new HashMap<>();
        List<long[]> providedDistanceRefs = new java.util.ArrayList<>();
        Map<Long, CsvCityRow> cityRowsByRef = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.trim().toLowerCase(Locale.ROOT).startsWith("record_type,")) {
                    continue;
                }
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                List<String> columns = parseCsvLine(line);
                String recordType = getColumn(columns, 0).trim().toUpperCase(Locale.ROOT);
                if ("CITY".equals(recordType)) {
                    long cityRef = parseLong(getColumn(columns, 1), "city_ref", lineNumber);
                    String cityName = getColumn(columns, 2).trim();
                    if (cityName.isEmpty()) {
                        throw new IllegalArgumentException("Nom de ville manquant (ligne " + lineNumber + ").");
                    }
                    double latitude = parseDouble(getColumn(columns, 3), "latitude", lineNumber);
                    double longitude = parseDouble(getColumn(columns, 4), "longitude", lineNumber);

                    if (cityRowsByRef.containsKey(cityRef)) {
                        throw new IllegalArgumentException(
                            "city_ref dupliqué (" + cityRef + ") à la ligne " + lineNumber + ".");
                    }
                    CsvCityRow cityRow = new CsvCityRow(cityRef, cityName, latitude, longitude);
                    cityRowsByRef.put(cityRef, cityRow);
                    cityRows.add(cityRow);
                    continue;
                }

                if ("DISTANCE".equals(recordType)) {
                    long fromRef = parseLong(getColumn(columns, 5), "from_city_ref", lineNumber);
                    long toRef = parseLong(getColumn(columns, 7), "to_city_ref", lineNumber);
                    double distanceKm = parseDouble(getColumn(columns, 9), "distance_km", lineNumber);
                    if (fromRef == toRef) {
                        throw new IllegalArgumentException(
                            "from_city_ref et to_city_ref identiques à la ligne " + lineNumber + ".");
                    }
                    String pairKey = canonicalRefPairKey(fromRef, toRef);
                    if (providedDistancesByPair.containsKey(pairKey)) {
                        throw new IllegalArgumentException(
                            "Distance dupliquee pour la paire " + fromRef + " / " + toRef + " (ligne " + lineNumber + ").");
                    }
                    providedDistancesByPair.put(pairKey, distanceKm);
                    providedDistanceRefs.add(new long[]{fromRef, toRef});
                    continue;
                }

                throw new IllegalArgumentException(
                    "record_type invalide '" + recordType + "' à la ligne " + lineNumber
                        + ". Valeurs attendues: CITY ou DISTANCE.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Impossible de lire le fichier CSV.", e);
        }

        if (cityRows.isEmpty()) {
            throw new IllegalArgumentException("Aucune ligne CITY valide trouvée dans le CSV.");
        }
        for (long[] refs : providedDistanceRefs) {
            if (!cityRowsByRef.containsKey(refs[0]) || !cityRowsByRef.containsKey(refs[1])) {
                throw new IllegalArgumentException(
                    "Référence de ville inconnue dans DISTANCE: " + refs[0] + " -> " + refs[1]);
            }
        }

        cityRows.sort(Comparator.comparingLong(CsvCityRow::ref));
        cityRepository.deleteAll();
        distanceCache.clear();

        Map<Long, City> savedCitiesByRef = new HashMap<>();
        for (CsvCityRow cityRow : cityRows) {
            City saved = cityRepository.save(new City(cityRow.name(), cityRow.latitude(), cityRow.longitude()));
            savedCitiesByRef.put(cityRow.ref(), saved);
        }

        int totalPairs = cityRows.size() * (cityRows.size() - 1) / 2;
        int osrmCalculatedPairs = 0;

        for (int i = 0; i < cityRows.size(); i++) {
            CsvCityRow fromRow = cityRows.get(i);
            City fromCity = savedCitiesByRef.get(fromRow.ref());
            if (fromCity == null || fromCity.getId() == null) {
                continue;
            }
            for (int j = i + 1; j < cityRows.size(); j++) {
                CsvCityRow toRow = cityRows.get(j);
                City toCity = savedCitiesByRef.get(toRow.ref());
                if (toCity == null || toCity.getId() == null) {
                    continue;
                }

                String pairKey = canonicalRefPairKey(fromRow.ref(), toRow.ref());
                Double providedDistance = providedDistancesByPair.get(pairKey);
                double distanceKm;
                if (providedDistance != null) {
                    distanceKm = providedDistance;
                } else {
                    distanceKm = routingService.getDistanceKm(fromCity, toCity);
                    osrmCalculatedPairs++;
                }

                distanceCache.put(cacheKey(fromCity.getId(), toCity.getId()), distanceKm);
                distanceCache.put(cacheKey(toCity.getId(), fromCity.getId()), distanceKm);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("citiesImported", savedCitiesByRef.size());
        result.put("distancePairsProvided", providedDistancesByPair.size());
        result.put("distancePairsCalculated", osrmCalculatedPairs);
        result.put("distancePairsTotal", totalPairs);
        result.put("distanceEntriesCached", totalPairs * 2);
        logger.info(
            "CSV imported: {} cities, {} provided pairs, {} calculated pairs, {} cache entries",
            savedCitiesByRef.size(), providedDistancesByPair.size(), osrmCalculatedPairs, totalPairs * 2
        );
        return result;
    }

    private List<City> generateEuropeanCities(int cityCount) {
        List<City> cities = new java.util.ArrayList<>(cityCount);
        for (int i = 0; i < cityCount; i++) {
            RealCity reference = EUROPEAN_REFERENCE_CITIES.get(i);
            cities.add(new City(reference.name(), reference.latitude(), reference.longitude()));
        }
        return cities;
    }

    private static String cacheKey(Long fromCityId, Long toCityId) {
        return fromCityId + "-" + toCityId;
    }

    private static String canonicalRefPairKey(long a, long b) {
        if (a < b) {
            return a + "-" + b;
        }
        return b + "-" + a;
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (ch == ',' && !inQuotes) {
                columns.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        columns.add(current.toString());
        return columns;
    }

    private String getColumn(List<String> columns, int index) {
        if (index < columns.size()) {
            return columns.get(index);
        }
        return "";
    }

    private long parseLong(String value, String fieldName, int lineNumber) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Valeur invalide pour " + fieldName + " à la ligne " + lineNumber + ": '" + value + "'.");
        }
    }

    private double parseDouble(String value, String fieldName, int lineNumber) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Valeur invalide pour " + fieldName + " à la ligne " + lineNumber + ": '" + value + "'.");
        }
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (!needsQuotes) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private record RealCity(String name, double latitude, double longitude) {}
    private record CsvCityRow(long ref, String name, double latitude, double longitude) {}
}
