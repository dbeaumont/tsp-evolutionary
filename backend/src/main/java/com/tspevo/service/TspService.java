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
        {"Marseille", "43.2965", "5.3698"},
        {"Lyon", "45.7640", "4.8357"},
        {"Toulouse", "43.6047", "1.4442"},
        {"Nice", "43.7102", "7.2620"},
        {"Nantes", "47.2184", "-1.5536"},
        {"Strasbourg", "48.5734", "7.7521"},
        {"Montpellier", "43.6108", "3.8767"},
        {"Bordeaux", "44.8378", "-0.5792"},
        {"Lille", "50.6292", "3.0573"},
        {"Rennes", "48.1173", "-1.6778"},
        {"Reims", "49.2583", "4.0317"},
        {"LeHavre", "49.4944", "0.1079"},
        {"SaintEtienne", "45.5017", "4.3873"},
        {"Toulon", "43.1242", "5.9280"},
        {"Grenoble", "45.1875", "5.7355"},
        {"Dijon", "47.3220", "5.0415"},
        {"Angers", "47.4784", "-0.5632"},
        {"Nimes", "43.8384", "4.3601"},
        {"LeMans", "47.9959", "0.1925"},
        {"ClermontFerrand", "45.7772", "3.0870"},
        {"AixEnProvence", "43.5297", "5.4474"},
        {"Brest", "48.3905", "-4.4865"},
        {"Limoges", "45.8143", "1.2677"},
        {"Amiens", "49.8942", "2.2953"},
        {"Metz", "49.1193", "6.1757"},
        {"Perpignan", "42.6986", "2.8956"},
        {"Besancon", "47.2378", "6.0241"},
        {"Orleans", "47.9025", "1.9091"},
        {"Mulhouse", "47.7508", "7.3361"},
        {"Rouen", "49.4404", "1.0939"},
        {"Caen", "49.1829", "-0.3707"},
        {"Nancy", "48.6921", "6.1844"},
        {"SaintDenis", "48.9350", "2.3587"},
        {"Annecy", "45.8992", "6.1289"},
        {"Valence", "44.9333", "4.8916"},
        {"Antibes", "43.5808", "7.1235"},
        {"Chambéry", "45.5647", "5.9348"},
        {"Troyes", "48.2970", "4.0723"},
        {"Avignon", "43.9352", "4.8072"},
        {"SaintMalo", "48.6496", "-2.0259"},
        {"Pau", "43.2950", "-0.3708"},
        {"Bayonne", "43.4929", "-1.4749"},
        {"LaRochelle", "46.1591", "-1.1524"},
        {"Poitiers", "46.5818", "0.3363"},
        {"Bourges", "47.0810", "2.3994"},
        {"Lorient", "47.7320", "-3.3704"},
        {"Cherbourg", "49.6458", "-1.6236"},
        {"Calais", "50.9513", "1.8542"},
        {"Dunkerque", "51.0344", "2.3773"},
        {"Thionville", "49.3667", "6.1667"},
        {"Tourcoing", "50.7239", "3.1588"},
        {"Drancy", "48.9333", "2.4500"},
        {"Colmar", "48.0819", "7.3556"},
        {"Nanterre", "48.8920", "2.2066"},
        {"Versailles", "48.8014", "2.1341"},
        {"SaintPierre", "46.2550", "-1.1500"},
        {"AulnaySousBois", "48.9333", "2.5000"},
        {"ChampignySurMarne", "48.8167", "2.5167"},
        {"Beziers", "43.3500", "3.2167"},
        {"LaCouronne", "45.6333", "0.1500"},
        {"SaintNazaire", "47.2833", "-2.2000"},
        {"Cannes", "43.5528", "7.0174"},
        {"CharlevilleMezieres", "49.7685", "4.7192"},
        {"Quimper", "47.9959", "-4.1019"},
        {"Wattrelos", "50.7000", "3.2167"},
        {"Valenciennes", "50.3667", "3.5333"},
        {"VilleneuveDAscq", "50.6167", "3.1667"},
        {"Vincennes", "48.8446", "2.4391"},
        {"Roanne", "46.0333", "4.0667"},
        {"Moulins", "46.5667", "3.3333"},
        {"Chelles", "48.8833", "2.6000"},
        {"Vannes", "47.6589", "-2.7598"},
        {"Evreux", "49.0243", "1.1515"},
        {"Belfort", "47.6333", "6.8667"},
        {"Albi", "43.9282", "2.1480"},
        {"SaintBrieuc", "48.5154", "-2.7685"},
        {"ChalonsEnChampagne", "48.9578", "4.5286"},
        {"Epinal", "48.1833", "6.4500"},
        {"Bezier", "43.3500", "3.2167"},
        {"Angouleme", "45.6539", "0.1576"},
        {"Nevers", "46.9877", "3.1592"},
        {"Soissons", "49.3817", "3.3231"},
        {"Ax", "42.8000", "1.8333"},
        {"Alencon", "48.4297", "0.0853"},
        {"Bayeux", "49.2768", "-0.7019"},
        {"Chateauroux", "46.8103", "1.6991"},
        {"Blois", "47.5864", "1.3356"},
        {"Brest", "48.3905", "-4.4865"},
        {"Arras", "50.2910", "2.7749"},
        {"Avesnois", "50.1667", "3.9333"},
        {"Douai", "50.3667", "3.0667"},
        {"Maubeuge", "50.2833", "3.9667"},
        {"Forbach", "49.1833", "6.9000"},
        {"Montbeliard", "47.5167", "6.8000"},
        {"SaintLo", "49.1158", "-1.0833"},
        {"Chartres", "48.4439", "1.4888"},
        {"Auxerre", "47.8467", "3.5850"},
        {"BourgoinJallieu", "45.5833", "5.4333"},
        {"Carpentras", "44.0500", "5.0500"},
        {"RomansSurIsere", "45.0333", "5.0500"},
        {"Lannion", "48.7304", "-3.4606"},
        {"ThononLesBains", "46.3667", "6.4833"}
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
