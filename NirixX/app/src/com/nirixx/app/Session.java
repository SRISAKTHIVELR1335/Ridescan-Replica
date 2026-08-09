package com.nirixx.app;

import java.util.ArrayList;
import java.util.List;

/** Static app state shared across screens (mock DMS/VCI session). */
public final class Session {
    private Session() {}

    public static String dealerEmail = "";
    public static String dealerName = "SR Sakthi Motors — Chennai";

    public static boolean vciConnected = false;
    public static String vciName = "";
    public static String vciFw = "2.14.0";

    public static String selectedVin = "MD634NF4XRCL12345";
    public static String selectedVehicle = "TVS Ronin";
    public static String selectedVariant = "RR310_REFRESH";
    public static String selectedEcu = "EMS — Sedemac (UDS)";
    public static String selectedEcuShort = "EMS";
    public static String selectedFlashFile = "RR310_REFRESH_NEW";

    public static int dtcsCleared = 0;
    public static boolean reportGenerated = false;
    public static String userType = "Service Technician";
    public static String odometer = "—";

    public static final List<String[]> vehicles = new ArrayList<String[]>();
    static {
        vehicles.add(new String[]{"TVS Ronin 225", "RR310_REFRESH · BSVI", "MD634NF4XRCL12345", "Today 09:41"});
        vehicles.add(new String[]{"TVS Apache RTR 160 4V", "RTR160_4V_1CH_EFI_BSVI · BSVI", "MD634KE21XRCA83412", "Yesterday"});
        vehicles.add(new String[]{"TVS Raider 125", "RTR160_2V_1CH_EFI_BABS_BSVI · BSVI", "MD634HG25XRCK66108", "Mon 11:05"});
        vehicles.add(new String[]{"TVS NTORQ 125", "NTQ125_EFI_BSVI · BSVI", "MD634BC18XRCZ90214", "Sat 16:22"});
        vehicles.add(new String[]{"TVS Jupiter 110", "JUP110_ISG_BSVI · BSVI", "MD634JP15XRCT15542", "Fri 10:17"});
    }

    public static final String[][] DTC_DATA = new String[][]{
        {"P0130", "O2 Sensor Circuit Malfunction (Bank 1 Sensor 1)", "Active"},
        {"P0451", "EVAP Pressure Sensor Range / Performance", "Stored"},
        {"P0562", "System Voltage Low — check charging system", "Stored"},
        {"P0850", "Side-stand Switch Input Circuit", "Active"},
        {"U0100", "Lost Communication With ECM/PCM", "History"},
    };
}
