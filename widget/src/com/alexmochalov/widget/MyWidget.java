package com.alexmochalov.widget;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.alexmochalov.widget.R;

import android.app.Notification;
import android.app.NotificationManager;
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
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidget extends AppWidgetProvider {
	final static String pathToFile = Environment.getExternalStorageDirectory().getPath() + "/log.html"; 
	
	private static boolean status = false;

	private static int state = 0;
	
	private static boolean autoTurn = false;
	
	//private static String info = "????";
	
	final static String ACTION_PRESSED = "ru.startandroid.develop.p1201clickwidget.button_pressed";
	final static String ACTION_WIFI_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	final static String ACTION_POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		clearFile(context);
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

	static void updateWidget(Context context, AppWidgetManager appWidgetManager,
			int widgetID) {
		// Кнопка
		RemoteViews widgetView = new RemoteViews(context.getPackageName(),
				R.layout.widget);
		
		// Читаем флажок
		SharedPreferences sp = context.getSharedPreferences(
				ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
		autoTurn = sp.getBoolean(ConfigActivity.WIDGET_AUTO_TURNING
				+ widgetID, false);
		
		Intent buttonIntent = new Intent(context, MyWidget.class);
		buttonIntent.setAction(ACTION_PRESSED);
		buttonIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
		PendingIntent pIntent = PendingIntent.getBroadcast(context, widgetID, buttonIntent, 0);
		widgetView.setOnClickPendingIntent(R.id.imageButton1, pIntent);
		

		writeToFile(context, "updateWidget. state "+state);
		
        if (state % 10 == WifiManager.WIFI_STATE_ENABLED)
        	widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn3);
        else if (state % 10 != WifiManager.WIFI_STATE_DISABLED) 
        	widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn2);
        else			
        	widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn1);
		
        
     	//widgetView.setTextViewText(R.id.widgetTextViewInfo, info);
     	// Конфигурационный экран (TextView)
        /*
        Intent configIntent = new Intent(ctx, ConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pIntentA = PendingIntent.getActivity(ctx, widgetID,
            configIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.widgetTextViewInfo, pIntentA);
     	*/
		// Обновляем виджет
		appWidgetManager.updateAppWidget(widgetID, widgetView);
	}
	
	private void clearFile(Context context) {
		try {
			File file = new File(pathToFile);
			
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writer.write("");
			writer.close();
	    }
	    catch (IOException e) {
	        Log.e("Exception", "File write failed: " + e.toString());
	    } 
	}
	
	private static void writeToFile(Context context, String string) {
		try {
			File file = new File(pathToFile);
			
			Writer writer = new BufferedWriter(new FileWriter(file, true));
			
			string = DateFormat.getDateTimeInstance().format(System.currentTimeMillis()) + " " + string;
			string = string+" <br>\n";
			writer.append(string);
			writer.close();
	    }
	    catch (IOException e) {
	        Log.e("Exception", "File write failed: " + e.toString());
	    } 
	}
	
	
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		if (intent.getAction().equalsIgnoreCase(ACTION_PRESSED)) {
			// Нажата ImageButton
			// Переключаем состояние wifi hotspot
			ApManager.configApState(context);
			writeToFile(context, "onReceive PRESSED ");
			
		} else if (intent.getAction().equalsIgnoreCase(ACTION_POWER_CONNECTED)){
			// Подключено зарядное устройство
			writeToFile(context, "onReceive POWER CONNECTED. Auto: "+autoTurn);
			if (autoTurn)
				// Включаем wifi hotspot 
				ApManager.configApState(context, true);
		} else if (intent.getAction().equalsIgnoreCase(ACTION_WIFI_STATE_CHANGED)){
            state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            writeToFile(context, "onReceive WIFI STATE CHANGED. state"+state);
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			
		    final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, 
		    		this.getClass().getName()));
		    
		    for (int i : appWidgetIds) {
				updateWidget(context, appWidgetManager, i);
			}		    
			
		}
	}

}
