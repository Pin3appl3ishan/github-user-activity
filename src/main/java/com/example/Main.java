package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    private static HttpRequest fetchUserData(String username, int numberOfEventsToDisplay) {
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.github.com/users/" + username + "/events?per_page=" + numberOfEventsToDisplay))
                    .GET()
                    .build();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return request;
    }

    public static void main(String[] args) {

    }
}