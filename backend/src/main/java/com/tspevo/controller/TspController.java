package com.tspevo.controller;

import com.tspevo.model.City;
import com.tspevo.model.TspResult;
import com.tspevo.service.TspProgress;
import com.tspevo.service.TspService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TspController {
    
    private static final Logger logger = LoggerFactory.getLogger(TspController.class);
    
    @Autowired
    private TspService tspService;
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    @PostMapping("/cities")
    public ResponseEntity<City> addCity(@RequestBody City city) {
        return ResponseEntity.ok(tspService.addCity(city));
    }
    
    @GetMapping("/cities")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(tspService.getAllCities());
    }
    
    @DeleteMapping("/cities")
    public ResponseEntity<Void> deleteAllCities() {
        tspService.deleteAllCities();
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/tsp/optimize")
    public ResponseEntity<TspResult> optimize() {
        return ResponseEntity.ok(tspService.optimize());
    }
    
    @GetMapping("/tsp/optimize/stream")
    public ResponseEntity<SseEmitter> optimizeStream() {
        logger.info("Starting SSE stream for TSP optimization");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        executor.execute(() -> {
            try {
                tspService.optimizeWithProgress(progress -> {
                    try {
                        logger.debug("Sending progress: generation {}", progress.getGeneration());
                        emitter.send(SseEmitter.event()
                            .name("progress")
                            .data(progress));
                    } catch (Exception e) {
                        logger.error("Error sending progress", e);
                        emitter.completeWithError(e);
                    }
                });
                logger.info("Optimization complete, closing emitter");
                emitter.complete();
            } catch (Exception e) {
                logger.error("Error in optimization", e);
                emitter.completeWithError(e);
            }
        });
        
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(emitter);
    }
    
    @PostMapping("/cities/seed")
    public ResponseEntity<List<City>> seedCities() {
        return ResponseEntity.ok(tspService.seedCities());
    }
}
