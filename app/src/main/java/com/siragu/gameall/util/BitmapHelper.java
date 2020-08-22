package com.siragu.gameall.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;

/**
 * A util class for processing bitmaps
 */

public class BitmapHelper {

    /**
     * Fixes the orientation of the image
     * @param imageFile
     * @return {@link Bitmap}
     */
    public Bitmap fixOrientation(File imageFile) {
        Bitmap source = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return source;
    }

    /**
     * Center crops the image supplied along with scaling it to supplied width and height
     * @param source Bitmap to be scaled
     * @param newHeight The height of the scaled bitmap
     * @param newWidth The width of the scaled bitmap
     * @return the scaled {@link Bitmap}
     */
    public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {

        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // Te target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);
        source.recycle();

        return dest;
    }
}
