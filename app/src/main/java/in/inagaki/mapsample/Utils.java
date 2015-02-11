package in.inagaki.mapsample;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.TextView;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Marker;

/**
 * Created by hiroki on 2/2/15.
 */
public class Utils {

    public static Bitmap viewToBitmap(Context c, View view) {
        view.measure(MeasureSpec.getSize(view.getMeasuredWidth()),
                MeasureSpec.getSize(view.getMeasuredHeight()));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Drawable drawable = new BitmapDrawable(c.getResources(),
                android.graphics.Bitmap.createBitmap(view.getDrawingCache()));
        view.setDrawingCacheEnabled(false);
        return AndroidGraphicFactory.convertToBitmap(drawable);
    }

    /**
     * Compatibility method.
     *
     * @param view
     *            the view to set the background on
     * @param background
     *            the background
     */
    @TargetApi(16)
    public static void setBackground(View view, Drawable background) {
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    private Utils() {
        throw new IllegalStateException();
    }

}
