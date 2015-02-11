package in.inagaki.mapsample;

import android.app.Application;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

/**
 * Created by hiroki on 1/31/15.
 */
public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidGraphicFactory.createInstance(this);
    }
}
