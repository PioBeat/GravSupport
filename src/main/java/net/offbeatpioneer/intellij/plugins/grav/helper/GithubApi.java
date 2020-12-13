package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.StringMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Retrieve Grav-related information from Github
 *
 * @author Dominik Grzelak
 */
public class GithubApi {
    private static Gson gson = new GsonBuilder().create();

    public static String GravRepoUrl = "https://api.github.com/repos/getgrav/grav/releases";

    @SuppressWarnings("unchecked")
    public static List<String> getGravVersionReleases(String gravReleasesUrl) throws IOException {
        List<String> versions;
        String response = getRequest(gravReleasesUrl);
        LinkedTreeMap<String, String>[] map = gson.fromJson(response, LinkedTreeMap[].class);
        versions = Arrays.stream(map)
                .map(x -> {
                    return x.get("tag_name");
                })
                .filter(Objects::nonNull)
                .filter(x -> !x.isEmpty())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        return versions;
    }

    private static String getRequest(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}
