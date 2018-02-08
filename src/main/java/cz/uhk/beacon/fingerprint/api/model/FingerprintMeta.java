package cz.uhk.beacon.fingerprint.api.model;

import java.util.Objects;

public class FingerprintMeta {

    // Variables of this class
    private int countNew;           // Count of new fingerprints based on last update timestamp
    private long lastInsert;        // Timestamp of last update based on deviceId

    // Default constructor used for Gson
    public FingerprintMeta() {}

    /**
     * @return the countNew
     */
    public int getCountNew() {
        return countNew;
    }

    /**
     * @param countNew the countNew to set
     */
    public void setCountNew(int countNew) {
        this.countNew = countNew;
    }

    /**
     * @return the lastInsert
     */
    public long getLastInsert() {
        return lastInsert;
    }

    /**
     * @param lastInsert the lastInsert to set
     */
    public void setLastInsert(long lastInsert) {
        this.lastInsert = lastInsert;
    }
    
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FingerprintMeta locationEntry = (FingerprintMeta) o;
        return Objects.equals(this.countNew, locationEntry.countNew) &&
               Objects.equals(this.lastInsert, locationEntry.lastInsert);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countNew, lastInsert);
    }


    @Override
    public String toString() {
        return "class FingerprintMeta {\n" +
                "    countNew: " + toIndentedString(countNew) + "\n" +
                "    lastInsert: " + toIndentedString(lastInsert) + "\n" +
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
