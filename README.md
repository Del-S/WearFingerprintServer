# WearFingerprintServer
Server between Android application and fingerprint server beacon.uhk.cz. Beacon server has Couchbase database with synch gateway which was deemend not usable for Android application due to complexity of documents. Single document can have more than 2000 sub-documents.

This server acts as REST API middleman between Android application and Couchebase database. It was created to work with Android fingerprint application: https://github.com/Del-S/WearFingerprint. It works on Tomcat server and it was not tested for GlassFish.

Main routes are:
- /fingerprints - handles GET and POST of fingerprint documents.
- /fingerprints - meta handles GET last insert time and count of new fingerprints for specific device.

Note: Device can be blacklisted from accessing this API by adding it's IMEI device id to file `blacklist.txt` in `source/main/resources`.

API documentation can be found here: https://app.swaggerhub.com/apis/Del-S/FingerprintAPI/1.0.1


