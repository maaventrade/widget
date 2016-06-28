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
	
	private static boolean pressed = false;

	private static int state = -1;
	private static int statePrev = -1;
	
	private static boolean autoTurn = false;
	
	//private static String info = "????";
	
	final static String ACTION_PRESSED = "ru.startandroid.develop.p1201clickwidget.button_pressed";
	final static String ACTION_WIFI_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	final static String ACTION_POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		clearFile(context);

		if  (ApManager.isApOn(context))
			state = WifiManager.WIFI_STATE_ENABLED;
		else
			state = WifiManager.WIFI_STATE_DISABLED;
		
        updateAllWigets(context);
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
		if (statePrev != state){
			pressed = false;
	  		statePrev = state;
		}

		if (pressed){
			switch (state % 10){
			case WifiManager.WIFI_STATE_DISABLING:
   				widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn21);
   				writeToFile(context, "set21");
				break;
			case WifiManager.WIFI_STATE_DISABLED:
 				widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn11);
   				writeToFile(context, "set11");
				break;
			case WifiManager.WIFI_STATE_ENABLING:
   				widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn21);
   				writeToFile(context, "set211");
				break;
			case WifiManager.WIFI_STATE_ENABLED:
 			 	widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn31);
   				writeToFile(context, "set31");
				break;
			default:
				break;
			}
		} else {
			switch (state % 10){
			case WifiManager.WIFI_STATE_DISABLING:
   				widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn2);
   				writeToFile(context, "set2");
				break;
			case WifiManager.WIFI_STATE_DISABLED:
 				widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn1);
   				writeToFile(context, "set1");
				break;
			case WifiManager.WIFI_STATE_ENABLING:
   				widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn2);
   				writeToFile(context, "set22");
				break;
			case WifiManager.WIFI_STATE_ENABLED:
 			 	widgetView.setImageViewResource(R.id.imageButton1, R.drawable.btn3);
   				writeToFile(context, "set3");
				break;
			default:
				break;
			}
		}
		
        
     	//widgetView.setTextViewText(R.id.widgetTextViewInfo, info);
     	// Конфигурационный экран (TextView)
        
        Intent configIntent = new Intent(context, ConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pIntentA = PendingIntent.getActivity(context, widgetID,
            configIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.widgetTextViewInfo, pIntentA);
     	
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
	
	
	private void updateAllWigets(Context context){
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		
	    final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, 
	    		this.getClass().getName()));
	    
	    for (int i : appWidgetIds) {
			updateWidget(context, appWidgetManager, i);
		}		    
	}
	
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		if (intent.getAction().equalsIgnoreCase(ACTION_PRESSED)) {
			// Нажата ImageButton
			pressed = true;
            updateAllWigets(context);
            
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
			
            updateAllWigets(context);
		}
	}

}
