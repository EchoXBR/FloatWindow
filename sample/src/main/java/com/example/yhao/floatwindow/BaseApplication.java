package com.example.yhao.floatwindow;

import android.app.Application;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhao.fixedfloatwindow.R;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.MoveType;
import com.yhao.floatwindow.PermissionListener;
import com.yhao.floatwindow.Screen;
import com.yhao.floatwindow.ViewStateListener;
import com.yhao.floatwindow.menu.FloatMenu;
import com.yhao.floatwindow.menu.MenuItem;
import com.yhao.floatwindow.utils.BackGroudSeletor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yhao on 2017/12/18.
 * https://github.com/yhaolpz
 */

public class BaseApplication extends Application {


    private static final String TAG = "FloatWindow";
    private PermissionListener mPermissionListener = new PermissionListener() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "onSuccess");
        }

        @Override
        public void onFail() {
            Log.d(TAG, "onFail");
        }
    };
    private ViewStateListener mViewStateListener = new ViewStateListener() {
        @Override
        public void onPositionUpdate(int x, int y) {
            Log.d(TAG, "onPositionUpdate: x=" + x + " y=" + y);
        }

        @Override
        public void onShow() {
            Log.d(TAG, "onShow");
        }

        @Override
        public void onHide() {
            Log.d(TAG, "onHide");
        }

        @Override
        public void onDismiss() {
            Log.d(TAG, "onDismiss");
        }

        @Override
        public void onMoveAnimStart() {
            Log.d(TAG, "onMoveAnimStart");
        }

        @Override
        public void onMoveAnimEnd() {
            Log.d(TAG, "onMoveAnimEnd");
        }

        @Override
        public void onBackToDesktop() {
            Log.d(TAG, "onBackToDesktop");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setImageResource(R.drawable.icon);
        List<MenuItem> menus = new ArrayList<>();
        MenuItem personItem = new MenuItem(BackGroudSeletor.getdrawble("ic_weixin", this)) {
            @Override
            public void action() {
                Toast.makeText(BaseApplication.this, "打开微信", Toast.LENGTH_SHORT).show();
//                toast("打开微信");
//                mFloatballManager.closeMenu();
            }
        };

        menus.add(personItem);
        menus.add(personItem);
        menus.add(personItem);
        menus.add(personItem);
        menus.add(personItem);
        menus.add(personItem);
        FloatWindow
                .with(getApplicationContext())
                .setView(imageView)
                .setWidth(Screen.width, 0.15f) //设置悬浮控件宽高
                .setHeight(Screen.width, 0.15f)
                .setX(Screen.width, 0.8f)
                .setY(Screen.height, 0.3f)
                .setMoveType(MoveType.slide, 0, -100)
                .setMoveStyle(500, new BounceInterpolator())
                .setFilter(true, A_Activity.class, C_Activity.class)
                .setViewStateListener(mViewStateListener)
                .setPermissionListener(mPermissionListener)
                .setDesktopShow(true)
                .setMenu(menus)
                .build();


//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(BaseApplication.this, "onClick", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}
