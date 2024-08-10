package com.projectpandas.ridemory.ride;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import com.projectpandas.ridemory.util.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RideService {
    @Autowired
    RideRepository rideRepository;

    private static final Logger logger = LoggerFactory.getLogger(RideService.class);

    public Ride createRide(Ride ride) {
        ride = rideRepository.save(ride);
        return ride;
    }

    /**
     * Helper method to generate some random rides. Should not be called in a
     * production context!
     *
     * @param quantity the amount of rides to generate
     * @return a list of generated rides.
     */
    public List<Ride> generateRides(int quantity) {
        Location[] locations = Location.values;
        List<Ride> generatedRides = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            Location from = locations[new Random().nextInt(locations.length)];
            Location to = locations[new Random().nextInt(locations.length)];

            Ride ride = new Ride(new User(), to, from);
            generatedRides.add(ride);
            rideRepository.save(ride);
        }

        return generatedRides;
    }

    public Ride getRide(ObjectId id) {
        return rideRepository.findById(id).orElse(null);
    }

    public Ride deleteRide(String id) {
        return rideRepository.deleteRideById(id);
    }

    /**
     * Search for rides matching the given filters.
     *
     * @param from starting location in lat,lng format
     * @param to destination location in lat,lng format
     * @param radius search radius in feet (default exact search)
     * @param space minimum number of available seats (default 1)
     * @param time time of departure in unix epoch time (default present)
     * @param after true if time is after, false if before (default true)
     * @return list of rides matching the filters
     */
    public List<Ride> searchRides(String from, String to, Double radius, Integer space, Long time, Boolean after) {
        GeoJsonPoint fromPoint = convertToPoint(from);
        GeoJsonPoint toPoint = convertToPoint(to);

        if (fromPoint == null || toPoint == null) {
            return null;
        }

        Distance maxDistance = radius == null
                ? new Distance(0, Metrics.MILES)
                : new Distance(radius / 5280, Metrics.MILES);
        maxDistance = maxDistance.in(Metrics.KILOMETERS);

        space = space == null ? 1 : space;
        time = time == null ? System.currentTimeMillis() / 1000L : time;
        after = after == null ? true : after;

        return rideRepository.getRidesBy(fromPoint, toPoint, maxDistance.getValue() * 1000, space, time, after);
    }

    /**
     * Convert a string to a GeoJsonPoint.
     *
     * @param source string to convert in lat,lng format
     * @return GeoJsonPoint
     */
    private GeoJsonPoint convertToPoint(String source) {
        try {
            String[] coordinates = source.split(",");
            double lng = Double.parseDouble(coordinates[0].trim());
            double lat = Double.parseDouble(coordinates[1].trim());
            return new GeoJsonPoint(lng, lat);
        } catch (Exception e) {
            logger.warn("{} is an invalid coordinate format! Expected source to be `longitude,latitude` pair.", source);
        }

        return null;
    }
}
