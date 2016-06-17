package widget;

import java.util.Arrays;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidget extends AppWidgetProvider {
	final String LOG_TAG = "myLogs";

	  @Override
	  public void onEnabled(Context context) {
	    super.onEnabled(context);
	    Log.d(LOG_TAG, "onEnabled");
	  }

	  @Override
	  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	      int[] appWidgetIds) {
	    super.onUpdate(context, appWidgetManager, appWidgetIds);
	 // обновляем все экземпляры
	    for (int i : appWidgetIds) {
	      updateWidget(context, appWidgetManager, i);
	    }	  
	  }

	  @Override
	  public void onDeleted(Context context, int[] appWidgetIds) {
	    super.onDeleted(context, appWidgetIds);
	 // Удаляем Preferences
	    /*
	    Editor editor = context.getSharedPreferences(
	        ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
	    for (int widgetID : appWidgetIds) {
	      editor.remove(ConfigActivity.WIDGET_TIME_FORMAT + widgetID);
	      editor.remove(ConfigActivity.WIDGET_COUNT + widgetID);
	    }
	    editor.commit();
	    */
	}

	  @Override
	  public void onDisabled(Context context) {
	    super.onDisabled(context);
	    Log.d(LOG_TAG, "onDisabled");
	  }
	  
	  static void updateWidget(Context ctx, AppWidgetManager appWidgetManager,
		      int widgetID) {
		  /*
		    SharedPreferences sp = ctx.getSharedPreferences(
		        ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);

		    // Читаем формат времени и определяем текущее время
		    String timeFormat = sp.getString(ConfigActivity.WIDGET_TIME_FORMAT
		        + widgetID, null);
		    if (timeFormat == null) return;
		    SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
		    String currentTime = sdf.format(new Date(System.currentTimeMillis()));

		    // Читаем счетчик
		    String count = String.valueOf(sp.getInt(ConfigActivity.WIDGET_COUNT
		        + widgetID, 0));
*/
		    // Помещаем данные в текстовые поля
		    RemoteViews widgetView = new RemoteViews(ctx.getPackageName(),
		        R.layout.widget);
		    widgetView.setTextViewText(R.id.tvTime, currentTime);
		    widgetView.setTextViewText(R.id.tvCount, count);

		    // Конфигурационный экран (первая зона)
		    Intent configIntent = new Intent(ctx, ConfigActivity.class);
		    configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
		    configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
		    PendingIntent pIntent = PendingIntent.getActivity(ctx, widgetID,
		        configIntent, 0);
		    widgetView.setOnClickPendingIntent(R.id.tvPressConfig, pIntent);

		    // Обновление виджета (вторая зона)
		    Intent updateIntent = new Intent(ctx, MyWidget.class);
		    updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		    updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
		        new int[] { widgetID });
		    pIntent = PendingIntent.getBroadcast(ctx, widgetID, updateIntent, 0);
		    widgetView.setOnClickPendingIntent(R.id.tvPressUpdate, pIntent);

		    // Счетчик нажатий (третья зона)
		    Intent countIntent = new Intent(ctx, MyWidget.class);
		    countIntent.setAction(ACTION_CHANGE);
		    countIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
		    pIntent = PendingIntent.getBroadcast(ctx, widgetID, countIntent, 0);
		    widgetView.setOnClickPendingIntent(R.id.tvPressCount, pIntent);

		    // Обновляем виджет
		    appWidgetManager.updateAppWidget(widgetID, widgetView);
		  }

		  public void onReceive(Context context, Intent intent) {
		    super.onReceive(context, intent);
		    // Проверяем, что это intent от нажатия на третью зону
		    if (intent.getAction().equalsIgnoreCase(ACTION_CHANGE)) {

		      // извлекаем ID экземпляра
		      int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		      Bundle extras = intent.getExtras();
		      if (extras != null) {
		        mAppWidgetId = extras.getInt(
		            AppWidgetManager.EXTRA_APPWIDGET_ID,
		            AppWidgetManager.INVALID_APPWIDGET_ID);

		      }
		      if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
		        // Читаем значение счетчика, увеличиваем на 1 и записываем
		        SharedPreferences sp = context.getSharedPreferences(
		            ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
		        int cnt = sp.getInt(ConfigActivity.WIDGET_COUNT + mAppWidgetId,  0);
		        sp.edit().putInt(ConfigActivity.WIDGET_COUNT + mAppWidgetId,
		                ++cnt).commit();

		        // Обновляем виджет
		        updateWidget(context, AppWidgetManager.getInstance(context),
		            mAppWidgetId);
		      }
		    }
		  }	  

}
