/*
 * Copyright (C) 2017 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settingslib;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ColorPickerDialogAdapter extends BaseAdapter {

    private Context mContext;
    private int[] mColors;
    private int mRowHeight;
    private int mSelectedPosition = 0;
    private int mSelectedImageResourceId = 0;
    private Integer mSelectedImageColorFilter;

    public ColorPickerDialogAdapter(Context context, int[] colors) {
        mContext = context;
        mColors = colors;

        final Resources res = context.getResources();
        mRowHeight = res.getInteger(R.integer.color_picker_dialog_row_height);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(
                GridView.LayoutParams.FILL_PARENT, mRowHeight));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            if (mSelectedImageColorFilter != null) {
                imageView.setColorFilter(mSelectedImageColorFilter);
            }
        } else {
            imageView = (ImageView) convertView;

            if (position != mSelectedPosition) {
                imageView.setImageResource(0);
            }
        }

        imageView.setBackgroundColor(mColors[position]);
        if (position == mSelectedPosition) {
            imageView.setImageResource(mSelectedImageResourceId);
        }

        return imageView;
    }

    @Override
    public int getCount() {
        return mColors.length;
    }

    @Override
    public Object getItem(int position) {
        return mColors[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int[] getColors() {
        return mColors;
    }

    public void setColors(int[] colors) {
        mColors = colors;
    }

    public int getRowHeight() {
        return mRowHeight;
    }

    public void setRowHeight(int rowHeight) {
        mRowHeight = rowHeight;
    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setSelectedImageResourceId(int resourceId) {
        mSelectedImageResourceId = resourceId;
    }

    public int getSelectedImageResourceId() {
        return mSelectedImageResourceId;
    }

    public void setSelectedImageColorFilter(int selectedImageColorFilter) {
        mSelectedImageColorFilter = selectedImageColorFilter;
    }

    public Integer getSelectedImageColorFilter() {
        return mSelectedImageColorFilter;
    }

    public int getSelectedColor() {
        return mColors[mSelectedPosition];
    }

    public void setSelectedColor(int color) {
        for(int i = 0; i < mColors.length; i++) {
            if (mColors[i] == color) {
                setSelectedPosition(i);
                return;
            }
        }
    }

}
