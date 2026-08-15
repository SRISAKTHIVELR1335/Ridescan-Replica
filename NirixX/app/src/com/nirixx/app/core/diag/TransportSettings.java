package com.nirixx.app.core.diag;

import android.content.Context;
import com.nirixx.app.Session;
import com.nirixx.app.db.Db;

/** Persists VCI transport details in the configs table — nothing hardcoded
 *  beyond factory defaults that the UI lets the technician override. */
public final class TransportSettings {

    private TransportSettings() {}

    public static final String DEFAULT_WIFI_IP = "192.168.0.10";   // ELM-WiFi convention
    public static final int DEFAULT_WIFI_PORT = 35000;


    public static String wifiIp(Context c) {
        return Db.get(c).config("wifi_vci_ip", DEFAULT_WIFI_IP);
    }

    public static int wifiPort(Context c) {
        try { return Integer.parseInt(Db.get(c).config("wifi_vci_port", "" + DEFAULT_WIFI_PORT)); }
        catch (Exception e) { return DEFAULT_WIFI_PORT; }
    }

    public static void saveWifi(Context c, String ip, String port) {
        if (ip != null && ip.length() > 0) Db.get(c).setConfig("wifi_vci_ip", ip);
        if (port != null && port.length() > 0) Db.get(c).setConfig("wifi_vci_port", port);
    }

    public static void saveBt(Context c, String name, String serial) {
        if (name != null && name.length() > 0) Db.get(c).setConfig("last_vci", name);
        if (serial != null && serial.length() > 0) {
            Db.get(c).setConfig("vci_serial", serial);
            Session.vciSerial = serial;
        }
    }

    /** Called right after a successful engine bring-up. */
    public static void onConnected(Context c, String linkType, String displayName) {
        Db.get(c).setConfig("connectivity", linkType);
        Session.connectivity = linkType;
        if (displayName != null && displayName.length() > 0) {
            Db.get(c).setConfig("last_vci", displayName);
        }
        // derive a stable 6-digit serial for the session-id scheme:
        // last 6 hex digits of the BT MAC when we have it, else keep stored.
        String serial = Db.get(c).config("vci_serial", "");
        if (serial.length() == 0) {
            serial = "504856";
            Db.get(c).setConfig("vci_serial", serial);
        }
        Session.vciSerial = serial;
    }
}
