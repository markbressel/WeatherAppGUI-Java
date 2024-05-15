import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// ez a backend resz ami vissza fogja teriteni a legkesobbi
// idojaras adatokat feluletnek, es lathatova teszi majd
// a felhasznalo szamara
public class WeatherApp {
    // idojaras adatok kiirasa megadott helyrol
    public static JSONObject getWeatherData(String locationName){
        // megkeresi a helyseget koordinatak alapjan (geolocation API)
        JSONArray locationData = getLocationData(locationName);

        // ha a helyseg adatai nincsenek megadva, teritsen vissza null - t
        if(locationData == null || locationData.isEmpty()){
            return null;
        }

        // hosszusag es szelesseg adatok
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        //API epitese az URL koordinatak alapjan
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=auto";

        try{
            // API meghivasa es valasza
            HttpURLConnection conn = fetchApiResponse(urlString);

            //megnezzuk a valaszt
            // 200 azt jelenti hogy a kapcsolodas sikeres volt
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // eredmeny eltarolasa json bol
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()){
                //kiolvassuk es eltaroljuk az adatot egy string builder - be
                resultJson.append(scanner.nextLine());
            }

            //scanner bezarasa
            scanner.close();

            //url kapcsolat bezarasa
            conn.disconnect();

            // atalakitas
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            //orai adatok lekerese
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            //szeretnenk tudni a mostani orai adatokat
            // kell a mostnai ora indexe hozza
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // homerseklet kerese
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // idojaras kod
            JSONArray weathercode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // paratartalom lekerese
            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // szelsebesseg lekerese
            JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);

            // az idojaras JSON datum objektum osszeallitasa amit fel fogunk hasznalni a frontend be
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // megtalalja a koordinatat az adott helyseg neverol
    public static JSONArray getLocationData(String locationName){
        // replace any whitespace in location name to + to adhere to API's request format
        locationName = locationName.replaceAll(" ", "+");

        // megcsinalja az API url - t a helyseg parameterevel
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try{
            // meghivja az API - t es valaszol
            HttpURLConnection conn = fetchApiResponse(urlString);

            // megnezzuk a valaszt
            // 200 azt jelenti hogy sikeres volt a csatlakozas
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }else{
                // eltaroljuk az eredmenyt
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // kiolvassa es eltarolja az eredmenyezett json adatot a string builder be
                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                // scanner bezarasa
                scanner.close();

                // url kapcsolat bezarasa
                conn.disconnect();

                // json string atalakitasa objektumma
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // lista a hely adatokkal amit az API generalt a helyseg nevekbol
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        // nem talalja a helyseget
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            // lehetoseg hogy lerrehozza a kapcsolatot
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // beallitja a metodus kereset
            conn.setRequestMethod("GET");

            // hozzakapcsolodik az API hoz
            conn.connect();
            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }

        // ha nem tudja letrehozni a kapcsolatot
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();

        // vegigmegyunk az idokon es megnezzuk hogy melyik hasonlo a mi jelenlegi idonkhoz
        for(int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                // visszateriti az indexet
                return i;
            }
        }

        return 0;
    }

    private static String getCurrentTime(){
        // jelenlegi datum es ido
        LocalDateTime currentDateTime = LocalDateTime.now();

        // formatum 2024-05-04T00:00 igy olvassa ki
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // a jelenlegi datum es ido formazasa es kiirasa
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    // idojaras kodot alakit olvashatova
    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if(weathercode == 0L){
            // clear
            weatherCondition = "Tiszta";
        }else if(weathercode > 0L && weathercode <= 3L){
            // cloudy
            weatherCondition = "Felhős";
        }else if((weathercode >= 51L && weathercode <= 67L)
                    || (weathercode >= 80L && weathercode <= 99L)){
            // rain
            weatherCondition = "Eső";
        }else if(weathercode >= 71L && weathercode <= 77L){
            // snow
            weatherCondition = "Hó";
        }

        return weatherCondition;
    }
}
