package com.alexmochalov.widget;

import com.alexmochalov.widget.R;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class ConfigActivity extends Activity {

  public final static String WIDGET_PREF = "widget_pref";
  public final static String WIDGET_AUTO_TURNING = "auto_turning";

  int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
  Intent resultValue;
  SharedPreferences sp;
  CheckBox checkBoxAutoTurn;
  
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // извлекаем ID конфигурируемого виджета
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    // и проверяем его корректность
    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish();
    }

    // формируем intent ответа
    resultValue = new Intent();
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

    // отрицательный ответ
    setResult(RESULT_CANCELED, resultValue);

    setContentView(R.layout.config);
    
    sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
    checkBoxAutoTurn = (CheckBox) findViewById(R.id.checkBoxAutoTurn);
  }
  
  public void onClick(View v){
    sp.edit().putBoolean(WIDGET_AUTO_TURNING + widgetID, checkBoxAutoTurn.isChecked()).commit();
    
    MyWidget.updateWidget(this, AppWidgetManager.getInstance(this), widgetID);
    
    setResult(RESULT_OK, resultValue);
    finish();
  }
}