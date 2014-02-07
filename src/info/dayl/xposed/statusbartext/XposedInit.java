
package info.dayl.xposed.statusbartext;

import info.dayl.xposed.statusbartext.activities.SettingsActivity;
import info.dayl.xposed.statusbartext.widget.MyText;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedInit implements IXposedHookLoadPackage {
    private static final String TAG = "XposedInit";
    private static TextView tvClock;

    public static TextView getClock() {
        return tvClock;
    }

    public void handleLoadPackage(final LoadPackageParam lpparam)
            throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;

        XposedBridge.hookAllConstructors(XposedHelpers.findClass(
                "com.android.systemui.statusbar.policy.Clock", lpparam.classLoader),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        tvClock = (TextView) param.thisObject;
                    }
                });

        XposedHelpers.findAndHookMethod(
                "com.android.systemui.statusbar.phone.PhoneStatusBar",
                lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        try {
                            Log.d("DayL", "hooked method start");
                            LinearLayout mSystemIconArea = (LinearLayout) XposedHelpers
                                    .getObjectField(param.thisObject, "mSystemIconArea");
                            LinearLayout mStatusBarContents = (LinearLayout) XposedHelpers
                                    .getObjectField(param.thisObject, "mStatusBarContents");
                            Context mContext = (Context) XposedHelpers.getObjectField(
                                    param.thisObject, "mContext");
                            int position = mContext.getSharedPreferences(MyText.PREF_KEY, 0)
                                    .getInt(SettingsActivity.KEY_POSITION, 0);
                            Log.d("DayL", "the position is " + position);
                            MyText myText = new MyText(mContext);

                            LinearLayout container = new LinearLayout(mContext);
                            container.setOrientation(LinearLayout.HORIZONTAL);
                            container.setWeightSum(1);
                            container.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.MATCH_PARENT));
                            container.setVisibility(View.GONE);
                            mStatusBarContents.addView(container, 0);

                            myText.setLayout(MyText.DIRECTION_LEFT, container);
                            myText.setLayout(MyText.DIRECTION_RIGHT, mSystemIconArea);

                            if (position == SettingsActivity.VALUE_LEFT) {
                                mSystemIconArea.addView(myText, 0);
                            } else if (position == SettingsActivity.VALUE_RIGHT) {
                                mSystemIconArea.addView(myText);
                            } else if (position == SettingsActivity.VALUE_ABSOLUTE_LEFT) {
                                container.addView(myText);
                                container.setVisibility(View.VISIBLE);
                            }
                        }
                        catch (Exception e) {
                            // Utils.log(Log.getStackTraceString(e));
                        }
                    }
                }
                );
    }
}
