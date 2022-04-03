package com.lastproject.used_item_market;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

class RecyclerDecoration extends RecyclerView.ItemDecoration {
    //리사이클러 뷰의 간격을 만드는 클래스이다.

    private final int divWidth;

    public RecyclerDecoration(int divWidth)
    {
        this.divWidth = divWidth;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.right = divWidth;
    }
}


