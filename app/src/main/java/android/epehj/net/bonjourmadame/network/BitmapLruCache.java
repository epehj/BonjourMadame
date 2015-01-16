package android.epehj.net.bonjourmadame.network;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by msette on 16/01/2015.
 */
public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache{


    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public BitmapLruCache(int maxSize) {
        super(maxSize);

    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}
