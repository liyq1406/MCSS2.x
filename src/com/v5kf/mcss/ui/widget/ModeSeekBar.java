package com.v5kf.mcss.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class ModeSeekBar extends SeekBar {
	
	private OnProgressChangedListener mListener;
	

	public ModeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public ModeSeekBar(Context context) {
		super(context);
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				int mode = progressToMode(progress);
				seekBar.setProgress(modeToProgress(mode));
				if (null != mListener) {
					mListener.onProgressChanged(mode, progress);
				}
			}
		});
	}
	
	/**
     * 进度值转换为mode值, 低疏高密
     * @param progressToMode ModeSeekbarDialog 
     * @return int
     * @param progress
     * @return
     */
    protected int progressToMode(int progress) {
		// TODO Auto-generated method stub
    	int mode = 0;
    	
    	if (progress == 0) {
    		mode = 0;
    	} else if (progress < 6) {
    		mode = 1;
    	} else if (progress < 21) {
    		mode = progress / 3;
    	} else if (progress < 48) {
    		mode = 7 + (progress - 21) / 2;
    	} else if (progress < 79) {
    		mode = 20 + (progress - 48);
    	} else {
    		mode = 50 + (progress - 78) * 5;
    	}
    	
		return mode;
	}
    
    /**
     * mode值转换为对应进度条的位置, 低疏高密
     * @param modeToProgress ModeSeekbarDialog 
     * @return int
     * @param mode
     * @return
     */
    protected int modeToProgress(int mode) {
    	int progress = 0;
    	if (mode <= 0) {
    		progress = 0;
    	} else if (mode == 1) {
    		progress = 3;
    	}else if (mode < 7) {
    		progress = 1 + mode * 3;
    	} else if (mode < 21) {
    		progress = 21 + (mode - 7) * 2;
    	} else if (mode < 51) {
    		progress = mode + 28;
    	} else {
    		progress = (mode - 50) / 5 + 78;
    	}
    	
    	return progress;
    }
    
    public int getSeekBarMode() {
    	return progressToMode(this.getProgress());
    }
    
    public void setSeekBarMode(int mode) {
    	this.setProgress(modeToProgress(mode));
    }
    
    public void setOnProgressChangedListener(OnProgressChangedListener l) {
    	this.mListener = l;
    }

	public interface OnProgressChangedListener {
		public void onProgressChanged(int mode, int progress);
	}
    
}
