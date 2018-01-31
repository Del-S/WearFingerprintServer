package cz.uhk.beacon.fingerprint.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
    public ResponseEntity getIndex(ModelMap model) {
        return new ResponseEntity<>("Fingerprint to Android server.", null, HttpStatus.OK);
    }
    
}
