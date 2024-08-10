package com.projectpandas.ridemory.ride;

import lombok.Data;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.projectpandas.ridemory.util.ObjectIdListSerializer;
import com.projectpandas.ridemory.util.ObjectIdSerializer;
import com.projectpandas.ridemory.util.user.User;

import java.util.ArrayList;
import java.util.List;

@Document("rides")
@CompoundIndexes({@CompoundIndex(name = "location_idx", def = "{'from': '2dsphere', 'to': '2dsphere'}"),
        @CompoundIndex(name = "time_idx", def = "{'departTime': -1}")})
@Data
public class Ride {
    private static final Logger logger = LoggerFactory.getLogger(Ride.class);

    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint to;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint from;
    private long departTime;

    /**
     * Needs to be ignored or serialized to be just count/anonymized users for
     * security reasons, maybe add custom JsonSerializer that converts to anonymous
     * name. Does NOT include organizer
     */
    @JsonSerialize(using = ObjectIdListSerializer.class)
    private List<ObjectId> riders;
    /** See {@link #riders}' security note */
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId organizer;

    /** Default constructor for MongoDB */
    public Ride() {
        super();
    }

    public Ride(User organizer, Location to, Location from) {
        this.to = to.getPoint();
        this.from = from.getPoint();
        this.riders = new ArrayList<>();
        this.organizer = organizer.getId();
        this.departTime = System.currentTimeMillis() / 1000L;
    }

    /**
     * Fetch a list of all riders.
     *
     * @return all riders' ids, including the organizer
     */
    public List<ObjectId> getRiders() {
        List<ObjectId> ridersCopy = new ArrayList<>(riders);
        if (organizer != null)
            ridersCopy.add(organizer);
        return ridersCopy;
    }

    /**
     * Add a user to the ride.
     *
     * @param rider rider to add; rider cannot be in ride, and ride must have space
     * @return all riders' ids, or null if failed
     */
    public List<ObjectId> addRider(User rider) {
        if (riders.contains(rider.getId())) {
            logger.warn("{} is already in {}.", rider, this);
            return null;
        } else if (getRiders().size() >= 6) {
            logger.warn("{} is at XL capacity and cannot take {}.", this, rider);
            return null;
        }

        riders.add(rider.getId());
        return getRiders();
    }

    /**
     * Remove a user from the ride.
     *
     * @param rider rider to remove; rider must exist in ride, and rider must not be
     * @return all remaining riders' ids, or null if failed.
     */
    public List<ObjectId> removeRider(User rider) {
        if (rider.getId().equals(organizer)) {
            logger.warn("{} is {}'s organizer.", rider, this);
            return null;
        } else if (!riders.remove(rider.getId())) {
            logger.warn("{} is not in {}.", rider, this);
            return null;
        }

        return getRiders();
    }

    /**
     * Transfer organizer status.
     *
     * @param rider rider to make organizer
     * @return new organizer's id, or null if failed.
     */
    public ObjectId transferOrganizer(User rider) {
        if (rider.getId().equals(organizer)) {
            logger.warn("{} is already {}'s organizer.", rider, this);
            return null;
        } else if (!riders.remove(rider.getId())) {
            logger.warn("{} is not in {}.", rider, this);
            return null;
        }

        riders.add(organizer);
        organizer = rider.getId();
        return organizer;
    }

    /**
     * Get the `to` location.
     *
     * @return the `to` location
     */
    public Location getTo() {
        return Location.fromPoint(to);
    }

    /**
     * Get the `from` location.
     *
     * @return the `from` location
     */
    public Location getFrom() {
        return Location.fromPoint(from);
    }

    @Override
    public String toString() {
        return "Ride-" + id.toHexString();
    }
}
