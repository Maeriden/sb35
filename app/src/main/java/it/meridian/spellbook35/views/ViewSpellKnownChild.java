package it.meridian.spellbook35.views;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;


public class ViewSpellKnownChild extends RelativeLayout
{
	public TextView textview_spell_name;
	public TextView textview_spell_info;
	
	public int color_studying;
	public int color_copying;
	
	
	public ViewSpellKnownChild(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	public ViewSpellKnownChild(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}
	
	public ViewSpellKnownChild(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}
	
	
	protected
	void
	init(Context context, AttributeSet attrs)
	{
		TypedArray attr_array = context.obtainStyledAttributes(attrs, R.styleable.ViewSpellKnownChild);
		if(attr_array != null)
		{
			this.color_studying = attr_array.getColor(R.styleable.ViewSpellKnownChild_backgroundColorStudying, 0x50_00_00_00);
			this.color_copying  = attr_array.getColor(R.styleable.ViewSpellKnownChild_backgroundColorCopying, 0x50_A0_A0_00);
			attr_array.recycle();
		}
		
		LayoutInflater.from(context).inflate(R.layout.view_spell_known_child, this, true);
		this.textview_spell_name = this.findViewById(R.id.spell_name);
		this.textview_spell_info = this.findViewById(R.id.spell_info);
	}
	
	
	@SuppressLint("DefaultLocale")
	public
	void
	set_content(String spell_name, int study_remaining_time, int copy_remaining_time)
	{
		this.textview_spell_name.setText(spell_name);
		
		if(study_remaining_time == 0 && copy_remaining_time == 0)
		{
			this.textview_spell_info.setVisibility(View.GONE);
			this.textview_spell_info.setText(null);
			this.setBackgroundColor(0);
		}
		else
		if(study_remaining_time > 0)
		{
			this.textview_spell_info.setVisibility(View.VISIBLE);
			this.textview_spell_info.setText(String.format("%d/8", 8 - study_remaining_time));
			this.setBackgroundColor(this.color_studying);
		}
		else
		if(copy_remaining_time > 0)
		{
			this.textview_spell_info.setVisibility(View.VISIBLE);
			this.textview_spell_info.setText(String.format("%d/24", 24 - copy_remaining_time));
			this.setBackgroundColor(this.color_copying);
		}
	}
}
