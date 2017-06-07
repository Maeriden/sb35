package it.meridian.spellbook35.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ViewSpellSlot extends LinearLayout
{
	private TextView text_view_name;
	private TextView text_view_desc;
	
	
	public ViewSpellSlot(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	public ViewSpellSlot(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}
	
	public ViewSpellSlot(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attributeSet)
	{
		this.setOrientation(LinearLayout.VERTICAL);
		
		this.text_view_name = new TextView(context);
		this.text_view_name.setTypeface(null, Typeface.BOLD);
		
		this.text_view_desc = new TextView(context);
		
		this.addView(this.text_view_name);
		this.addView(this.text_view_desc);
	}
	
	
	
	public void setNameText(String text)
	{
		this.text_view_name.setText(text);
	}
	
	public void setDescText(String text)
	{
		this.text_view_desc.setText(text);
	}
	
	public boolean isEmpty()
	{
		int spell_name_len = this.text_view_name.getText().length();
		return spell_name_len == 0;
	}
}
