package com.uestc.jenkin.imageeditor;

import android.app.Application;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/05/23
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class App extends Application {

    private static App app;


    @Override
    public void onCreate() {
        super.onCreate();

        app = this;
    }

    public static App getInstance()
    {
        return app;
    }
}
