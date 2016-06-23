package com.alexmochalov.widget;

import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;

import com.alexmochalov.widget.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidget extends AppWidgetProvider {
	private static boolean status = false;

	private static int state = 0;
	
	private static boolean autoTurn = false;
	
	final static String ACTION_PRESSED = "ru.startandroid.develop.p1201clickwidget.button_pressed";
	final static String ACTION_WIFI_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	final static String ACTION_POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.d("W","WIDGET onUpdate");
		
		// обновляем все экземпляры
		for (int i : appWidgetIds) {
			updateWidget(context, appWidgetManager, i);
		}
	}

	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		// Удаляем Preferences
		Editor editor = context.getSharedPreferences(
				ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
		for (int widgetID : appWidgetIds) {
			editor.remove(ConfigActivity.WIDGET_AUTO_TURNING + widgetID);
		}
		editor.commit();
	}

	static void updateWidget(Context ctx, AppWidgetManager appWidgetManager,
			int widgetID) {
		SharedPreferences sp = ctx.getSharedPreferences(
				ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);

		// Читаем флажок
		autoTurn = sp.getBoolean(ConfigActivity.WIDGET_AUTO_TURNING
				+ widgetID, false);
		
		if (autoTurn){
			///
		}

		// Кнопка
		RemoteViews widgetView = new RemoteViews(ctx.getPackageName(),
				R.layout.widget);
		
		Intent buttonIntent = new Intent(ctx, MyWidget.class);
		buttonIntent.setAction(ACTION_PRESSED);
		buttonIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
		PendingIntent pIntent = PendingIntent.getBroadcast(ctx, widgetID, buttonIntent, 0);
		widgetView.setOnClickPendingIntent(R.id.imageButton1, pIntent);
		
        if (state % 10 == WifiManager.WIFI_STATE_ENABLED)
        	widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn3);
        else if (state % 10 == WifiManager.WIFI_STATE_DISABLED) 
        	widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn1);
        else			
        	widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn2);
		
		// Обновляем виджет
		appWidgetManager.updateAppWidget(widgetID, widgetView);
	}
	
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		Log.d("W","onReceive "+intent.getAction());
		
		if (intent.getAction().equalsIgnoreCase(ACTION_PRESSED)) {
			// Нажата ImageButton
			// Переключаем состояние wifi hotspot
			ApManager.configApState(context);
		} else if (intent.getAction().equalsIgnoreCase(ACTION_POWER_CONNECTED)){
			// Подключено зарядное устройство
			if (autoTurn)
				// Включаем wifi hotspot 
				ApManager.configApState(context, true);
		} else if (intent.getAction().equalsIgnoreCase(ACTION_WIFI_STATE_CHANGED)){
            state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
			Log.d("W","state "+state);
			
			//status = (WifiManager.WIFI_STATE_ENABLED == state % 10);
			//Log.d("W","status "+status);
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			
		    final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, 
		    		this.getClass().getName()));
		    
		    for (int i : appWidgetIds) {
				updateWidget(context, appWidgetManager, i);
			}		    
			
		}
	}

}