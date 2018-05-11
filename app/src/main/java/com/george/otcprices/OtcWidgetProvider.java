package com.george.otcprices;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class OtcWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,String name,String price,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.otc_widget_provider);
        //Click to launch MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.appwidget_textName, pendingIntent);

        if (!name.equals("")) {
            views.setTextViewText(R.id.appwidget_textName, name);
            views.setTextViewText(R.id.appwidget_textPrice, price);
        }else{
            views.setTextViewText(R.id.appwidget_textName, context.getString(R.string.defaultInfo));
            views.setTextViewText(R.id.appwidget_textPrice, context.getString(R.string.defaultInfo));
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        String string = "";
        String string2 = "";
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager,string,string2, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void updateWidgetWithInfo(MainActivity context, AppWidgetManager appWidgetManager, String name, String price, int[] widgetId) {

        for (int appWidgetId : widgetId) {
            updateAppWidget(context, appWidgetManager, name, price, appWidgetId);
        }

    }
}

