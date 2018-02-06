package cz.uhk.beacon.fingerprint.api.controller;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.Expression;
import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cz.uhk.beacon.fingerprint.api.model.Fingerprint;
import cz.uhk.beacon.fingerprint.api.model.FingerprintMeta;
import cz.uhk.beacon.fingerprint.api.model.LocationEntry;

@Controller
@RestController
public class FingerprintController {
    
    private final static String GATEWAY_URL = "http://localhost:4985/fingerprintgw";
    private final static String UNAUTHORIZED = "Unauthorized";
    private final static String BAD_REQUEST = "Data is malformed, please correct the errors and try again.";
    private static final Logger LOGGER = Logger.getLogger("FingerprintController");
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Route /fingerprints (GET) that loads synchonized fingerprints from specific Couchebase bucket.
     * - Can be parametrized by timestamp and location.
     * 
     * @param request to handle and get data from
     * @return ResponseEntity with Fingerprint JSON data
     */
    @RequestMapping(value = "/fingerprints", method = RequestMethod.GET, produces="application/json")
    public ResponseEntity getFingerprints(HttpServletRequest request) {
        
        // UNAUTHORIZED exception when there is no deviceId in the header
        String deviceId = request.getHeader("deviceId");
        if(StringUtils.isEmpty(deviceId)) {
            return new ResponseEntity<>(UNAUTHORIZED, null, HttpStatus.UNAUTHORIZED);
        }
        
        // Query expression filtering deleted and not synchonized documents
        Expression whereEx = x("_deleted").ne(x("true"))
            .or( x("_deleted").isMissing() )
            .and( x("_sync").isNotMissing() );
        
        // Get query timestamp parameter and modify Query expression
        String timestamp = request.getParameter("timestamp");
        if(timestamp != null && isLong(timestamp)) {     
            whereEx = whereEx.and("timestamp").gt(x(timestamp));
        }
        
        // Parse the request body into an object
        LocationEntry locationEntry;
        try {
            String body = request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual); 
            // Check if body is not empty
            if(!StringUtils.isEmpty(body)) {
                // Map string to LocationEntry
                locationEntry = objectMapper.readValue(body, LocationEntry.class);
                
                // Get level from LocationEntry and modify Query expression
                String level = locationEntry.getLevel();
                if(!StringUtils.isEmpty(level)) {
                    whereEx = whereEx.and("level").eq(s(level));
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Cannot convert request body to LocationEntry", ex);
        }
        
        // Initiate result and JSON variables
        List<JSONObject> result = new ArrayList();
        JSONParser parser = new JSONParser();
        
        // Connect to the Couchebase cluster and open connection to the bucket
        Cluster cluster = CouchbaseCluster.create();
        cluster.authenticate("admin", "admin123");
        Bucket bucket = cluster.openBucket("fingerprint");
        
        // Create a N1QL Select statement to get fingerprints
        Statement statement = select("fingerprint.*, META(fingerprint).id").from(i("fingerprint"))
            .where( whereEx );
        
        // Run query on the specific bucket
        N1qlQueryResult queryResult = bucket.query(N1qlQuery.simple(statement));

        try {
            // Parse N1QL rows into JSON objects
            for (N1qlQueryRow row : queryResult.allRows()) {
                String json = new String(row.byteValue());                 
                result.add((JSONObject) parser.parse(json));
            }
        } catch(ParseException e) {
            LOGGER.log(Level.SEVERE, "Cannot convert N1ql row into JSONObject", e);
        }
            
        // Disconnect from the bucket and cluster
        bucket.close();
        cluster.disconnect();
     
        // Return calculated data
        return new ResponseEntity<>(result, null, HttpStatus.OK);
    }
    
    /**
     * Route /fingerprints (POST) saves fingerprints into couchebase databse using
     * sync gateway to enable synchronization.
     * 
     * @param request to handle and get data from
     * @return ResponseEntity with only HTTP code and nothing else
     */
    @RequestMapping(value = "/fingerprints", method = RequestMethod.POST, produces="application/json")
    public ResponseEntity addFingerprint(HttpServletRequest request) {
        
        // UNAUTHORIZED exception when there is no deviceId in the header
        String deviceId = request.getHeader("deviceId");
        if(StringUtils.isEmpty(deviceId)) {
            return new ResponseEntity<>(UNAUTHORIZED, null, HttpStatus.UNAUTHORIZED);
        }
        
        // Parse the request body into an object
        Fingerprint fingerprint = null;
        List<Fingerprint> fingerprints = new ArrayList();
        try {
            // Get body string from request
            String body = request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual); 

            // Check if body is not empty
            if(!StringUtils.isEmpty(body)) {
                
                // Map json data to proper objects (list or single fingerprint)
                if(body.startsWith("[")) {
                    fingerprints = objectMapper.readValue(body, new TypeReference<List<Fingerprint>>(){});
                } else {
                    fingerprint = objectMapper.readValue(body, Fingerprint.class);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not convert request body to Fingerprints", ex);
        }
        
        // Check if Json data was mapped to proper objects else return error
        if( (fingerprint == null && fingerprints.isEmpty()) ||
                (fingerprint != null && !fingerprint.isValid())) {
            return new ResponseEntity<>(BAD_REQUEST, null, HttpStatus.BAD_REQUEST);
        }
        
        // If only single fingerprint was mapped then save it into the database
        if(fingerprint != null && fingerprint.isValid()) {
            try {
                saveSingleFingerprint(fingerprint);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not send data to the Sync Gateway", ex);
                return new ResponseEntity<>(BAD_REQUEST, null, HttpStatus.BAD_REQUEST);
            }
        }
        
        // Save multiple fingerprint into the database
        if(!fingerprints.isEmpty()) {
            saveMultipleFingerprints(fingerprints);
        }
        
        // Result will not inform about errors
        return new ResponseEntity<>(null, null, HttpStatus.OK);
    }
    
    /**
     * Route /fingerprints-meta (GET) that gets meta information for specific device.
     * - Data loaded is count of new fngerprints and last added fingerprint.
     * 
     * @param request to handle and get data from
     * @return ResponseEntity with FingerprintMeta JSON data
     */
    @RequestMapping(value = "/fingerprints-meta", method = RequestMethod.GET, produces="application/json")
    public ResponseEntity getFingerprintMeta(HttpServletRequest request) {
        
        // UNAUTHORIZED exception when there is no deviceId in the header
        String deviceId = request.getHeader("deviceId");
        if(StringUtils.isEmpty(deviceId)) {
            return new ResponseEntity<>(UNAUTHORIZED, null, HttpStatus.UNAUTHORIZED);
        }
       
        // Get query timestamp parameter and check if it's in a correct format
        String timestamp = request.getParameter("timestamp");
        if(timestamp == null || !isLong(timestamp)) {     
            return new ResponseEntity<>(BAD_REQUEST, null, HttpStatus.BAD_REQUEST);
        }
        
        // Connect to the Couchebase cluster and open connection to the bucket
        Cluster cluster = CouchbaseCluster.create();
        cluster.authenticate("admin", "admin123");
        Bucket bucket = cluster.openBucket("fingerprint");
        
        // Create a N1QL Select for count of new Fingerprints
        Statement statementCountNew = select("COUNT(*) as countNew").from(i("fingerprint"))
            .where( x("_deleted").ne(x("true"))
                .or( x("_deleted").isMissing() )
                .and( x("_sync").isNotMissing() )
                .and("timestamp").gt(x(timestamp))
            );
        
        // Create a N1QL Select for timestamp of last added fingerprint by specific device
        Statement statementLastInsert = select("MAX(timestamp) as lastInsert").from(i("fingerprint"))
            .where( x("_deleted").ne(x("true"))
                .or( x("_deleted").isMissing() )
                .and( x("_sync").isNotMissing() )
                .and("deviceRecord.telephone").eq(s(deviceId))
            );
        
        // Union query so there is no need to run two of them
        String queryString = "(" +statementCountNew + ") UNION (" + statementLastInsert + ")";
        // Run query to get the data
        N1qlQueryResult query = bucket.query(N1qlQuery.simple(queryString));

        // Result data to display
        FingerprintMeta result = new FingerprintMeta();
        try {
            // Parse N1QL rows into JsonObjects and put data into result
            query.allRows().forEach((row) -> {
                JsonObject data = row.value();  // Parse data into JsonObject
                
                // Get count of new fingerprints
                if(data.containsKey("countNew")) {
                    result.setCountNew( (int) row.value().get("countNew") );
                }
                
                // Get timestamp of last insert by specific device
                if (data.containsKey("lastInsert")) {
                    result.setLastInsert( (long) row.value().get("lastInsert") );
                }
            });
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot convert N1ql row into FingerprintMeta", e);
        }
        
        // Disconnect from the bucket and cluster
        bucket.close();
        cluster.disconnect();
        
        // Return data
        return new ResponseEntity<>(result, null, HttpStatus.OK);
    }
    
    /**
     * Checks if String can be converted to Long.
     * Used with checking timestamps.
     * 
     * @param string to convert
     * @return boolean
     */
    private boolean isLong(String string) {
        try{ 
            Long.parseLong( string ); 
            return true;
        }
        catch(NumberFormatException e){
            LOGGER.log(Level.WARNING, "Cannot convert " + string + " to long.", e);
            return false;
        }
    }
    
    /**
     * Saves single fingerprint into Couchebase using Sync Gateway.
     * 
     * @param fingerprint to save into the database
     * @throws IOException 
     */
    private void saveSingleFingerprint(Fingerprint fingerprint) throws IOException {
        // Map fingerprint as json in byte format
        byte[] data = objectMapper.writeValueAsBytes(fingerprint);    
        
        // Calculate call url
        String url = GATEWAY_URL + "/" + fingerprint.getId();
        URL requestUrl = new URL(url);
        
        // Create HTTP connection
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");

        // Write fingerprint data
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(data);
            os.flush();
        }

        // Check result
        int responseCode = connection.getResponseCode();
        if (responseCode < HttpURLConnection.HTTP_OK || responseCode > HttpURLConnection.HTTP_PARTIAL) {
            throw new IOException("Response (" + responseCode + "): " + connection.getResponseMessage());
        }
    }
    
    /**
     * Saves multiple fingerprints into Couchebase using Sync Gateway.
     * 
     * @param fingerprints to save into the database
     */
    private void saveMultipleFingerprints(List<Fingerprint> fingerprints) {
        fingerprints.forEach((fingerprint) -> {
            try {
                saveSingleFingerprint(fingerprint);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not save fingerprint (id: " + fingerprint.getId() + ")", e);
            }
        });
    }
}
