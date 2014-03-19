package mona.android.customcomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/*
* A custom button with a switchable state : .ex state on and off and with the bossibility to
* set a background and a text for each state
*/
public class SwitchableButton extends Button {

	private Context mContext;
	private int mOnTextColor;
	private int mOffTextColor;
	
	private Drawable mOnBackground;
	private Drawable mOffBackground;
	
	private String mOnText;
	private String mOffText;
	
	private boolean mChecked;
	private OnClickListener mToggleListener = null;

	CompositeOnClickListener mGroupListener;
	
	public SwitchableButton(Context context) {
		this(context, (AttributeSet) null);
	}

	public SwitchableButton(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public SwitchableButton(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		mContext = context;
		
		 // Attribute initialization
        final TypedArray a = context.obtainStyledAttributes(attrs, 
        		R.styleable.TextAndColorSwitchableButton,
                defStyle, 0);

        mOnTextColor = a.getInt(R.styleable.TextAndColorSwitchableButton_onTextColor,
        					Color.WHITE);
        mOffTextColor = a.getInt(R.styleable.TextAndColorSwitchableButton_offTextColor,
				Color.BLACK);
        
        mOnBackground = a.getDrawable(R.styleable.TextAndColorSwitchableButton_onBackground);
        if(mOnBackground == null){
        	mOnBackground = getResources().getDrawable(android.R.drawable.btn_default);
        }
        
        mOffBackground = a.getDrawable(R.styleable.TextAndColorSwitchableButton_offBackground);
        if(mOffBackground == null){
        	mOffBackground = getResources().getDrawable(android.R.drawable.btn_default);
        }
        mOnText = a.getString(R.styleable.TextAndColorSwitchableButton_onText);
        mOffText = a.getString(R.styleable.TextAndColorSwitchableButton_offText);
        a.recycle();
		init();
	}

	private void init(){
		mGroupListener = new CompositeOnClickListener();
		setOnClickListener(mGroupListener);
		
		mGroupListener.addOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				setChecked(!isChecked());
			}
		});
		
		setChecked(false);
	}

	//TODO: maybe implements Checkable and override interface's methods
	public void setChecked(boolean checked) {
		mChecked = checked;
	    syncTextState();
	}

	private void syncTextState() {
		boolean checked = isChecked();
		if (! checked && mOnText != null) {
			setText(mOnText);
			setTextColor(mOnTextColor);
			setBackground(mOnBackground);
			//setBackgroundDrawable(mOnBackground);
		} else if (checked && mOffText != null) {
			setText(mOffText);
			setTextColor(mOffTextColor);
			setBackground(mOffBackground);
		}
	}
	    
	public boolean isChecked(){
		return mChecked;
	}
	
	public void addOnClickListener(View.OnClickListener clickListener){
		mGroupListener.addOnClickListener(clickListener);
	}

    public void setToggleListener(View.OnClickListener toggleListener){
        //remove previous one
        if(mToggleListener != null){
            mGroupListener.removeOnClickListener(mToggleListener);
        }
        mToggleListener = toggleListener;
        mGroupListener.addOnClickListener(mToggleListener);
    }

    public class CompositeOnClickListener implements View.OnClickListener {
        List<OnClickListener> listeners;

        public CompositeOnClickListener() {
            listeners = new ArrayList<OnClickListener>();
        }

        public void addOnClickListener(View.OnClickListener listener) {
            listeners.add(listener);
        }

        //TODO: make sure remove(Obj) here works
        public void removeOnClickListener(View.OnClickListener listener) {
            listeners.remove(listener);
        }

        @Override
        public void onClick(View v) {
            for (View.OnClickListener listener : listeners) {
                listener.onClick(v);
            }
        }
    }
}