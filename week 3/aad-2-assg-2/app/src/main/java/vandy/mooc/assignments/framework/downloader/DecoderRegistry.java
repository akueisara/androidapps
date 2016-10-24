package vandy.mooc.assignments.framework.downloader;

import android.net.Uri;

import java.util.HashMap;

/**
 * A factory class that produces decoder implementations for specified target
 * resource data types. These decoders are used decode downloaded data sources
 * (data files currently) to various resource types.
 * <p/>
 * Default decoders can either be registered added to the constructor using
 * calls to registerDecoder() or they can by installed dynamically on an as
 * needed basis. Note that this factory only supports registering a single
 * decoder per data type. Attempts to re-register a decoder for the same class
 * will silently be ignored.
 */
public final class DecoderRegistry {
    /**
     * This singleton.
     */
    private static DecoderRegistry sDecoderFactory;

    /**
     * Maps resource data class (key) to a decoder implementation class.
     */
    private final HashMap<Class, Decoder> mDecoderMap;

    /**
     * Constructs the singleton and installs the default decoder implementation
     * that provides directly access to cached download files (creates a temp
     * copy of the cache file). Other decoders can be added dynamically by
     * calling registerDecoder().
     */
    private DecoderRegistry() {
        mDecoderMap = new HashMap<>();
        registerDecoder(Uri.class, new CacheDecoder());
        //registerDecoder(Uri.class, new DefaultDecoder());
    }

    /**
     * Returns the decoder registry singleton. The first call will construct the
     * singleton.
     *
     * @return This singleton.
     */
    public static DecoderRegistry get() {
        if (sDecoderFactory == null) {
            synchronized (DecoderRegistry.class) {
                if (sDecoderFactory == null) {
                    sDecoderFactory = new DecoderRegistry();
                }
            }
        }

        return sDecoderFactory;
    }

    /**
     * Uses the Factory pattern to construct and return a decoder that that
     * matches the input and output data types.
     */
    public Decoder getDecoder(Class input, Class output) {
        Decoder decoder = mDecoderMap.get(output);
        return decoder != null && decoder.canDecodeFrom(input) ? decoder : null;
    }

    /**
     * Installs a decoder for a specific resource type. Registering the same
     * decoder for the the same resource type is allowed and is silently
     * ignored. However, registering a new decoder for an already registered
     * resource is considered an implementation error and this method will throw
     * an IllegalArgumentException when this occurs.
     *
     * @param resource The resource class.
     * @param decoder  The decoder class.
     */
    public void registerDecoder(Class resource, Decoder decoder) {
        if (!mDecoderMap.containsKey(resource)) {
            mDecoderMap.put(resource, decoder);
        } else {
            Decoder registeredDecoder = mDecoderMap.get(resource);
            if (!registeredDecoder.getClass().equals(decoder.getClass())) {
                throw new IllegalArgumentException(
                        "A decoder already exists for " + resource.getName());
            }
        }
    }
}
