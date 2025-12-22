package xyz.overdyn.dyngui.tools;

/**
 * Enum representing Minecraft server versions with their protocol numbers and NMS versions.
 */
public enum ServerVersion {
    V_1_16_4(754, "v1_16_R3"), V_1_16_5(754, "v1_16_R3"),
    V_1_17(755, "v1_17_R1"), V_1_17_1(756, "v1_17_R1"),
    V_1_18(757, "v1_18_R1"), V_1_18_1(757, "v1_18_R1"), V_1_18_2(758, "v1_18_R2"),
    V_1_19(759, "v1_19_R1"), V_1_19_1(760, "v1_19_R2"), V_1_19_2(760, "v1_19_R2"), 
    V_1_19_3(761, "v1_19_R3"), V_1_19_4(762, "v1_19_R3"),
    V_1_20(763, "v1_20_R1"), V_1_20_1(763, "v1_20_R1"), V_1_20_2(764, "v1_20_R2"), 
    V_1_20_3(765, "v1_20_R3"), V_1_20_4(765, "v1_20_R3"), V_1_20_5(766, "v1_20_R4"), V_1_20_6(766, "v1_20_R4"),
    V_1_21(767, "v1_21_R1"), V_1_21_1(767, "v1_21_R1"), V_1_21_2(768, "v1_21_R2"), V_1_21_3(768, "v1_21_R2"), 
    V_1_21_4(769, "v1_21_R3"), V_1_21_5(770, "v1_21_R3"), V_1_21_6(771, "v1_21_R3"), V_1_21_7(772, "v1_21_R3"), 
    V_1_21_8(772, "v1_21_R3"), V_1_21_9(773, "v1_21_R3"), V_1_21_10(773, "v1_21_R3"), V_1_21_11(774, "v1_21_R3"),
    ERROR(-1, "unknown", true);

    private final int protocolVersion;
    private final String nmsVersion;
    private final boolean isError;

    ServerVersion(int protocolVersion, String nmsVersion) {
        this(protocolVersion, nmsVersion, false);
    }

    ServerVersion(int protocolVersion, String nmsVersion, boolean isError) {
        this.protocolVersion = protocolVersion;
        this.nmsVersion = nmsVersion;
        this.isError = isError;
    }

    /**
     * Gets the protocol version number.
     *
     * @return the protocol version
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Gets the NMS version string.
     *
     * @return the NMS version
     */
    public String getNmsVersion() {
        return nmsVersion;
    }

    /**
     * Checks if this is an error state.
     *
     * @return true if error, false otherwise
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Checks if this version is supported for inventory title updates.
     *
     * @return true if supported, false otherwise
     */
    public boolean isSupported() {
        return !isError && protocolVersion >= 754;
    }

    /**
     * Gets ServerVersion by NMS version string.
     *
     * @param nmsVersion the NMS version string
     * @return the matching ServerVersion or ERROR if not found
     */
    public static ServerVersion fromNmsVersion(String nmsVersion) {
        for (ServerVersion version : values()) {
            if (version.getNmsVersion().equals(nmsVersion)) {
                return version;
            }
        }
        return ERROR;
    }

    /**
     * Gets ServerVersion by Bukkit version string.
     *
     * @param bukkitVersion the Bukkit version string
     * @return the matching ServerVersion or ERROR if not found
     */
    public static ServerVersion fromBukkitVersion(String bukkitVersion) {
        if (bukkitVersion.contains("1.16.4")) return V_1_16_4;
        if (bukkitVersion.contains("1.16.5")) return V_1_16_5;
        if (bukkitVersion.contains("1.17.1")) return V_1_17_1;
        if (bukkitVersion.contains("1.18.1")) return V_1_18_1;
        if (bukkitVersion.contains("1.18.2")) return V_1_18_2;
        if (bukkitVersion.contains("1.19.0")) return V_1_19;
        if (bukkitVersion.contains("1.19.2")) return V_1_19_2;
        if (bukkitVersion.contains("1.19.3")) return V_1_19_3;
        if (bukkitVersion.contains("1.19.4")) return V_1_19_4;
        if (bukkitVersion.contains("1.20.0")) return V_1_20;
        if (bukkitVersion.contains("1.20.1")) return V_1_20_1;
        if (bukkitVersion.contains("1.20.2")) return V_1_20_2;
        if (bukkitVersion.contains("1.20.3")) return V_1_20_3;
        if (bukkitVersion.contains("1.20.4")) return V_1_20_4;
        if (bukkitVersion.contains("1.20.5")) return V_1_20_5;
        if (bukkitVersion.contains("1.20.6")) return V_1_20_6;
        if (bukkitVersion.contains("1.21.0")) return V_1_21;
        if (bukkitVersion.contains("1.21.1")) return V_1_21_1;
        if (bukkitVersion.contains("1.21.2")) return V_1_21_2;
        if (bukkitVersion.contains("1.21.3")) return V_1_21_3;
        if (bukkitVersion.contains("1.21.4")) return V_1_21_4;
        if (bukkitVersion.contains("1.21.5")) return V_1_21_5;
        if (bukkitVersion.contains("1.21.6")) return V_1_21_6;
        if (bukkitVersion.contains("1.21.7")) return V_1_21_7;
        if (bukkitVersion.contains("1.21.8")) return V_1_21_8;
        if (bukkitVersion.contains("1.21.9")) return V_1_21_9;
        if (bukkitVersion.contains("1.21.10")) return V_1_21_10;
        if (bukkitVersion.contains("1.21.11")) return V_1_21_11;
        
        if (bukkitVersion.contains("1.16")) return V_1_16_5;
        if (bukkitVersion.contains("1.17")) return V_1_17_1;
        if (bukkitVersion.contains("1.18")) return V_1_18_2;
        if (bukkitVersion.contains("1.19")) return V_1_19_4;
        if (bukkitVersion.contains("1.20")) return V_1_20_6;
        if (bukkitVersion.contains("1.21")) return V_1_21_11;
        
        return ERROR;
    }
}