package com.nirixx.app.vci;

/** A supported VCI hardware model + the static catalog of everything NirixX
 *  can talk to (NirixX hardware plus compatible third-party Android adapters). */
public class VciModel {

    public final String name;
    public final String linkType;   // "BT Classic" | "BLE" | "Wi-Fi" | "USB OTG"
    public final String drawable;   // drawable resource name for the product render
    public final String blurb;
    public final String firmware;
    public final boolean thirdParty;

    public VciModel(String name, String linkType, String drawable, String blurb, String firmware, boolean thirdParty) {
        this.name = name;
        this.linkType = linkType;
        this.drawable = drawable;
        this.blurb = blurb;
        this.firmware = firmware;
        this.thirdParty = thirdParty;
    }

    public static VciModel[] all() {
        return new VciModel[]{
            // NirixX hardware family
            new VciModel("NRX Pro VCI", "BT Classic + BLE + Wi-Fi", "vci_nrx_pro",
                "Flagship handheld interface with status display. Full UDS, K-line and CAN-FD.", "v4.2.1", false),
            new VciModel("TZ VCI (Classic)", "BT Classic (SPP)", "vci_tz_classic",
                "Original TZ VCI interface box. UDS + K-line over RFCOMM.", "v2.9.4", false),
            new VciModel("TZ Mini VCI", "BT Classic (SPP)", "vci_tz_mini",
                "Pocket-size classic-Bluetooth dongle for quick service lane work.", "v2.7.0", false),
            new VciModel("TZ New VCI", "BLE 5.0", "vci_tz_new",
                "New-generation BLE dongle with USB-C wired fallback.", "v3.3.2", false),
            new VciModel("TZ 24V HD VCI", "BT Classic (SPP)", "vci_tz_24v",
                "Armored 24-volt unit for commercial and genset work.", "v1.8.6", false),
            new VciModel("NirixX Link Pod", "BLE 5.2", "vci_ble_pod",
                "Minimal BLE pod — always-on pairing, battery monitoring built in.", "v1.2.0", false),
            // Compatible third-party adapters (Android-supported)
            new VciModel("ELM327-compatible (SPP)", "BT Classic (SPP)", "vci_elm327",
                "Generic ELM327-style Bluetooth adapter — read/clear DTCs and live data.", "AT v1.5", true),
            new VciModel("ELM327-compatible (BLE)", "BLE 4.0+", "vci_elm327",
                "Generic BLE OBD adapter — read/clear DTCs and live data.", "AT v2.1", true),
            new VciModel("J2534 Pass-Thru", "Wi-Fi", "vci_j2534",
                "Workshop pass-thru interface for module programming sessions.", "v1.20.04", true),
            new VciModel("USB K-Line (OTG)", "USB OTG", "vci_usbk",
                "Wired K-line adapter through USB OTG — legacy models only.", "n/a", true),
        };
    }

    /** Models surfaced by the pairing scanner's simulated radio. */
    public static String[][] discoveredPool() {
        return new String[][]{
            {"NRX-VCI-24F1", "TZ VCI (Classic)", "BT Classic", "vci_tz_classic"},
            {"TZ-MINI-VCI-8DA2", "TZ Mini VCI", "BT Classic", "vci_tz_mini"},
            {"TZ-NEW-VCI-31C7", "TZ New VCI", "BLE", "vci_tz_new"},
            {"NRXPRO-VCI-1193", "NRX Pro VCI", "BT Classic + BLE", "vci_nrx_pro"},
            {"NRX-POD-7B2E", "NirixX Link Pod", "BLE", "vci_ble_pod"},
            {"OBDII-ADAPTER", "ELM327-compatible (SPP)", "BT Classic", "vci_elm327"},
        };
    }
}
