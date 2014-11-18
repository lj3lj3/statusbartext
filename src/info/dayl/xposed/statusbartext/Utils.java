package info.dayl.xposed.statusbartext;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class Utils {
    private static final String TAG = "Utils";
    private static final String NAME_SYSTEM_UI = "com.android.systemui";
    
    public static void killSystemUi (){
        killPackage(NAME_SYSTEM_UI);
    }

    public static void killPackage(String packageToKill) {
        Process su = null;
        
        // get superuser
        Log.d("DayL", "trying to get the superuser");
        try {
            su = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
            Log.w("DayL", "failed to get the superuser");
        }
        
        // kill given package
        if (su != null ){
            Log.d("DayL", "now, i am the super user!");
            Log.d("DayL", "killing the package : " + packageToKill);
            try {
                DataOutputStream os = new DataOutputStream(su.getOutputStream()); 
                os.writeBytes("pkill " + packageToKill + "\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();
                su.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }  
}
