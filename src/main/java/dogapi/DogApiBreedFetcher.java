package dogapi;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<String> getSubBreeds(String breed) {
        String url = "https://dog.ceo/api/breed/" + breed.toLowerCase() + "/list";
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new BreedNotFoundException("Failed to fetch breed: " + breed);
            }

            String jsonData = response.body().string();
            JSONObject json = new JSONObject(jsonData);

            if (!json.getString("status").equals("success")) {
                throw new BreedNotFoundException("Breed not found: " + breed);
            }

            JSONArray subBreedsJson = json.getJSONArray("message");
            List<String> subBreeds = new ArrayList<>();

            for (int i = 0; i < subBreedsJson.length(); i++) {
                subBreeds.add(subBreedsJson.getString(i));
            }

            return subBreeds;
        } catch (IOException e) {
            throw new BreedNotFoundException("Error fetching data for breed: " + breed);
        }
    }
    public String run(String baseURL, String paramKey, String paramValue) throws IOException {
        HttpUrl url = HttpUrl.parse(baseURL).newBuilder()
                .addQueryParameter(paramKey, paramValue)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader(paramKey, paramValue)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}