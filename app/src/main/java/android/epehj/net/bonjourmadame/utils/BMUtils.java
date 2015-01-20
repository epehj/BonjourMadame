package android.epehj.net.bonjourmadame.utils;

import android.app.Activity;
import android.epehj.net.bonjourmadame.R;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by msette on 20/01/2015.
 */
public class BMUtils {

    private static RequestQueue requestQueue;

    //TODO faire la vérification que l'image à pas déjà été DL
    public static void getPicOfTheDay_volley(final Activity activity) {
        requestQueue = Volley.newRequestQueue(activity.getApplicationContext());
        Log.i(activity.getClass().toString(), "sending request to get image bitmap");
        StringRequest sr = new StringRequest(Request.Method.GET, Globals.URL, new Response.Listener<String>() {
            /**
             * Called when a response is received.
             *
             * @param response string containing full html page
             */
            @Override
            public void onResponse(String response) {
                Log.d(getClass().toString(), "response received ");
                String img = parse(response);
                // si réponse, faire une ImageRequest avec l'url de la page !
                getRemoteImage(activity,img);

            }
        }, new Response.ErrorListener(){

            /**
             * Callback method that an error has been occurred with the
             * provided error code and optional user-readable message.
             *
             * @param error
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity.getApplicationContext(), "Image couldn't be updated", Toast.LENGTH_LONG).show();
                Log.e(activity.getClass().toString(), error.getMessage());
            }
        });
        requestQueue.add(sr);
    }

    private static void getRemoteImage(final Activity activity, String aURL) {
        requestQueue = Volley.newRequestQueue(activity.getApplicationContext());
        ImageRequest ir = new ImageRequest(aURL, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                /*NetworkImageView niv = (NetworkImageView) findViewById(R.id.netimageView);*/
                ImageView niv = (ImageView) activity.findViewById(R.id.imageView);
                niv.setImageBitmap(response);
                Log.i(getClass().toString(), "getRemoteImage().onResponse() imaqe set");

            }
        }, 0, 0, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(activity.getApplicationContext(),"Image couldn't be downloaded", Toast.LENGTH_LONG).show();
                        Log.e(activity.getClass().toString(), error.toString());
                    }
                });


        requestQueue.add(ir);
    }

    //method to parse BM page and get url of the pic
    private static String parse(String response) {
        String imgUrl = null;
        // Document doc = Jsoup.connect(Globals.URL).get();
        Document d = Jsoup.parse(response);

        // return div containing a tag, itself containing the img
        Element div = d.getElementsByClass("photo").get(0);
        //<img tag with src att
        imgUrl = div.child(0).child(0).attr("src");

        return imgUrl;
    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }
}
