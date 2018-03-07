package com.example.bindview_api.finder;

import android.content.Context;
import android.view.View;
/**
 * Created by leiyu on 2018/1/31.
 */

public interface Finder {
    Context getContext(Object source);

    View findView(Object source, int id);
}
