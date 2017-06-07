package it.meridian.spellbook35.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;


public class ViewSpellKnownGroup extends RelativeLayout
{
	private TextView text_view_source;
	private TextView text_view_level;
	private TextView text_view_amount;
	
	public ViewSpellKnownGroup(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	private void init(Context context, AttributeSet attrs)
	{
		final float TEXT_SIZE = 22f;
		
		this.text_view_source = new TextView(context);
		{
			this.text_view_source.setId(R.id.text_view_1);
			this.text_view_source.setTextSize(TEXT_SIZE);
			
			this.addView(this.text_view_source);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.text_view_source.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
			layoutParams.setMarginEnd(8);
			this.text_view_source.setLayoutParams(layoutParams);
		}
		
		this.text_view_level = new TextView(context);
		{
			this.text_view_level.setId(R.id.text_view_2);
			this.text_view_level.setTextSize(TEXT_SIZE);
			
			this.addView(this.text_view_level);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.text_view_level.getLayoutParams();
			layoutParams.addRule(RelativeLayout.END_OF, this.text_view_source.getId());
			this.text_view_level.setLayoutParams(layoutParams);
		}
		
		this.text_view_amount = new TextView(context);
		{
			this.text_view_amount.setId(R.id.text_view_3);
			this.text_view_amount.setTextSize(TEXT_SIZE);
			
			this.addView(this.text_view_amount);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.text_view_amount.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
			this.text_view_amount.setLayoutParams(layoutParams);
		}
	}
	
	
	public void setTextSource(String text)
	{
		this.text_view_source.setText(text);
	}
	
	public void setTextLevel(String text)
	{
		this.text_view_level.setText(text);
	}
	
	public void setTextAmount(String text)
	{
		this.text_view_amount.setText(text);
	}
}
