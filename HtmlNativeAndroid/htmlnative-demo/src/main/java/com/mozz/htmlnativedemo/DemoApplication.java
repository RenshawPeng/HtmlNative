package com.mozz.htmlnativedemo;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mozz.htmlnative.HNLog;
import com.mozz.htmlnative.HNativeEngine;
import com.mozz.htmlnative.HrefLinkHandler;
import com.mozz.htmlnative.ImageViewAdapter;
import com.mozz.htmlnative.script.ScriptRunner;
import com.mozz.htmlnative.view.BackgroundViewDelegate;
import com.squareup.leakcanary.LeakCanary;

/**
 * @author Yang Tao, 17/3/1.
 */

public class DemoApplication extends Application {

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        HNativeEngine.getInstance().init(this);
        HNativeEngine.getInstance().debugRenderProcess();

        HNativeEngine.getInstance().setImageViewAdapter(new ImageViewAdapter() {
            @Override
            public void setImage(String src, final BackgroundViewDelegate imageView) {
                Glide.with(DemoApplication.this).load(src).asBitmap().into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap>
                            glideAnimation) {
                        Log.d("GlideTest", "onResourceReady");
                        imageView.setBitmap(resource);
                    }
                });

            }
        });

        HNativeEngine.getInstance().setHrefLinkHandler(new HrefLinkHandler() {
            @Override
            public void onHref(String url, View view) {
                Toast.makeText(DemoApplication.this, url, Toast.LENGTH_SHORT).show();

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        HNativeEngine.registerScriptCallback(new ScriptRunner.OnScriptCallback() {
            @Override
            public void error(final Exception e) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(DemoApplication.this).setMessage("LuaScript " +
                                "Wrong:\n" + e.getMessage()).setTitle("LuaSyntaxError")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {


                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                    }
                });
            }
        });

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        HNativeEngine.getInstance().destroy();
    }
}
