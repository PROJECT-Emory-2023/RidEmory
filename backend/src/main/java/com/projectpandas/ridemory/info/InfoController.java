package com.projectpandas.ridemory.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.projectpandas.ridemory.ride.Ride;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/info")
public class InfoController {
    @Autowired
    InfoService service;

    @GetMapping("/")
    public boolean test() {
        return true;
    }

    @GetMapping("/atl")
    public Map<String, Integer> getATLWaitTime() {
        return service.getATLWaitTime();
    }

    @PostMapping("/duration")
    public String getTripDuration(@RequestBody Ride ride) {
        return service.getTrafficTimeEstimate(ride);
        // return service.getTrafficaTimeEstimate(ride);
    }

    @GetMapping("/trendingLocation")
    public List<SortRidesByLocation> getTrendingLocation() {
        return service.getTrendingLocation();
    }
}
