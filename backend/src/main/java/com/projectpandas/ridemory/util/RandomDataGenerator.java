package com.projectpandas.ridemory.util;

import com.projectpandas.ridemory.ride.Location;
import com.projectpandas.ridemory.ride.Ride;
import com.projectpandas.ridemory.ride.RideRepository;
import com.projectpandas.ridemory.util.user.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class RandomDataGenerator {
    @Autowired
    RideRepository rides;

    @EventListener(ApplicationReadyEvent.class)
    private void generateRandomData() {
        // Generate random data logic
        List<Ride> allRides = rides.findAll();
        if (allRides.isEmpty()) {
            // Auto-populate with 100 rides randomly
            for (int i = 0; i < 100; i++) {
                Ride ride = generateRandomRide();
                rides.save(ride);
            }
        }
    }

    private Ride generateRandomRide() {
        Random random = new Random();
        Location to = Location.values[random.nextInt(Location.values.length)];
        Location from = Location.values[random.nextInt(Location.values.length)];
        while (from.equals(to)) {
            from = Location.values[random.nextInt(Location.values.length)];
        }

        return new Ride(new User(), to, from);
    }
}
