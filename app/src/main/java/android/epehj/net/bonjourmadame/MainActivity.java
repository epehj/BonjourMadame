package android.epehj.net.bonjourmadame;

import android.content.Context;
import android.content.SharedPreferences;
import android.epehj.net.bonjourmadame.utils.BMParser;
import android.epehj.net.bonjourmadame.utils.Globals;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * ToDo : sauvegarder les images téléchargées dans un cache : pour pouvoir les réafficher si l'utilisateur relance l'appli sans avoir à reDL et qu'il ne voulait pas spécialement les sauvegarder
 * todo : faire une barre de progression du DL
 * todo : réduire la taille des images qui vont servir à faire un thumbnail (pour faire un genre de carroussel dans l'app, qui va permettre de choisir l'image à voir)
 * todo utiliser volley pour DL les images plutot que un asynctask
 */
public class MainActivity extends ActionBarActivity {

    private BMParser bm;
    //useless non ?
    private Bitmap bitmap;

    private RequestQueue requestQueue;
    //private ImageLoader mVolleyImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bm = new BMParser(this);

        // get the picture of the day
        //getPicOfTheDay_async(true);

        getPicOfTheDay_volley();
        View networkImageView = (NetworkImageView)findViewById(R.id.netimageView);
        networkImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    savePic(bitmap);
                return true;
            }
        });
    }

    private void getPicOfTheDay_volley() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest sr = new StringRequest(Request.Method.GET, Globals.URL, new Response.Listener<String>() {
            /**
             * Called when a response is received.
             *
             * @param response  string containing full html page
             */
            @Override
            public void onResponse(String response) {
                Log.d(getClass().toString(), "proout response received ");

                String img = parse(response);
                bitmap = getRemoteImage(img);
                NetworkImageView niv = (NetworkImageView) findViewById(R.id.netimageView);
                niv.setImageBitmap(bitmap);
            }

            //method to parse BM page and get url of the pic
            private String parse(String response) {
                String imgUrl = null;
                try {
                    Document doc = Jsoup.connect(Globals.URL).get();;
                    // return div containing a tag, itself containing the img
                    Element div = doc.getElementsByClass("photo").get(0);
                    //<img tag with src att
                    imgUrl = div.child(0).child(0).attr("src");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return imgUrl;
            }

            private Bitmap getRemoteImage(String aURL) {
                try {
                    URL imgURL = new URL(aURL);
                    final URLConnection conn = imgURL.openConnection();
                    conn.connect();
                    final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    final Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    return bm;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {}
                return null;
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(getClass().toString(), "error encountered "+error.toString());
            }
        });
        requestQueue.add(sr);
    }

    /**
     * Simple method to save the downloaded pic to phone external storage
     * @param bitmap to be saved
     */
    private void savePic(final Bitmap bitmap) {

        final DateTime today = new DateTime();

        if(isExtStorageAvailable()) {
            File dir = new File(Globals.DIRECTORY);
            if(!dir.exists())
                dir.mkdir();
            //pic of the day
            //ne correspond pas au bon repertoire
            File pic = new File(Globals.DIRECTORY + File.separator + today.toString("ddMMyyyy")+".jpeg");
            if (!pic.exists()){
                //saving pic on external storage
                try {
                    FileOutputStream fos = new FileOutputStream(pic);
                    //sending bitmap to fos, no compression, jpeg format
                   if(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
                       Toast.makeText(getApplicationContext(), "Pic successfully saved", Toast.LENGTH_LONG).show();
                       Log.d(getClass().toString(), "Picture is saved");
                       Log.d(getClass().toString(), "Directory "+pic.getPath());
                   }
                   fos.flush();
                   fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
                Toast.makeText(getApplicationContext(), "This pic is already saved", Toast.LENGTH_LONG).show();
        }
    }

    private void getPicOfTheDay_async(boolean test) {

        DateTimeFormatter dtf = DateTimeFormat.forPattern(Globals.FORMAT_HOUR);
        DateTime today = new DateTime();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        //if pref lastMaj does not exists, defaut returned value is day
        String l = settings.getString("lastMaj", today.toString(Globals.FORMAT_HOUR));
        //maybe there is another way to get datetime from string
        DateTime lastMaj = dtf.parseDateTime(l);

        //10am is when BM is updated
        //if first time of the day app is launched, or if was previously launched but b4 10am
        //then dl pic, create thumb and save it to cache it, in internal files
        //else, show cached pic
        if(((lastMaj.toString(Globals.FORMAT).equals(today.toString(Globals.FORMAT))
                && lastMaj.getHourOfDay() < 10)) || (today.getDayOfYear() - lastMaj.getDayOfYear() > 0) || test)  {
            bm.execute();
            //asynctask donc il arrive que le bitmap soit null quand on y accède…
           //createCache();
        }
         else {
            showPic();
        }

        //update lastMaj datetime
        SharedPreferences.Editor e = settings.edit();
        e.putString("lastMaj", today.toString(Globals.FORMAT_HOUR));
        e.apply();

    }

    //
    private void createCache() {
        try{
            DateTime today = new DateTime();
            //internalStorage test testencore
            FileOutputStream fos = getApplicationContext().openFileOutput(today.toString("ddMMyyyy")+".jpeg", Context.MODE_PRIVATE);
            FileOutputStream fosThumb = getApplicationContext().openFileOutput(today.toString("ddMMyyyy")+"_thumb.jpeg", Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);

            //screensize
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            //10thumbs in a screen ?
            Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, metrics.heightPixels/10, metrics.widthPixels/10);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fosThumb);

            fos.flush();
            fos.close();
            fosThumb.flush();
            fosThumb.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * load pic from phone memory
     */
    private void showPic() {
        /*ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);*/
    }

    /**
     * get external storage state
     * @return true if available for reading AND writing
     */
    private boolean isExtStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
