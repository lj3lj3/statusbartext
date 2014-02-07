
package info.dayl.xposed.statusbartext.widget;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Message;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ActionProvider.VisibilityListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.dayl.xposed.statusbartext.XposedInit;
import info.dayl.xposed.statusbartext.activities.SettingsActivity;

@SuppressLint("HandlerLeak")
public class MyText extends TextView implements OnSharedPreferenceChangeListener {
    private static final String TAG = "MyText";
    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 1;
    // final public static String INTENT_ACTION_UPDATE = "cputemp_update_timer";
    final private Context mContext;
    final public static String PREF_KEY = "statusbartext_preferences";
    // private PendingIntent pi = null;
    // private File tempFile = null;
    private LinearLayout mContainer;
    private LinearLayout mSystemIconArea;
    private String text;

    public MyText(Context context) {
        this(context, null);
    }

    public MyText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d("DayL", "construct the text");
        mContext = context;
        // init
        // initTempFile();
        // try {
        // Utils.log("tempFile="+tempFile==null?"null":tempFile.getPath());
        // } catch (Exception e) {
        // Utils.log(Log.getStackTraceString(e));
        // }
        text = mContext.getSharedPreferences(PREF_KEY, 0).getString(SettingsActivity.KEY_DEFAULT_TEXT, "DayL");
        Log.d("DayL", "read the text :" + text);
        setText(text);
        // style
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        setTextColor(context.getResources().getColor(
                android.R.color.holo_blue_light));
        setSingleLine(true);
        setPadding(6, 0, 0, 0);
        setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
    }
    
    public void setLayout (int direction, LinearLayout layout){
        if(direction == DIRECTION_LEFT){
            mContainer = layout;
        } else if (direction == DIRECTION_RIGHT){
            mSystemIconArea = layout;
        }
    }

    // private void initTempFile() {
    // String temperature_file = mContext.getSharedPreferences(PREF_KEY,
    // 0).getString("temperature_file", null);
    // tempFile = Utils.getTempFile(mContext, temperature_file);
    // }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("DayL", "attacting to the window");
        IntentFilter filter = new IntentFilter();
        // filter.addAction(INTENT_ACTION_UPDATE);
        filter.addAction(SettingsActivity.ACTION_SETTINGS_UPDATE);
        // filter.addAction(Intent.ACTION_SCREEN_OFF);
        // filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mBroadcastReceiver, filter);
        mContext.getSharedPreferences(PREF_KEY, 0).registerOnSharedPreferenceChangeListener(this);

        // start update interval
        // int updateInterval = mContext.getSharedPreferences(PREF_KEY,
        // 0).getInt("update_interval", 1000);
        // setAlarm(updateInterval);

        // set text color
        TextView mClock = XposedInit.getClock();
        if (mClock != null) {
            setTextColor(mClock.getCurrentTextColor());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d("DayL", "detacched from the window");
        mContext.unregisterReceiver(mBroadcastReceiver);
        mContext.getSharedPreferences(PREF_KEY, 0).unregisterOnSharedPreferenceChangeListener(this);
        // cancelAlarm();
        super.onDetachedFromWindow();
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        // private boolean isScreenOn = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            // if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // isScreenOn = true;
            // }
            // else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // isScreenOn = false;
            // }
            // else if(intent.getAction().equals(INTENT_ACTION_UPDATE) &&
            // isScreenOn) {
            // updateTempuency();
            // }
            Log.e("DayL", "receive a broadcast, intent : " + intent);
            if (intent.getAction().equals(SettingsActivity.ACTION_SETTINGS_UPDATE)) {
                if (mContext != null) {
                    Log.e("DayL", "the mContext is ok, let's do with the broadcast");
                    SharedPreferences sp = mContext.getSharedPreferences(PREF_KEY, 0);
                    Editor editor = sp.edit();
                    // if(intent.hasExtra("update_interval")) {
                    // editor.putInt("update_interval",
                    // intent.getIntExtra("update_interval", 1000));
                    // }
                    if (intent.hasExtra(SettingsActivity.KEY_POSITION_WITH_PACK)) {
                        int posi = intent.getIntExtra(SettingsActivity.KEY_POSITION_WITH_PACK, 0); 
                        Log.e("DayL", "the position update : " + posi);
                        editor.putInt(SettingsActivity.KEY_POSITION, posi);
                    } else if (intent.hasExtra(SettingsActivity.KEY_DEFAULT_TEXT_WITH_PACK)) {
                        String text = intent.getStringExtra(SettingsActivity.KEY_DEFAULT_TEXT_WITH_PACK);
                        Log.e("DayL", "the text update : " + text);
                        editor.putString(SettingsActivity.KEY_DEFAULT_TEXT, text);
                    }
                    // if(intent.hasExtra("temperature_file")) {
                    // editor.putString("temperature_file",
                    // intent.getStringExtra("temperature_file"));
                    // }
                    // if(intent.hasExtra("temperature_divider")) {
                    // editor.putInt("temperature_divider",
                    // intent.getIntExtra("temperature_divider", 1));
                    // }
                    editor.commit();
                } else {
                    Log.e("DayL", "the mContent is null, drop this braodcast");
                }
            }
        }
    };

    /*
     * public void setAlarm(int interval) { AlarmManager am = (AlarmManager)
     * mContext .getSystemService(Context.ALARM_SERVICE); Intent intent = new
     * Intent(INTENT_ACTION_UPDATE); pi = PendingIntent.getBroadcast(mContext,
     * 0, intent, 0); am.setRepeating(AlarmManager.RTC,
     * System.currentTimeMillis(), interval, pi); }
     */

    /*
     * public void cancelAlarm() { AlarmManager am = (AlarmManager) mContext
     * .getSystemService(Context.ALARM_SERVICE); if(pi!=null) { am.cancel(pi);
     * pi.cancel(); } }
     */

    /*
     * private void updateTempuency() { try { FileInputStream fis = new
     * FileInputStream(tempFile); StringBuffer sbTemp = new StringBuffer("");
     * byte[] buffer = new byte[1024]; while (fis.read(buffer) != -1) {
     * sbTemp.append(new String(buffer)); } fis.close(); String sTemp =
     * sbTemp.toString().replaceAll("[^0-9]+", ""); float temp =
     * Float.valueOf(sTemp); int divider =
     * mContext.getSharedPreferences(PREF_KEY, 0).getInt("temperature_divider",
     * 1); if(divider!=0) temp = temp/divider; setText((int)temp + "°C"); }
     * catch (Exception e) { e.printStackTrace();
     * Utils.log(Log.getStackTraceString(e)); setText("-"); } }
     */

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals(SettingsActivity.KEY_POSITION)) {
            int position = pref.getInt(SettingsActivity.KEY_POSITION, 0);
            Log.e("DayL", "the position is changed in listener : " + position);
            if (position == SettingsActivity.VALUE_LEFT) {
                mSystemIconArea.removeView(this);
                mContainer.removeView(this);

                mSystemIconArea.addView(this, 0);
                mContainer.setVisibility(View.GONE);
            }
            else if (position == SettingsActivity.VALUE_RIGHT) {
                mSystemIconArea.removeView(this);
                mContainer.removeView(this);

                mSystemIconArea.addView(this);
                mContainer.setVisibility(View.GONE);
            }
            else if (position == SettingsActivity.VALUE_ABSOLUTE_LEFT) {
                mSystemIconArea.removeView(this);
                mContainer.removeView(this);

                mContainer.addView(this);
                mContainer.setVisibility(View.VISIBLE);
            }
        }

        // else if(key.equals("update_interval")) {
        // int updateInterval = pref.getInt("update_interval", 1000);
        // cancelAlarm();
        // setAlarm(updateInterval);
        // }

        // else if(key.equals("temperature_file")) {
        // String temperature_file = pref.getString("temperature_file", null);
        // tempFile = Utils.getTempFile(mContext, temperature_file);
        // }

        else if (key.equals(SettingsActivity.KEY_DEFAULT_TEXT)) {
            String newText = pref.getString(SettingsActivity.KEY_DEFAULT_TEXT, "DayL");
            Log.e("DayL", "the default text is changed in listener : " + newText);
            setText(newText);
        }
    }
}
