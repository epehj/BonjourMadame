package android.epehj.net.bonjourmadame.utils;

import android.app.Activity;
import android.content.Context;
import android.epehj.net.bonjourmadame.MainActivity;
import android.epehj.net.bonjourmadame.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLConnection;

/**
 * Created by msette on 14/01/2015.
 * simple parser using asynctask
 *
 * TODO : use Volley Library
 */
public class BMParser extends AsyncTask<Void, Void, Bitmap> {

    private final String URL = "http://www.bonjourmadame.fr";
    private final Context context;
    private final Activity activity;

    private Bitmap bitmap = null;

    public BMParser(final Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
    }


    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            Document doc = Jsoup.connect(URL).get();
            // return div containing a tag, itself containing the img
            Element div = doc.getElementsByClass("photo").get(0);

            //<img tag with src att
            String imgUrl = div.child(0).child(0).attr("src");
            Log.d(this.getClass().toString(), "ImgUrl Value "+ imgUrl);
            bitmap = getRemoteImage(new java.net.URL(imgUrl));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p/>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param bitmap The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        ImageView image = (ImageView) activity.findViewById(R.id.imageView);
        image.setImageBitmap(bitmap);
        (MainActivity)activity.setBitmap() = bitmap;
    }

    /**
     * Runs on the UI thread after {@link #publishProgress} is invoked.
     * The specified values are the values passed to {@link #publishProgress}.
     *
     * @param values The values indicating progress.
     * @see #publishProgress
     * @see #doInBackground
     */
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    private Bitmap getRemoteImage(java.net.URL aURL) {
        try {
            final URLConnection conn = aURL.openConnection();
            conn.connect();
            final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            final Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            return bm;
        } catch (IOException e) {}
        return null;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }


}
