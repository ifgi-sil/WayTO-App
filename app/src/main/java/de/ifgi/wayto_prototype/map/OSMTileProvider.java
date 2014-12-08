package de.ifgi.wayto_prototype.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class to request tiles from OpenStreetMap
 *
 * @author Marius Runde
 */
public class OSMTileProvider implements TileProvider {

    private String url = "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png";
    private Paint opacityPaint = new Paint();

    public OSMTileProvider() {
        setOpacity(75);
    }

    /**
     * Sets the desired opacity of map {@link Tile}s, as a percentage where 0% is invisible and 100% is completely opaque.
     *
     * @param opacity The desired opacity of map {@link Tile}s (as a percentage between 0 and 100, inclusive)
     */
    public void setOpacity(int opacity) {
        int alpha = (int) Math.round(opacity * 2.55);    // 2.55 = 255 * 0.01
        opacityPaint.setAlpha(alpha);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        URL tileUrl = getTileUrl(x, y, zoom);

        Tile tile = null;
        ByteArrayOutputStream stream = null;

        try {
            Bitmap image = BitmapFactory.decodeStream(tileUrl.openConnection().getInputStream());
            image = adjustOpacity(image);

            stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);

            byte[] byteArray = stream.toByteArray();

            tile = new Tile(256, 256, byteArray);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return tile;
    }

    /**
     * Helper method that returns the {@link URL} of the tile image for the given x/y/zoom location.
     * <p/>
     * <p>This method assumes the URL string provided in the constructor contains three placeholders for the x-
     * and y-positions as well as the zoom level of the desired tile; <code>{x}</code>, <code>{y}</code>, and
     * <code>{zoom}</code>. An example for an OpenWeatherMap URL would be:
     * http://tile.openweathermap.org/map/precipitation/{zoom}/{x}/{y}.png</p>
     *
     * @param x    The x-position of the tile
     * @param y    The y-position of the tile
     * @param zoom The zoom level of the tile
     * @return The {@link URL} of the desired tile image
     */
    private URL getTileUrl(int x, int y, int zoom) {
        String tileUrl = url
                .replace("{x}", Integer.toString(x))
                .replace("{y}", Integer.toString(y))
                .replace("{zoom}", Integer.toString(zoom));

        try {
            return new URL(tileUrl);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    private Bitmap adjustOpacity(Bitmap bitmap) {
        Bitmap adjustedBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(adjustedBitmap);
        canvas.drawBitmap(bitmap, 0, 0, opacityPaint);

        return adjustedBitmap;
    }
}
