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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.view.View;
import android.view.Window;

public class ColorPickerDialog extends Dialog {

    private Context mContext;
    private int mColumns;

    private GridView mGridView;
    private ColorPickerDialogAdapter mAdapter;

    private View.OnClickListener mOnCancelListener;
    private View.OnClickListener mOnDefaultListener;
    private View.OnClickListener mOnOkListener;
    private OnColorSelectedListener mOnColorSelectedListener;

    public ColorPickerDialog(Context context) {
        super(context);
        mContext = context;

        final Resources res = context.getResources();
        mColumns = res.getInteger(R.integer.color_picker_dialog_columns);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.color_picker_dialog);

        TypedArray ta = mContext.obtainStyledAttributes(new int[]{
                android.R.attr.colorBackground,
                android.R.attr.colorAccent});
        final int backgroundColor = ta.getColor(0, 0);
        final int accentColor = ta.getColor(1, 0);
        ta.recycle();
        getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));

        final ColorPickerDialogAdapter adapter = getAdapter();

        mGridView = (GridView) findViewById(R.id.grid_view);
        mGridView.setNumColumns(mColumns);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                adapter.setSelectedPosition(position);
                refreshPalette();

                if (mOnColorSelectedListener != null) {
                    mOnColorSelectedListener.onColorSelected(ColorPickerDialog.this,
                        adapter.getSelectedColor());
                }
            }

        });

        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setTextColor(accentColor);
        if (mOnCancelListener != null) {
            cancelButton.setOnClickListener(mOnCancelListener);
        } else {
            cancelButton.setVisibility(View.INVISIBLE);
        }

        Button defaultButton = (Button) findViewById(R.id.button_default);
        defaultButton.setTextColor(accentColor);
        if (mOnDefaultListener != null) {
            defaultButton.setOnClickListener(mOnDefaultListener);
        } else {
            defaultButton.setVisibility(View.INVISIBLE);
        }

        Button okButton = (Button) findViewById(R.id.button_ok);
        okButton.setTextColor(accentColor);
        if (mOnOkListener != null) {
            okButton.setOnClickListener(mOnOkListener);
        }
    }

    public int getColumns() {
        return mColumns;
    }

    public void setColumns(int columns) {
        mColumns = columns;
    }

    public void setOnCancelListener(View.OnClickListener listener) {
        mOnCancelListener = listener;
    }

    public void setOnDefaultListener(View.OnClickListener listener) {
        mOnDefaultListener = listener;
    }

    public void setOnOkListener(View.OnClickListener listener) {
        mOnOkListener = listener;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mOnColorSelectedListener = listener;
    }

    public void setColors(int[] colors, int selectedColor) {
        final ColorPickerDialogAdapter adapter = getAdapter();
        adapter.setColors(colors);
        adapter.setSelectedColor(selectedColor);
        refreshPalette();
    }

    public void setColors(int[] colors) {
        getAdapter().setColors(colors);
    }

    public int getSelectedColor() {
        return getAdapter().getSelectedColor();
    }

    public void setSelectedColor(int selectedColor) {
        getAdapter().setSelectedColor(selectedColor);
        refreshPalette();
    }

    public ColorPickerDialogAdapter getAdapter() {
        if (mAdapter == null) {
            final Resources res = mContext.getResources();

            mAdapter = new ColorPickerDialogAdapter(mContext,
                res.getIntArray(R.array.color_picker_dialog_colors));
        }

        return mAdapter;
    }

    public void setAdapter(ColorPickerDialogAdapter adapter) {
        mAdapter = adapter;
    }

    private void refreshPalette() {
        getAdapter().notifyDataSetChanged();
    }

    public interface OnColorSelectedListener {
        public void onColorSelected(DialogInterface dialog, int color);
    }
}
