package it.meridian.spellbook35.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ViewSpellSlotGroup extends RelativeLayout
{
	// remaining/available
	static public final String COUNT_FORMAT = "%d/%d";
	
	private TextView text_view_level;
	private TextView text_view_count;
	
	
	
	public ViewSpellSlotGroup(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	public ViewSpellSlotGroup(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}
	
	public ViewSpellSlotGroup(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attributeSet)
	{
//		this.setFocusable(false);
		
		final int PADDING = 4;
		this.setPaddingRelative(PADDING, PADDING, PADDING, PADDING);
		
		final float TEXT_SIZE = 24f;
		this.text_view_level = new TextView(context);
		{
//			this.text_view_level.setFocusable(false);
			this.text_view_level.setTextSize(TEXT_SIZE);
			
			this.addView(this.text_view_level);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.text_view_level.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
			this.text_view_level.setLayoutParams(layoutParams);
		}
		
		this.text_view_count = new TextView(context);
		{
//			this.text_view_count.setFocusable(false);
			this.text_view_count.setTextSize(TEXT_SIZE);
			
			this.addView(this.text_view_count);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.text_view_count.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
			this.text_view_count.setLayoutParams(layoutParams);
		}
		
	}
	
	
	
	public void setLevelText(String text)
	{
		this.text_view_level.setText(text);
	}
	
	public void setCountText(String text)
	{
		this.text_view_count.setText(text);
	}
}
