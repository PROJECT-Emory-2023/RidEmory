package com.projectpandas.ridemory.info;

import com.projectpandas.ridemory.config.APIKeys;
import com.projectpandas.ridemory.ride.Location;
import com.projectpandas.ridemory.ride.Ride;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InfoService {
    // TODO:
    // Traffic times
    // Uber/Lyft price estimates
    // Transloc
    // DONE:
    // ATL TSA wait times
    public final HttpClient client;

    @Autowired
    private MongoTemplate mongoTemplate;

    public static final String TSAWaitTimeAPI = "https://www.atl.com/times/";
    public static final String[] checkpoints = {"MAIN", "NORTH", "LOWER NORTH", "SOUTH", "INT'L"};
    private static final String GoogleMapAPIKEY = APIKeys.googleAPIKey;

    // @Autowired
    public InfoService() {
        client = HttpClient.newHttpClient();

        // example request code using Http
        // HttpRequest request = HttpRequest.newBuilder()
        // .uri(URI.create(TSAWaitTimeAPI))
        // .GET()
        // .build();

        // CompletableFuture<String> response = client.sendAsync(request,
        // BodyHandlers.ofString())
        // .thenApply(HttpResponse::body);
    }

    public Map<String, Integer> getATLWaitTime() {
        // hard coded, since scraping directly from www.atl.com is specific to
        // ATL
        // airport
        // will need to use some other api for general wait times

        try {
            Map<String, Integer> times = new HashMap<>();
            Document doc = Jsoup.connect(TSAWaitTimeAPI).get();
            Elements els = doc.select("div.row");

            for (int i = 4; i < 9; i++) {
                String checkpoint = checkpoints[i - 4];
                Integer wait = Integer.parseInt(els.get(i).select("span").html());
                times.put(checkpoint, wait);
            }

            return times;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getTrafficTimeEstimate(Ride ride) {
        Location origin = ride.getTo();
        Location destination = ride.getFrom();
        // GeoJsonPoint origin=ride.getOrigin();
        // GeoJsonPoint destination=ride.getDestination();
        try {
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?" + "origins=" + origin.getLat()
                    + "," + origin.getLon() + "&destinations=" + destination.getLat() + "," + destination.getLon()
                    + "&departure_time=now" + "&traffic_model=best_guess" + "&key=" + GoogleMapAPIKEY;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.out.println("Error retrieving traffic time. Status code: " + response.statusCode());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<SortRidesByLocation> getTrendingLocation() {
        // defining pipelines
        Aggregation agg = Aggregation.newAggregation(Aggregation.sortByCount("to"),
                Aggregation.project("count").and("_id").as("Destination"), Aggregation.limit(3));

        AggregationResults<SortRidesByLocation> results = mongoTemplate.aggregate(agg, "rides",
                SortRidesByLocation.class);
        return results.getMappedResults();
    }
}
