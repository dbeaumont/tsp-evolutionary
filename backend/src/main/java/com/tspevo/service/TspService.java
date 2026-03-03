package com.tspevo.service;

import com.tspevo.model.City;
import com.tspevo.model.TspResult;
import com.tspevo.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class TspService {
    
    @Autowired
    private CityRepository cityRepository;
    
    @Autowired
    private EvolutionaryAlgorithm evolutionaryAlgorithm;
    
    private static final String[][] FRENCH_CITIES = {
        {"Paris", "48.8566", "2.3522"},
        {"London", "51.5074", "-0.1278"},
        {"Berlin", "52.5200", "13.4050"},
        {"Madrid", "40.4168", "-3.7038"},
        {"Rome", "41.9028", "12.4964"},
        {"Amsterdam", "52.3676", "4.9041"},
        {"Vienna", "48.2082", "16.3738"},
        {"Brussels", "50.8503", "4.3517"},
        {"Munich", "48.1351", "11.5820"},
        {"Milan", "45.4642", "9.1900"},
        {"Barcelona", "41.3851", "2.1734"},
        {"Prague", "50.0755", "14.4378"},
        {"Dublin", "53.3498", "-6.2603"},
        {"Lisbon", "38.7223", "-9.1393"},
        {"Copenhagen", "55.6761", "12.5683"},
        {"Stockholm", "59.3293", "18.0686"},
        {"Oslo", "59.9139", "10.7522"},
        {"Helsinki", "60.1699", "24.9384"},
        {"Warsaw", "52.2297", "21.0122"},
        {"Budapest", "47.4979", "19.0402"},
        {"Athens", "37.9838", "23.7275"},
        {"Zurich", "47.3769", "8.5417"},
        {"Geneva", "46.2044", "6.1432"},
        {"Manchester", "53.4808", "-2.2426"},
        {"Birmingham", "52.4862", "-1.8904"},
        {"Glasgow", "55.8642", "-4.2518"},
        {"Frankfurt", "50.1109", "8.6821"},
        {"Cologne", "50.9375", "6.9603"},
        {"Hamburg", "53.5511", "9.9937"},
        {"Marseille", "43.2965", "5.3698"},
        {"Lyon", "45.7640", "4.8357"},
        {"Toulouse", "43.6047", "1.4442"},
        {"Nice", "43.7102", "7.2620"},
        {"Nantes", "47.2184", "-1.5536"},
        {"Strasbourg", "48.5734", "7.7521"},
        {"Bordeaux", "44.8378", "-0.5792"},
        {"Lille", "50.6292", "3.0573"},
        {"Florence", "43.7696", "11.2558"},
        {"Venice", "45.4408", "12.3155"},
        {"Naples", "40.8518", "14.2681"},
        {"Turin", "45.0703", "7.6869"},
        {"Palermo", "38.1157", "13.3615"},
        {"Rotterdam", "51.9225", "4.4792"},
        {"TheHague", "52.0705", "4.3007"},
        {"Utrecht", "52.0907", "5.1214"},
        {"Edinburgh", "55.9533", "-3.1883"},
        {"Bristol", "51.4545", "-2.5879"},
        {"Leeds", "53.8008", "-1.5491"},
        {"Liverpool", "53.4084", "-2.9916"},
        {"Stuttgart", "48.7758", "9.1829"},
        {"Dusseldorf", "51.2277", "6.7735"},
        {"Dortmund", "51.5136", "7.4653"},
        {"Essen", "51.4556", "7.0116"},
        {"Leipzig", "51.3397", "12.3731"},
        {"Dresden", "51.0504", "13.7373"},
        {"Hanover", "52.3759", "9.7320"},
        {"Nuremberg", "49.4521", "11.0767"},
        {"Duisburg", "51.4344", "6.7623"},
        {"Bochum", "51.4818", "7.2162"},
        {"Wuppertal", "51.2562", "7.1508"},
        {"Bremen", "53.0793", "8.8017"},
        {"Freiburg", "47.9990", "7.8421"},
        {"Karlsruhe", "49.0069", "8.4037"},
        {"Mannheim", "49.4875", "8.4660"},
        {"Augsburg", "48.3705", "10.8978"},
        {"Wiesbaden", "50.0826", "8.2400"},
        {"Mönchengladbach", "51.1805", "6.4428"},
        {"Gelsenkirchen", "51.5177", "7.0857"},
        {"Braunschweig", "52.2689", "10.5268"},
        {"Kiel", "54.3233", "10.1228"},
        {"Chemnitz", "50.8278", "12.9214"},
        {"Aachen", "50.7753", "6.0839"},
        {"Halle", "51.4825", "11.9697"},
        {"Magdeburg", "52.1205", "11.6276"},
        {"Freiburg", "47.9990", "7.8421"},
        {"Lubeck", "53.8655", "10.6866"},
        {"Oberhausen", "51.4963", "6.8516"},
        {"Erfurt", "50.9848", "11.0299"},
        {"Mainz", "49.9929", "8.2473"},
        {"Rostock", "54.0887", "12.1404"},
        {"Kassel", "51.3127", "9.4797"},
        {"Hagen", "51.3671", "7.4632"},
        {"Hamm", "51.6739", "7.8150"},
        {"Saarbrucken", "49.2401", "6.9969"},
        {"Mulhouse", "47.7508", "7.3361"},
        {"Orleans", "47.9025", "1.9091"},
        {"Angers", "47.4784", "-0.5632"},
        {"Dijon", "47.3220", "5.0415"},
        {"Brest", "48.3905", "-4.4865"},
        {"Grenoble", "45.1875", "5.7355"},
        {"Reims", "49.2583", "4.0317"},
        {"SaintEtienne", "45.5017", "4.3873"},
        {"Rouen", "49.4404", "1.0939"},
        {"Caen", "49.1829", "-0.3707"},
        {"Nancy", "48.6921", "6.1844"},
        {"Metz", "49.1193", "6.1757"},
        {"Amiens", "49.8942", "2.2953"},
        {"Perpignan", "42.6986", "2.8956"},
        {"Bologna", "44.4949", "11.3426"},
        {"Valencia", "39.4699", "-0.3763"}
    };
    
    public City addCity(City city) {
        return cityRepository.save(city);
    }
    
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
    
    public void deleteAllCities() {
        cityRepository.deleteAll();
    }
    
    public TspResult optimize() {
        List<City> cities = cityRepository.findAll();
        return evolutionaryAlgorithm.solve(cities);
    }
    
    public TspResult optimizeWithProgress(Consumer<TspProgress> progressCallback) {
        List<City> cities = cityRepository.findAll();
        return evolutionaryAlgorithm.solve(cities, progressCallback);
    }
    
    public List<City> seedCities() {
        cityRepository.deleteAll();
        
        for (String[] cityData : FRENCH_CITIES) {
            City city = new City(cityData[0], Double.parseDouble(cityData[1]), Double.parseDouble(cityData[2]));
            cityRepository.save(city);
        }
        
        return cityRepository.findAll();
    }
}
