import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LocationDetector {

    public static JSONObject getCurrentLocation() {
        try {
          
            URL ipUrl = new URL("https://api.ipify.org?format=json");
            HttpURLConnection ipConn = (HttpURLConnection) ipUrl.openConnection();
            ipConn.setRequestMethod("GET");
            ipConn.connect();
            Scanner ipScanner = new Scanner(ipConn.getInputStream());
            StringBuilder ipResult = new StringBuilder();
            while (ipScanner.hasNext()) {
                ipResult.append(ipScanner.nextLine());
            }
            ipScanner.close();

            JSONParser parser = new JSONParser();
            JSONObject ipObj = (JSONObject) parser.parse(ipResult.toString());
            String ip = ipObj.get("ip").toString();

            URL geoUrl = new URL("https://ipapi.co/" + ip + "/json/");
            HttpURLConnection geoConn = (HttpURLConnection) geoUrl.openConnection();
            geoConn.setRequestMethod("GET");
            geoConn.connect();
            Scanner geoScanner = new Scanner(geoConn.getInputStream());
            StringBuilder geoResult = new StringBuilder();
            while (geoScanner.hasNext()) {
                geoResult.append(geoScanner.nextLine());
            }
            geoScanner.close();

            return (JSONObject) parser.parse(geoResult.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
