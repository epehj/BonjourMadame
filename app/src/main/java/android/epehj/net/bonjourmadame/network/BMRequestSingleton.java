package android.epehj.net.bonjourmadame.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by msette on 16/01/2015.
 */
public class BMRequestSingleton {
    private ImageLoader imageLoader;
    private RequestQueue requestQueue;
    private static BMRequestSingleton instance;
    private static Context context;

    private BMRequestSingleton(final Context c){
        context = c;
        requestQueue = getRequestQueue();
        //todo changer la taille du cache
        imageLoader = new ImageLoader(getRequestQueue(), new BitmapLruCache(100));
        }

    public static synchronized BMRequestSingleton getInstance(Context c){
        if(instance == null)
            instance = new BMRequestSingleton(c);
        return instance;
    }

    /**
     *
     * @return
     */
    public RequestQueue getRequestQueue(){
        if (requestQueue == null) {
            //application context to get the request available during the whole app lifecycle
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public void addToRequest(Request<String> r){
        getRequestQueue().add(r);
    }

    public ImageLoader getImageLoader(){
        return imageLoader;
    }

}
