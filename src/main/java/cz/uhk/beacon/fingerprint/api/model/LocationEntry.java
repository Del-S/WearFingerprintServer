package cz.uhk.beacon.fingerprint.api.model;

import java.util.Objects;

/**
 * This class keeps information about location of the fingerprint.
 * To enable multiple buildings and floors.
 */
public class LocationEntry {

    // Variables of this class
    private String building;        // Name of the building
    private int floor;              // Floor number inside the building
    private String level;           // Level identifier to distinguish floors

    // Default constructor used for Gson
    public LocationEntry() {}

    // Default constructor used for Gson
    public LocationEntry(String location) {
        level = location;
        switch(location) {
            case "J1NP":
                this.building = "UHK";
                this.floor = 1;
                break;
            case "J2NP":
                this.building = "UHK";
                this.floor = 2;
                break;
            case "J3NP":
                this.building = "UHK";
                this.floor = 3;
                break;
            case "J4NP":
                this.building = "UHK";
                this.floor = 4;
                break;
        }
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocationEntry locationEntry = (LocationEntry) o;
        return Objects.equals(this.building, locationEntry.building) &&
               Objects.equals(this.floor, locationEntry.floor) &&
               Objects.equals(this.level, locationEntry.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, floor, level);
    }


    @Override
    public String toString() {
        return "class LocationEntry {\n" +
                "    building: " + toIndentedString(building) + "\n" +
                "    floor: " + toIndentedString(floor) + "\n" +
                "    level: " + toIndentedString(level) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
