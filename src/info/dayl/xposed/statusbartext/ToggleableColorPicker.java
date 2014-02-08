
package info.dayl.xposed.statusbartext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

public class ToggleableColorPicker extends Preference {
    public static final String TAG = "ToggleableColorPicker";
    public static final String EXPANDED_STATE = "expanded_state";

    // the white color in integer
    public static final int COLOR_WHITE = -1;

    private View mLayoutTitle;
    private View mLayoutPicker;
    private ColorPicker mColorPicker;
    private OpacityBar mOpacityBar;
    private SVBar mSvBars;
    private CheckBox mArrawCheckBox;
    private Button mButtonSave;

    private int mCurrentColor;
    private int mOldColor;

    private boolean mExpanded = false;

    public ToggleableColorPicker(Context context) {
        super(context);
        initView();
    }

    public ToggleableColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public ToggleableColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        Log.i(TAG, "initing view...");
        setLayoutResource(R.layout.toggleable_color_picker);
    }

    @Override
    protected void onBindView(View view) {
        Log.i(TAG, "on binding view");
        mLayoutTitle = view.findViewById(R.id.color_picker_layout_title);
        mLayoutPicker = view.findViewById(R.id.color_picker_layout_picker);
        mColorPicker = (ColorPicker) view.findViewById(R.id.color_picker_picker);
        mOpacityBar = (OpacityBar) view.findViewById(R.id.color_picker_opac);
        mSvBars = (SVBar) view.findViewById(R.id.color_picker_svbar);
        mArrawCheckBox = (CheckBox) view.findViewById(R.id.color_picker_checkeable_image);
        mButtonSave = (Button) view.findViewById(R.id.color_picker_save);

        mColorPicker.addSVBar(mSvBars);
        mColorPicker.addOpacityBar(mOpacityBar);

        // set the expanded state of the color picker
        setExpanded();

        mLayoutTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                final int visibility;

                if (mLayoutPicker.getVisibility() == View.GONE) {
                    visibility = View.VISIBLE;
                    mExpanded = true;
                } else {
                    visibility = View.GONE;
                    mExpanded = false;
                }

                Log.d(TAG, "the new visibility is : " + visibility);
                ObjectAnimator animator = ObjectAnimator.ofFloat(mLayoutPicker, "scaleY", 0f,
                        1f);
                // Log.d(TAG, "the measured height is : " +
                // mLayoutPicker.getMeasuredHeight());
                animator.start();
                mLayoutPicker.setVisibility(visibility);

                mArrawCheckBox.setChecked(mExpanded);

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
            }
        });

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when user click the save button, let's save it
                Log.d(TAG, "persist color");
                mOldColor = mColorPicker.getColor();
                // don't forget to call the listener
                // if the listener return false, don't persist the color
                if (callChangeListener(mOldColor)) {
                    persistInt(mOldColor);
                }
            }
        });

        mCurrentColor = mOldColor;
        mColorPicker.setOldCenterColor(mOldColor);
        mColorPicker.setColor(mCurrentColor);

        super.onBindView(view);
    }

    /**
     * get the expanded value of the color picker
     * 
     * @return
     */
    public boolean isExpanded() {
        return mExpanded;
    }

    /**
     * set the expanded state to the new value
     * 
     * @param expanded
     */
    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
        if (mLayoutPicker != null) {
            setExpanded();
        } else {
            Log.d(TAG, "the layout picker is null now, let's igonre this request");
        }
    }

    private void setExpanded() {
        if (mExpanded) {
            mLayoutPicker.setVisibility(View.VISIBLE);
        } else {
            mLayoutPicker.setVisibility(View.GONE);
        }

        mArrawCheckBox.setChecked(mExpanded);
    }

    @Override
    protected void onClick() {
        // TODO Auto-generated method stub
        super.onClick();
        // at last, use the persistInt or something to save the value.
        // EDIT: no nothing now
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // TODO Auto-generated method stub
        if (restorePersistedValue) {
            // here, we restore the default value
            mOldColor = this.getPersistedInt(COLOR_WHITE);
        } else {
            // here, we should set it to the default value
            mOldColor = (Integer) defaultValue;
            persistInt(mOldColor);
        }

        super.onSetInitialValue(restorePersistedValue, defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // TODO Auto-generated method stub
        return COLOR_WHITE;
        // return super.onGetDefaultValue(a, index);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent, use
            // superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current
        // setting value
        // myState.value = mNewValue;

        // save the color of current color picker
        myState.oldColor = mColorPicker.getOldCenterColor();
        myState.currentColor = mColorPicker.getColor();
        myState.expanded = new boolean[] {
            mExpanded
        };

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        // mNumberPicker.setValue(myState.value);

        // restored color
        mColorPicker.setOldCenterColor(myState.oldColor);
        mColorPicker.setColor(myState.currentColor);
        mExpanded = myState.expanded[0];
        setExpanded();
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        int oldColor;
        int currentColor;
        boolean[] expanded = new boolean[1];

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            oldColor = source.readInt(); // Change this to read the appropriate
                                         // data type
            currentColor = source.readInt();
            source.readBooleanArray(expanded);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(oldColor); // Change this to write the appropriate
                                     // data
                                     // type
            dest.writeInt(currentColor);
            dest.writeBooleanArray(expanded);
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
