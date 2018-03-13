package com.example.bindview_api.finder;

import android.content.Context;
import android.view.View;

/**
 * Created by leiyu on 2018/1/31.
 * 支持fragment
 */

public class ViewFinder implements Finder{

    @Override
    public Context getContext(Object source) {
        return ((View)source).getContext();
    }

    @Override
    public View findView(Object source, int id) {
        return ((View)source).findViewById(id);
    }
}
