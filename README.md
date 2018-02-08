# WearFingerprintServer
Server between Android application and fingerprint server beacon.uhk.cz. Beacon server has couchebase database with synch gateway but it was deemend not usable for Android application dues to complexity of documents.

This server acts as REST API middleman between Android application and Couchebase database. It was created to work with Android fingerprint application: https://github.com/Del-S/WearFingerprint. It works on Tomcat server and it was not tested for GlassFish.

Main routes are:
- /fingerprints - handles GET and POST of fingerprint documents
- /fingerprints-meta handles GET last insert time and count of new fingerprints for specific device
API documentation can be found here: https://app.swaggerhub.com/apis/Del-S/FingerprintAPI/1.0.0

Note: Device can be blacklisted by adding IMEI device id to file `blacklist.txt` in `source/main/resources`.


