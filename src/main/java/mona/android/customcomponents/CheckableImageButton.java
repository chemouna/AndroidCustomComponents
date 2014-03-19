package mona.android.customcomponents;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 *  An imageview that have a checked state and toggles its state when clicked
 *  and have support for animation to be run when state changes (
 */
public class CheckableImageButton extends ImageButton implements Checkable {

    private static final int DURATION = 300;

    private boolean mChecked;
    private boolean mBroadcasting;
    private int mType;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
    private static final int TYPE_RADIO_BUTTON = 0;
    private static final int TYPE_CHECK_BOX = 1;

    private AnimatorListenerAdapter mAnimationListener = null;
    //optional checked / unchecked : to animate automaticaly
    public CheckableImageButton(Context context) {
        this(context, null);
    }

    public CheckableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckableImageButton);

        mType = a.getInt(R.styleable.CheckableImageButton_type, TYPE_CHECK_BOX);
        boolean animate = a.getBoolean(R.styleable.CheckableImageButton_animate, false);
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.menu_btn_anim);
        //temporarly setting up a default checkedlistener

        setChecked(false);

        a.recycle();
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    public void setAnimationListener(AnimatorListenerAdapter listener){
        mAnimationListener = listener;
    }

    @Override
    public boolean performClick() {
        if (mType == TYPE_RADIO_BUTTON) {
            setChecked(true);
        } else if (mType == TYPE_CHECK_BOX) {
            toggle();
        }
        return super.performClick();
    }

    public boolean isChecked() {
        return mChecked;
    }

    /**
     * <p>
     * Changes the checked state of this button.
     * </p>
     *
     * @param checked
     *            true to check the button, false to uncheck it
     */
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
            // Avoid infinite recursions if setChecked() is called from a listener
            if (mBroadcasting) {
                return;
            }
            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
            }
            mBroadcasting = false;
        }
    }

    /**
     * Register a callback to be invoked when the checked state of this button changes.
     *
     * @param listener
     *            the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    /**
     * Interface definition for a callback.
     */
    public static interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a button has changed.
         *
         * @param button
         *            The button view whose state has changed.
         * @param isChecked
         *            The new checked state of button.
         */
        void onCheckedChanged(CheckableImageButton button, boolean isChecked);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    static class SavedState extends BaseSavedState {
        boolean checked;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            checked = (Boolean) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(checked);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.checked = isChecked();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        setChecked(ss.checked);
        requestLayout();
    }

    //call this to set an animation that runs when a state changes
    public void setCheckDrawableAnimation(final int checkedResId, final int uncheckedResId){
        mOnCheckedChangeListener = new OnCheckedChangeListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onCheckedChanged(CheckableImageButton button, boolean isChecked) {
                if(isChecked){
                    animateIconTo(button, isChecked() ? checkedResId : uncheckedResId, true);
                }
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void animateIconTo(final ImageView imageView,
                                    final int currRestId,
                                    boolean animate) {
        if (hasICS() && imageView.getTag() != null) {
            if (imageView.getTag() instanceof Animator) {
                Animator anim = (Animator) imageView.getTag();
                anim.end();
                imageView.setAlpha(1f);
            }
        }

        animate = animate && hasICS();
        if (animate) {
            int duration = DURATION;
            Animator outAnimator = ObjectAnimator.ofFloat(imageView, View.ALPHA, 0f);
            outAnimator.setDuration(duration / 2);
            outAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animator) {
                    if(mAnimationListener != null){
                        mAnimationListener.onAnimationEnd(animator);
                        if(currRestId > 0){
                            imageView.setImageResource(currRestId);
                        }
                    }
                }
            });

            AnimatorSet inAnimator = new AnimatorSet();
            outAnimator.setDuration(duration);
            inAnimator.playTogether(
                    ObjectAnimator.ofFloat(imageView, View.ALPHA, 1f),
                    ObjectAnimator.ofFloat(imageView, View.SCALE_X, 0f, 1f),
                    ObjectAnimator.ofFloat(imageView, View.SCALE_Y, 0f, 1f)
            );

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(outAnimator, inAnimator);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    imageView.setTag(null);
                }
            });
            imageView.setTag(set);
            set.start();
        }
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

}