/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.uhk.beacon.fingerprint.api.controller;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
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
import static com.couchbase.client.java.query.Select.select;
import com.couchbase.client.java.query.Statement;
import static com.couchbase.client.java.query.dsl.Expression.*;

@Controller
@RestController
@RequestMapping("/fingerprints")
public class FingerprintController {
    
    private final static String UNAUTHORIZED = "Unauthorized";
    
    @RequestMapping(method = RequestMethod.GET, produces="application/json")
    public ResponseEntity getFingerprints(HttpServletRequest request) {
        
        // UNAUTHORIZED exception when there is no app-id in the header
        String deviceId = request.getHeader("deviceId");
        if(StringUtils.isEmpty(deviceId)) {
            //return new ResponseEntity<>(UNAUTHORIZED, null, HttpStatus.UNAUTHORIZED);
        }
        
        Cluster cluster = CouchbaseCluster.create();
        cluster.authenticate("admin", "admin123");
        Bucket bucket = cluster.openBucket("fingerprint");
        
        List<JSONObject> result = new ArrayList();
        JSONParser parser = new JSONParser();
        
        String timestamp = request.getParameter("timestamp");
        if(timestamp == null) {     
            Statement statement = select("*").from(i("fingerprint"))
                        .where( x("_deleted").ne(x("true"))
                            .or( x("_deleted").isMissing() )
                            .and( x("_sync").isNotMissing() )
                        );

            N1qlQueryResult queryResult = bucket.query(N1qlQuery.simple(statement));
            
            try {
            
                for (N1qlQueryRow row : queryResult.allRows()) {
                    //we go straight for the raw bytes and turn into string
                    String rowData = new String(row.byteValue());
                    result.add((JSONObject)parser.parse(rowData));
                }
            
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
     
        return new ResponseEntity<>(result, null, HttpStatus.OK);
    }
    
}
