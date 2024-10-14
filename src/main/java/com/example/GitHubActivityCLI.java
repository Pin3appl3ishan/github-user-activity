package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GitHubActivityCLI {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java GitHubActivityCLI <username>");
            return;
        }
        GitHubActivityCLI cli = new GitHubActivityCLI();
        cli.fetchGitHubActivity(args[0]);
    }

    private void fetchGitHubActivity(String username) {
        String GITHUB_API_URL = "https://api.github.com/users/" + username + "/events";

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(GITHUB_API_URL))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Java CLI Tool") // Added User-Agent header
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                System.out.println("User not found. Please check the username.");
                return;
            }
            if (response.statusCode() == 200) {
                JsonParser parser = new JsonParser();
                JsonArray jsonArray = parser.parseString(response.body()).getAsJsonArray();
                if (jsonArray.size() == 0) {
                    System.out.println("No recent events found for this user.");
                } else {
                    displayActivity(jsonArray);
                }
            } else if (response.statusCode() == 403) {
                System.out.println("API rate limit exceeded. Please try again later.");
            } else {
                System.out.println("Error: HTTP Status Code " + response.statusCode());
            }
        } catch (URISyntaxException uriSyntaxException) {
            System.out.println("Invalid URI syntax: " + uriSyntaxException.getMessage());
        } catch (IOException ioException) {
            System.out.println("IO Exception: " + ioException.getMessage());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            System.out.println("Request was interrupted.");
        }
    }

    private void displayActivity(JsonArray events) {
        for (JsonElement element : events) {
            JsonObject event = element.getAsJsonObject();
            String type = event.get("type").getAsString();
            String action;
            switch (type) {
                case "PushEvent":
                    JsonArray commits = event.get("payload").getAsJsonObject().get("commits").getAsJsonArray();
                    int commitCount = commits.size();
                    String repoNamePush = event.get("repo").getAsJsonObject().get("name").getAsString();
                    action = "Pushed " + commitCount + " commit(s) to " + repoNamePush;
                    break;
                case "IssuesEvent":
                    String issueAction = event.get("payload").getAsJsonObject().get("action").getAsString();
                    String issueTitle = event.get("payload").getAsJsonObject()
                            .getAsJsonObject("issue")
                            .get("title").getAsString();
                    action = capitalizeFirstLetter(issueAction) + " an issue in "
                            + event.get("repo").getAsJsonObject().get("name").getAsString()
                            + ": " + issueTitle;
                    break;
                case "WatchEvent":
                    String repoNameWatch = event.get("repo").getAsJsonObject().get("name").getAsString();
                    action = "Starred " + repoNameWatch;
                    break;
                case "ForkEvent":
                    String repoNameFork = event.get("repo").getAsJsonObject().get("name").getAsString();
                    action = "Forked " + repoNameFork;
                    break;
                case "CreateEvent":
                    String refType = event.get("payload").getAsJsonObject().get("ref_type").getAsString();
                    String ref = event.get("payload").getAsJsonObject().has("ref") ?
                            event.get("payload").getAsJsonObject().get("ref").getAsString() : "";
                    String repoNameCreate = event.get("repo").getAsJsonObject().get("name").getAsString();
                    if (!ref.isEmpty()) {
                        action = "Created " + refType + " '" + ref + "' in " + repoNameCreate;
                    } else {
                        action = "Created " + refType + " in " + repoNameCreate;
                    }
                    break;
                default:
                    String repoNameDefault = event.get("repo").getAsJsonObject().get("name").getAsString();
                    action = type.replace("Event", "") + " in " + repoNameDefault;
                    break;
            }
            System.out.println("- " + action);
        }
    }

    /**
     * Capitalizes the first letter of the input string.
     *
     * @param input The string to capitalize.
     * @return The string with the first letter capitalized.
     */
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
