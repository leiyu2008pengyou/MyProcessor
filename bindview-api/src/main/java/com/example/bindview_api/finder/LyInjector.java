package com.example.bindview_api.finder;

/**
 * Created by leiyu on 2018/1/31.
 */

public interface LyInjector<T> {

    void lyInject(T host, Object source, Finder finder);
}
