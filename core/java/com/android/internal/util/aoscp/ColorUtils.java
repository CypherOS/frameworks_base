/*
* Copyright (C) 2017 Paranoid Android
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.internal.util.aoscp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class ColorUtils {

    public static int getIconColorFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return -3;
        }
        if (drawable.getConstantState() == null) {
            return -3;
        }
        Drawable copyDrawable = drawable.getConstantState().newDrawable();
        if (copyDrawable == null) {
            return -3;
        }
        if (copyDrawable instanceof ColorDrawable) {
            return ((ColorDrawable) drawable).getColor();
        }
        Bitmap bitmap = drawableToBitmap(copyDrawable);
        if (bitmap == null) {
            return -3;
        }
        return getDominantColor(bitmap, true);
    }

    public static int getDominantColor(Bitmap source, boolean applyThreshold) {
        if (source == null) {
            return -3;
        }
        int[] colorBins = new int[36];
        int maxBin = -1;
        float[] sumHue = new float[36];
        float[] sumSat = new float[36];
        float[] sumVal = new float[36];
        float[] hsv = new float[3];

        int height = source.getHeight();
        int width = source.getWidth();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                  int c = pixels[col + row * width];
                  if (Color.alpha(c) < 128) {
                      continue;
                  }
                  Color.colorToHSV(c, hsv);

                  if (applyThreshold && (hsv[1] <= 0.35f || hsv[2] <= 0.35f)) {
                      continue;
                  }

                  int bin = (int) Math.floor(hsv[0] / 10.0f);
                  sumHue[bin] = sumHue[bin] + hsv[0];
                  sumSat[bin] = sumSat[bin] + hsv[1];
                  sumVal[bin] = sumVal[bin] + hsv[2];
                  colorBins[bin]++;
                  if (maxBin < 0 || colorBins[bin] > colorBins[maxBin]) {
                      maxBin = bin;
                  }
             }
        }

        if (maxBin < 0) {
            return -3;
        }
        hsv[0] = sumHue[maxBin]/colorBins[maxBin];
        hsv[1] = sumSat[maxBin]/colorBins[maxBin];
        hsv[2] = sumVal[maxBin]/colorBins[maxBin];
        return Color.HSVToColor(hsv);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = null;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width > 0 && height > 0) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }

    /**
     * Checks whether color is dark or not.
     * Magic numbers are a definition of luminance for digital formats: https://en.wikipedia.org/wiki/Luma_%28video%29
     */
    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }
}
