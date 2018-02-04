package cz.uhk.beacon.fingerprint.api.controller;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.Expression;
import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import cz.uhk.beacon.fingerprint.api.model.LocationEntry;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;

@Controller
@RestController
@RequestMapping("/fingerprints")
public class FingerprintController {
    
    private final static String UNAUTHORIZED = "Unauthorized";
    private static final Logger LOGGER = Logger.getLogger("FingerprintController");
    
    /**
     * Route /fingerprints that loads synchonized fingerprints from specific Couchebase bucket.
     * - Can be parametrized by timestamp and location.
     * 
     * @param request to handle and get data from
     * @return ResponseEntity with JSON data
     */
    @RequestMapping(method = RequestMethod.GET, produces="application/json")
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
                ObjectMapper objectMapper = new ObjectMapper();
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
        //Statement statement = select("fingerprint.*, META(fingerprint).id").from(i("fingerprint"))
            //.where( whereEx );
        
        // Create a N1QL Select statement to get fingerprints
        Statement statement = select("COUNT(*)").from(i("fingerprint"))
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
    
}
