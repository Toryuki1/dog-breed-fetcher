package dogapi;

import java.util.*;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {
    // TODO Task 2: Complete this class
    private int callsMade = 0;

    /** The underlying fetcher we delegate to on a cache miss. */
    private final BreedFetcher fetcher;

    /** Cache: normalized breed name -> immutable list of sub-breeds. */
    private final Map<String, List<String>> cache = new HashMap<>();

    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.fetcher = Objects.requireNonNull(fetcher, "fetcher must not be null");
    }

    @Override
    public List<String> getSubBreeds(String breed) {
        // Normalize key to improve cache hits (API expects lowercase)
        String key = breed == null ? null : breed.toLowerCase(Locale.ROOT);

        if (key != null && cache.containsKey(key)) {
            // Return a defensive copy to avoid external mutation of our cached list
            return new ArrayList<>(cache.get(key));
        }

        // Cache miss: delegate to the underlying fetcher and count the call
        callsMade++;
        try {
            List<String> subBreeds = fetcher.getSubBreeds(breed);
            // Cache even empty lists (some breeds have no sub-breeds)
            if (key != null) {
                // Store an unmodifiable copy to prevent accidental mutation of the cache
                cache.put(key, Collections.unmodifiableList(new ArrayList<>(subBreeds)));
            }
            // Return a fresh mutable copy to callers (tests may not rely on mutability, but it's safer)
            return new ArrayList<>(subBreeds);
        } catch (BreedNotFoundException e) {
            // Do NOT cache failures; just propagate
            throw e;
        }
    }

    public int getCallsMade() {
        return callsMade;
    }
}