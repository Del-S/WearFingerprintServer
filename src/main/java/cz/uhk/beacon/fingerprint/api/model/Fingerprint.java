package cz.uhk.beacon.fingerprint.api.model;

import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

public class Fingerprint {

    // Variables of this class
    @JsonProperty("_id")
    private UUID id;                                // UUID of this scan
    private UUID scanID;                            // UUID to enable fingerprint grouping
    private int x,y;                                // Calculated X and Y locations
    private long scanLength;                        // Length of the scan in ms
    @JsonProperty("timestamp")
    private long scanStart;                         // Timestamps of scan start
    @JsonProperty("finish")
    private long  scanEnd;                          // Timestamps of scan end
    /**
     * @Deprecated
     * Use LocationEntry instead
     */
    private String level;
    private LocationEntry locationEntry;            // Location of fingerprint to enable multiple buildings and floors
    private String deviceEntry;                // Device that created this fingerprint
    private String beaconEntries;        // List of beacon entries scanned for this fingerprint
    private String wirelessEntries;    // List of wireless entries scanned for this fingerprint
    private String cellularEntries;    // List of cellular entries scanned for this fingerprint
    private String sensorEntries;        // List of beacon entries scanned for this fingerprint

    public Fingerprint() {
        // Set id and scan UUID to send into other device
        id = UUID.randomUUID();
        scanID = UUID.randomUUID();

        // Initiate lists
        beaconEntries = "{entries:500}";
        wirelessEntries = "{entries:300}";
        cellularEntries = "{entries:200}";
        sensorEntries = "{entries:300}";

        // Set device
        deviceEntry = "{\"entries\":\"1\"}";

        // Default scan length is 60s
        scanLength = 60000;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getScanID() {
        return scanID;
    }

    public void setScanID(UUID scanID) {
        this.scanID = scanID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getScanLength() {
        return scanLength;
    }

    public void setScanLength(long scanLength) {
        this.scanLength = scanLength;
    }

    public long getScanStart() {
        return scanStart;
    }

    public void setScanStart(long scanStart) {
        this.scanStart = scanStart;
    }

    public long getScanEnd() {
        return scanEnd;
    }

    public void setScanEnd(long scanEnd) {
        this.scanEnd = scanEnd;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        if(level != null) {
            this.locationEntry = new LocationEntry(level);
        }
        this.level = level;
    }

    public LocationEntry getLocationEntry() {
        return locationEntry;
    }

    public void setLocationEntry(LocationEntry locationEntry) {
        this.locationEntry = locationEntry;
    }

    public String getDeviceEntry() {
        return deviceEntry;
    }

    public void setDeviceEntry(String deviceEntry) {
        this.deviceEntry = deviceEntry;
    }

    public String getBeaconEntries() {
        return beaconEntries;
    }

    public void setBeaconEntries(String beaconEntries) {
        this.beaconEntries = beaconEntries;
    }

    public String getWirelessEntries() {
        return wirelessEntries;
    }

    public void setWirelessEntries(String wirelessEntries) {
        this.wirelessEntries = wirelessEntries;
    }

    public String getCellularEntries() {
        return cellularEntries;
    }

    public void setCellularEntries(String cellularEntries) {
        this.cellularEntries = cellularEntries;
    }

    public String getSensorEntries() {
        return sensorEntries;
    }

    public void setSensorEntries(String sensorEntries) {
        this.sensorEntries = sensorEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Fingerprint fingerprint = (Fingerprint) o;
        return Objects.equals(this.id, fingerprint.id) &&
                Objects.equals(this.scanID, fingerprint.scanID) &&
                Objects.equals(this.x, fingerprint.x) &&
                Objects.equals(this.y, fingerprint.y) &&
                Objects.equals(this.scanLength, fingerprint.scanLength) &&
                Objects.equals(this.scanStart, fingerprint.scanStart) &&
                Objects.equals(this.scanEnd, fingerprint.scanEnd) &&
                Objects.equals(this.locationEntry, fingerprint.locationEntry) &&
                Objects.equals(this.deviceEntry, fingerprint.deviceEntry) &&
                Objects.equals(this.beaconEntries, fingerprint.beaconEntries) &&
                Objects.equals(this.wirelessEntries, fingerprint.wirelessEntries) &&
                Objects.equals(this.cellularEntries, fingerprint.cellularEntries) &&
                Objects.equals(this.sensorEntries, fingerprint.sensorEntries);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, scanID, x, y, scanStart, scanEnd, locationEntry, deviceEntry, beaconEntries, wirelessEntries, cellularEntries, sensorEntries);
    }


    @Override
    public String toString() {
        return "class Fingerprint {\n" +
                "    id: " + toIndentedString(id) + "\n" +
                "    scanID: " + toIndentedString(scanID) + "\n" +
                "    x: " + toIndentedString(x) + "\n" +
                "    y: " + toIndentedString(y) + "\n" +
                "    scanLength: " + toIndentedString(scanLength) + "\n" +
                "    scanStart: " + toIndentedString(scanStart) + "\n" +
                "    scanEnd: " + toIndentedString(scanEnd) + "\n" +
                "    locationEntry: " + toIndentedString(locationEntry) + "\n" +
                "    deviceEntry: " + toIndentedString(deviceEntry) + "\n" +
                "    beaconEntriesCount: " + toIndentedString(beaconEntries) + "\n" +
                "    wirelessEntriesCount: " + toIndentedString(wirelessEntries) + "\n" +
                "    cellularEntriesCount: " + toIndentedString(cellularEntries) + "\n" +
                "    sensorEntriesCount: " + toIndentedString(sensorEntries) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
