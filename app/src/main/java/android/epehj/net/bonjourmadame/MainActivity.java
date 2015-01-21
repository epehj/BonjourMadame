package android.epehj.net.bonjourmadame;

import android.epehj.net.bonjourmadame.utils.BMUtils;
import android.epehj.net.bonjourmadame.utils.Globals;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * ToDo : sauvegarder les images téléchargées dans un cache : pour pouvoir les réafficher si l'utilisateur relance l'appli sans avoir à reDL et qu'il ne voulait pas spécialement les sauvegarder
 * todo : faire une barre de progression du DL
 * todo : réduire la taille des images qui vont servir à faire un thumbnail (pour faire un genre de carroussel dans l'app, qui va permettre de choisir l'image à voir)
 * todo utiliser volley pour DL les images plutot que un asynctask
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                /*View networkImageView = (NetworkImageView)findViewById(R.id.netimageView);*/
        View imageView = (ImageView)findViewById(R.id.imageView);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // view contains a bitmap
                if(((ImageView)v).getDrawable() != null) {
                    //image from the view
                    savePic(((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap());
                }
                return true;
            }
        });
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

    @Override
    protected void onStop() {
        if(BMUtils.getRequestQueue() != null )
            BMUtils.getRequestQueue().cancelAll(this);
        Log.i(getClass().toString(), "onStop");
        super.onStop();
    }

    /**
     * looking at the activity lifecycle, this is the "main", where request should be put to be executed
     * /!\ attention, on request l'image à chaque fois que l'activité est relancée (changement d'orientation, passage en arrière plan…)
     */
    @Override
    protected void onResume() {
        Log.i(getClass().toString(), "onResume");
        super.onResume();
        // if no pict
        if(((ImageView)findViewById(R.id.imageView)).getDrawable() == null) {
            Log.i(getClass().toString(), "onResume setting image");
            //image from the view
            BMUtils.getPicOfTheDay_volley(this);
        }
        Log.i(getClass().toString(), "onResume image is already set");
    }

    @Override
    protected void onDestroy(){
        Log.i(getClass().toString(), "onStart");
        super.onDestroy();
        }

    @Override
    protected void onRestart(){
        Log.i(getClass().toString(), "onRestart");
        super.onRestart();
    }

    @Override
    protected void onStart(){
        Log.i(getClass().toString(), "onStart");
        super.onStart();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(getClass().toString(), "onPause");
    }

    /**
     * get external storage state
     * @return true if available for reading AND writing
     */
    private boolean isExtStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
