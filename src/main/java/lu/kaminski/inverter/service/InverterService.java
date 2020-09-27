package lu.kaminski.inverter.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.common.AppConfig;
import lu.kaminski.inverter.model.rest.ProdRestModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
public class InverterService {

    public ProdRestModel getLiveData() throws Exception {
        log.debug("getLiveData from [" + LocalDateTime.now() + "]");
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            URIBuilder builder = new URIBuilder(AppConfig.inverterAddress + "/v1/livedata");
            URI uri = builder.build();
            HttpGet getRequest = new HttpGet(uri);

            getRequest.addHeader("Authorization", "Basic " + AppConfig.authenticationToken);

            HttpResponse response = httpClient.execute(getRequest);
            HttpEntity entity = response.getEntity();
            String e0_runtime = "";
            String e0_7D = "";
            String e0_30D = "";
            String e0_1Y = "";

            if (entity != null) {
                // return it as a String
                String jsonStr = EntityUtils.toString(entity);
                // String to JSONObject
                JsonObject jsonResp = new Gson().fromJson(jsonStr, JsonObject.class);
                // Get the relevant data
                JsonArray tab = jsonResp.getAsJsonObject("301978-3P21-1920")
                        .getAsJsonArray("points");

                for (int i = 0; i < tab.size(); i++) {
                    JsonElement element = tab.get(i);
                    switch (((JsonObject) element).get("name").getAsString()) {
                        case "E0_runtime":
                            e0_runtime = ((JsonObject) element).get("value").getAsString();
                            break;
                        case "E0_7D":
                            e0_7D = ((JsonObject) element).get("value").getAsString();
                            break;
                        case "E0_30D":
                            e0_30D = ((JsonObject) element).get("value").getAsString();
                            break;
                        case "E0_1Y":
                            e0_1Y = ((JsonObject) element).get("value").getAsString();
                            break;
                        default:
                            break;
                    }
                }
            }
            return ProdRestModel.builder()
                    .date(LocalDate.now().toString())
                    .dayProd(new BigDecimal(e0_runtime).divide(new BigDecimal(1000)))
                    .weekProd(new BigDecimal(e0_7D).divide(new BigDecimal(1000)))
                    .monthProd(new BigDecimal(e0_30D).divide(new BigDecimal(1000)))
                    .yearProd(new BigDecimal(e0_1Y).divide(new BigDecimal(1000)))
                    .build();
        } catch (SocketException se) {
            log.warn("Error while reaching inverter", se);
        } catch (Exception e) {
            log.error("Error while getting live data", e);
        }
        // In case there is no data, the function returns an empty object
        return new ProdRestModel();
    }

    /**
     * Return production data for each requested day
     * @param startDate format YYYY-MM-DDTHH:MM:SS.000Z
     * @param endDate format YYYY-MM-DDTHH:MM:SS.000Z
     */
    public List<ProdRestModel> getDailyProd(String startDate, String endDate) throws Exception {
        log.debug("getDailyProd from [" + startDate + "] to [" + endDate + "]");
        List<ProdRestModel> myList = new ArrayList<>();
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            URIBuilder builder = new URIBuilder(AppConfig.inverterAddress + "/v1/feeds/" +
                    "?client=queryND" +
                    "&feedList[]=E0_runtime" +
                    "&interval=1day" +
                    "&start=" + startDate +
                    "&end=" + endDate);
            URI uri = builder.build();
            HttpGet getRequest = new HttpGet(uri);

            getRequest.addHeader("Authorization", "Basic " + AppConfig.authenticationToken);


            HttpResponse response = httpClient.execute(getRequest);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // return it as a String
                String jsonStr = EntityUtils.toString(entity);
                // String to JSONObject
                JsonObject jsonResp = new Gson().fromJson(jsonStr, JsonObject.class);
                // Get the relevant data
                JsonArray tab = jsonResp.getAsJsonObject("feeds")
                        .getAsJsonObject("ser4:301978-3P21-1920")
                        .getAsJsonObject("datastreams")
                        .getAsJsonObject("E0_runtime")
                        .getAsJsonArray("data");

                for (int i = 0; i < tab.size(); i++) {
                    JsonElement element = tab.get(i);
                    String timestamp = String.valueOf(((JsonObject) element).get("timestamp"));
                    String valueStr = String.valueOf(((JsonObject) element).get("value"));
                    ProdRestModel prod = ProdRestModel.builder()
                            // Convert timestamp to date
                            .date(timestamp.substring(1, 11))
                            .value(new BigDecimal(valueStr).divide(new BigDecimal(1000)))
                            .build();
                    myList.add(prod);
                }
            }
        } catch (SocketException se) {
            log.error("Error while reaching inverter", se);
            throw new Exception("Connexion à l'onduleur impossible");
        } catch (Exception e) {
            log.error("Error while getting production data", e);
            throw new Exception("Erreur lors de la récupération de données" + e.getMessage());
        }
        return myList;
    }

    /**
     * Return production data for requested day
     * @param day format YYYY-MM-DD
     */
    public List<ProdRestModel> getProdForDay(String day) throws Exception {
        log.debug("getProdForDay for [" + day + "]");
        List<ProdRestModel> myList = new ArrayList<>();

        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            URIBuilder builder = new URIBuilder(AppConfig.inverterAddress + "/v1/feeds/" +
                    "?client=query1D" +
                    "feedList[]=Pin" +
                    "&maxDataPointsPerPage=1440" +
                    "&start=" + day + "T00:00:00" +
                    "&end=" + day + "T23:59:59");
            URI uri = builder.build();
            HttpGet getRequest = new HttpGet(uri);

            getRequest.addHeader("Authorization", "Basic " + AppConfig.authenticationToken);


            HttpResponse response = httpClient.execute(getRequest);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // return it as a String
                String jsonStr = EntityUtils.toString(entity);
                // String to JSONObject
                JsonObject jsonResp = new Gson().fromJson(jsonStr, JsonObject.class);
                // Get the relevant data
                JsonArray tab = jsonResp.getAsJsonObject("feeds")
                        .getAsJsonObject("ser4:301978-3P21-1920")
                        .getAsJsonObject("datastreams")
                        .getAsJsonObject("Pin")
                        .getAsJsonArray("data");

                for (int i = 0; i < tab.size(); i++) {
                    JsonElement element = tab.get(i);
                    String timestamp = String.valueOf(((JsonObject) element).get("timestamp"));
                    String valueStr = String.valueOf(((JsonObject) element).get("value"));
                    ProdRestModel prod = ProdRestModel.builder()
                            .date(day)
                            // Remove date to keep only times (HH:MM)
                            .time(timestamp.substring(12, 17))
                            .value(new BigDecimal(valueStr).divide(new BigDecimal(1000),
                                    RoundingMode.CEILING))
                            .build();
                    myList.add(prod);
                }
            }
        } catch (SocketException se) {
            log.error("Error while reaching inverter", se);
            throw new Exception("Connexion à l'onduleur impossible");
        } catch (Exception e) {
            log.error("Error while getting production data", e);
            throw new Exception("Erreur lors de la récupération de données" + e.getMessage());
        }
        return myList;
    }
}
