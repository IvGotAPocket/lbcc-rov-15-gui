package common;

import org.json.JSONObject;

public class VehicleCommand {

    public static String getPing() {
        JSONObject ping = new JSONObject();
        ping.put("ping", "rov");
        return ping.toString();
    }

}
