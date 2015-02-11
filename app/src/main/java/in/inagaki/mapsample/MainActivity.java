package in.inagaki.mapsample;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.layer.MyLocationOverlay;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.util.MapViewerTemplate;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends MapViewerTemplate {

    private MyLocationOverlay myLocationOverlay;
    private List<Feature> featureList;
    private List<Marker> placeList = new ArrayList<>();

    @Override
    public void onPause() {
        myLocationOverlay.disableMyLocation();
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        this.myLocationOverlay.enableMyLocation(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getMapViewId() {
        return R.id.mapView;
    }

    @Override
    protected String getMapFileName() {
        return "malaysia_singapore_brunei.map";
    }

    @Override
    protected XmlRenderTheme getRenderTheme() {
        return InternalRenderTheme.OSMARENDER;
    }

    @Override
    protected void createLayers() {
        LayerManager layerManager = this.mapView.getLayerManager();
        final Layers layers = layerManager.getLayers();

        // reading geoson
        {
            try {
                File file = new File(Environment.getExternalStorageDirectory(), "sample.geojson");
                BufferedReader reader = new BufferedReader(new FileReader(file));
                InputStream fileInputStream = new FileInputStream(file);
                FeatureCollection featureCollection =
                        new ObjectMapper().readValue(fileInputStream, FeatureCollection.class);
                featureList = featureCollection.getFeatures();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }

        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(this.tileCaches.get(0),
                this.mapView.getModel().mapViewPosition, getMapFile(), getRenderTheme(), false, true);
        layers.add(tileRendererLayer);

        for (final Feature feature : featureList) {
            Marker point = null;
            GeoJsonObject geometry = feature.getGeometry();
            final String description = feature.getProperty("description");
            if (geometry instanceof Point) {
                final LatLong latLong = new LatLong(((Point) geometry).getCoordinates().getLatitude(), ((Point) geometry).getCoordinates().getLongitude());
                Drawable drawable = getResources().getDrawable(R.drawable.marker_red);
                final Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
                bitmap.incrementRefCount();
                point = new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2) {
                    private Marker marker;
                    private Marker text;

                    @Override
                    protected void onAdd() {
                        String name = feature.getProperty("name");
                        TextView bubbleView = new TextView(MainActivity.this);
                        bubbleView.setGravity(Gravity.CENTER);
                        bubbleView.setMaxEms(20);
                        bubbleView.setTextSize(15);
                        bubbleView.setTextColor(Color.BLACK);
                        bubbleView.setText(name);
                        Bitmap bubbleBitmap = Utils.viewToBitmap(MainActivity.this, bubbleView);
                        bubbleBitmap.incrementRefCount();
                        text = new Marker(latLong, bubbleBitmap, 0, bubbleBitmap.getHeight() / 2);
                        MainActivity.this.mapView.getLayerManager().getLayers().add(text);
                    }

                    @Override
                    public boolean onTap(LatLong tapLatLong, org.mapsforge.core.model.Point layerXY, org.mapsforge.core.model.Point tapXY) {
                        if (contains(layerXY, tapXY)) {
                            Log.w("Tapp", "The Marker was touched with onTap: "
                                    + this.getLatLong().toString());

                            if (marker != null && MainActivity.this.mapView.getLayerManager().getLayers().contains(marker)) {
                                MainActivity.this.mapView.getLayerManager().getLayers().remove(marker);
                            } else {
                                TextView bubbleView = new TextView(MainActivity.this);
                                Utils.setBackground(bubbleView, MainActivity.this.getResources().getDrawable(R.drawable.balloon_overlay_unfocused));
                                bubbleView.setGravity(Gravity.CENTER);
                                bubbleView.setMaxEms(20);
                                bubbleView.setTextSize(15);
                                bubbleView.setTextColor(Color.BLACK);
                                bubbleView.setText(description);
                                Bitmap bubble1 = Utils.viewToBitmap(MainActivity.this, bubbleView);
                                bubble1.incrementRefCount();
                                marker = new Marker(latLong, bubble1, 0, -bubble1.getHeight() / 2 - bitmap.getHeight());
                                MainActivity.this.mapView.getLayerManager().getLayers().add(marker);
                            }

                            return true;
                        }
                        return false;
                    }

                    @Override
                    protected void onRemove() {
                        if (marker != null) {
                            MainActivity.this.mapView.getLayerManager().getLayers().remove(marker);
                        }
                        if (text != null) {
                            MainActivity.this.mapView.getLayerManager().getLayers().remove(text);
                        }
                    }

                    @Override
                    public boolean onLongPress(LatLong tapLatLong, org.mapsforge.core.model.Point layerXY, org.mapsforge.core.model.Point tapXY) {
                        MainActivity.this.mapView.getLayerManager().getLayers().remove(this);
                        return true;
                    }
                };
            }
            layers.add(point);

        }

        // a marker to show at the position
        Drawable drawable = getResources().getDrawable(R.drawable.marker_green);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        // create the overlay and tell it to follow the location
        this.myLocationOverlay = new MyLocationOverlay(this,
                this.mapView.getModel().mapViewPosition, bitmap);
        this.myLocationOverlay.setSnapToLocationEnabled(false);
        layers.add(this.myLocationOverlay);

    }

    @Override
    protected void createTileCaches() {
        this.tileCaches.add(AndroidUtil.createTileCache(this, getPersistableId(),
                this.mapView.getModel().displayModel.getTileSize(), this.getScreenRatio(),
                this.mapView.getModel().frameBufferModel.getOverdrawFactor(),
                false, 0
        ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String map = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.MAP, null);
        if (map == null) {
            Intent intent = new Intent(this, MapSelectionActivity.class);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        ActionBar actionBar = this.getActionBar();
//        actionBar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.map_selection) {
            Intent intent = new Intent(this, MapSelectionActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
