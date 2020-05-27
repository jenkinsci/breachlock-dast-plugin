package com.breachlock.helpers;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import org.json.simple.parser.ParseException;


/**
 * @author mitchel.k@breachlock.com
 */
public class ApiHelper {
    private final OkHttpClient client;
    private final String apiKey;
    private final String userAgent;
    private final String basePath;
    private String endpoint;
    private RequestBody formBody;
    
    /**
     * Parse string as JSON.
     * 
     * @param json String of JSON data
     * @return JSONArray
     */
    @SuppressWarnings("empty-statement")
    public JSONArray parseJSON(String json) {
        JSONArray jsonArray = new JSONArray();

        try {
            JSONParser parser = new JSONParser();
            Object jsonObj = parser.parse(json);

            jsonArray = (JSONArray) jsonObj;
        } catch (ParseException ex) {
            jsonArray = new JSONArray();
        }
        finally {
            return jsonArray;
        }
    }
    
    /**
     * Create a new HTTP client
     */
    public ApiHelper() {
        this.client = new OkHttpClient();
        
        this.apiKey = "TRUEREST apikey=d0a7e7997b6d5fcd55f795932611b87cd923e88837b637352941ef819dc8ca282";
        this.userAgent = "JenkinsPlugin/1.0";
        this.basePath = "https://acc.breachlock.com";
        this.endpoint = "/";
    }

    /**
     * Set and endpoint to for the base path.
     * 
     * @param endpoint Endpoint to visit
     */
    public void setEndpoint(String endpoint) {
        StringBuilder newEndpoint = new StringBuilder();
        newEndpoint.append(this.basePath);
        newEndpoint.append(endpoint);

        this.endpoint = newEndpoint.toString();
    }
    
    /**
     * Set form content for POST requests.
     * 
     * @param formBody Build form data
     */
    public void setFormBody(RequestBody formBody) {
        this.formBody = formBody;
    }

    /**
     * Get the base path to the API.
     * 
     * @return Return path to the API
     */
    public String getBasePath() {
        return this.basePath;
    }

    /**
     * Perform a HTTP GET-request.
     * 
     * @return Parse body content
     */
    public String getRequest() {
        String body = null;
        
        final Request request = new Request.Builder()
                .url(this.endpoint)
                .addHeader("Authorization", this.apiKey)
                .addHeader("User-Agent", this.userAgent)
                .build();

        try {
            Response response = client.newCall(request).execute();
            ResponseBody resBody = response.body();
            
            if (resBody != null) {
                body = resBody.string();
                resBody.close();
            } else {
                body = "";
            }
        } catch (IOException e) {
            body = "";
        }
        
        return body;
    }

    /**
     * Perform a HTTP POT-request.
     * 
     * @return Parse body content
     */
    @SuppressWarnings("kn")
    public String postRequest() {
        String body = null;
        
        final Request request = new Request.Builder()
                .url(this.endpoint)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", this.apiKey)
                .addHeader("User-Agent", this.userAgent)
                .post(this.formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            ResponseBody resBody = response.body();
            
            if (resBody != null) {
                body = resBody.string();
                resBody.close();
            } else {
                body = "";
            }
        } catch (IOException e) {
            body = "";
        }
        
        return body;
    }
}
