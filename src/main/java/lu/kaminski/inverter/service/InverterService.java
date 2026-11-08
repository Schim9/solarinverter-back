package lu.kaminski.inverter.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import lu.kaminski.inverter.config.properties.InverterProperties;
import lu.kaminski.inverter.model.rest.DailyProdResponse;
import lu.kaminski.inverter.model.rest.LiveDataResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
public class InverterService {

    private final InverterProperties inverterProperties;
    private final RestClient restClient;

    public InverterService(InverterProperties inverterProperties, RestClient.Builder restClientBuilder) {
        this.inverterProperties = inverterProperties;
        this.restClient = restClientBuilder
                .defaultHeader("Authorization", "Basic " + inverterProperties.getToken())
                .build();
    }

    public LiveDataResponse getLiveData() throws Exception {
        log.debug("getLiveData from [" + LocalDateTime.now() + "]");
        try {
            String jsonStr = restClient.get()
                    .uri(inverterProperties.getUrl() + "/v1/livedata")
                    .retrieve()
                    .body(String.class);

            String e0_runtime = "";
            String e0_7D = "";
            String e0_30D = "";
            String e0_1Y = "";

            if (jsonStr != null) {
                JsonObject jsonResp = new Gson().fromJson(jsonStr, JsonObject.class);
                JsonArray tab = jsonResp.getAsJsonObject(inverterProperties.getDeviceId())
                        .getAsJsonArray("points");

                for (int i = 0; i < tab.size(); i++) {
                    JsonElement element = tab.get(i);
                    switch (((JsonObject) element).get("name").getAsString()) {
                        case "E0_runtime" -> e0_runtime = ((JsonObject) element).get("value").getAsString();
                        case "E0_7D"      -> e0_7D      = ((JsonObject) element).get("value").getAsString();
                        case "E0_30D"     -> e0_30D     = ((JsonObject) element).get("value").getAsString();
                        case "E0_1Y"      -> e0_1Y      = ((JsonObject) element).get("value").getAsString();
                        default -> {}
                    }
                }
            }
            return LiveDataResponse.builder()
                    .date(LocalDate.now().toString())
                    .dayProd(new BigDecimal(e0_runtime).divide(new BigDecimal(1000), RoundingMode.CEILING))
                    .weekProd(new BigDecimal(e0_7D).divide(new BigDecimal(1000), RoundingMode.CEILING))
                    .monthProd(new BigDecimal(e0_30D).divide(new BigDecimal(1000), RoundingMode.CEILING))
                    .yearProd(new BigDecimal(e0_1Y).divide(new BigDecimal(1000), RoundingMode.CEILING))
                    .build();
        } catch (ResourceAccessException e) {
            log.warn("Error while reaching inverter", e);
            throw new Exception("Inverter connection failed");
        } catch (Exception e) {
            log.error("Error while getting live data", e);
            throw new Exception("Error retrieving live data: " + e.getMessage());
        }
    }

    /**
     * Return production data for each requested day
     * @param startDate format YYYY-MM-DDTHH:MM:SS.000Z
     * @param endDate format YYYY-MM-DDTHH:MM:SS.000Z
     */
    public List<DailyProdResponse> getDailyProd(String startDate, String endDate) throws Exception {
        log.debug("getDailyProd from [" + startDate + "] to [" + endDate + "]");
        List<DailyProdResponse> myList = new ArrayList<>();
        try {
            String uri = inverterProperties.getUrl() + "/v1/feeds/" +
                    "?client=queryND" +
                    "&feedList[]=E0_runtime" +
                    "&interval=1day" +
                    "&start=" + startDate +
                    "&end=" + endDate;

            String jsonStr = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (jsonStr != null) {
                JsonObject jsonResp = new Gson().fromJson(jsonStr, JsonObject.class);
                JsonArray tab = jsonResp.getAsJsonObject("feeds")
                        .getAsJsonObject("ser4:" + inverterProperties.getDeviceId())
                        .getAsJsonObject("datastreams")
                        .getAsJsonObject("E0_runtime")
                        .getAsJsonArray("data");

                for (int i = 0; i < tab.size(); i++) {
                    JsonElement element = tab.get(i);
                    String timestamp = String.valueOf(((JsonObject) element).get("timestamp"));
                    String valueStr  = String.valueOf(((JsonObject) element).get("value"));
                    myList.add(DailyProdResponse.builder()
                            .date(timestamp.substring(1, 11))
                            .value(new BigDecimal(valueStr).divide(new BigDecimal(1000), RoundingMode.CEILING))
                            .build());
                }
            }
        } catch (ResourceAccessException e) {
            log.error("Error while reaching inverter", e);
            throw new Exception("Inverter connection failed");
        } catch (Exception e) {
            log.error("Error while getting production data", e);
            throw new Exception("Error retrieving production data: " + e.getMessage());
        }
        return myList;
    }
}
