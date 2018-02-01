package cz.uhk.beacon.fingerprint.api.controller;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import cz.uhk.beacon.fingerprint.api.model.Fingerprint;
import cz.uhk.beacon.fingerprint.api.model.LocationEntry;
import java.io.File;
import java.io.FileReader;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller
 */
@Controller
@RestController
@RequestMapping("/")
public class IndexController {
    
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getIndex() {
        return new ResponseEntity<>("Fingerprint to Android server.", null, HttpStatus.OK);
    }
    
    /*@RequestMapping(path = "/couch_auto", method = RequestMethod.GET)
    public ResponseEntity createCouchAuto() {
        Cluster cluster = CouchbaseCluster.create();
        cluster.authenticate("Del_S", "deldas38");
        Bucket bucket = cluster.openBucket("beacon");
        
        try {
            JSONParser jsonParser = new JSONParser();
            JSONArray a = (JSONArray) jsonParser.parse(new FileReader("/home/del_s/Plocha/beacon.json"));
            for (Object o : a) {

                JSONObject beacon = (JSONObject) o;
                JsonObject beaconJson = JsonObject.fromJson(beacon.toJSONString());
                
                bucket.upsert(JsonDocument.create((String) beacon.get("id"), beaconJson));
                // parse your objects by means of parser.getXxxValue() and/or other parser's methods

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return new ResponseEntity<>("Fingerprint to Android server.", null, HttpStatus.OK);
    }*/
}
