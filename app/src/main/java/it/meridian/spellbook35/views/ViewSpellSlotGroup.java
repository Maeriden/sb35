package it.meridian.spellbook35.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;


public class ViewSpellSlotGroup extends RelativeLayout
{
	public TextView textview_level;
	public TextView textview_remaining;
	public TextView textview_available;
	
	
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
	
	
	private
	void
	init(Context context, AttributeSet attrs)
	{
		LayoutInflater.from(context).inflate(R.layout.view_spell_slot_group, this, true);
		this.textview_level = this.findViewById(R.id.level);
		this.textview_remaining = this.findViewById(R.id.remaining);
		this.textview_available = this.findViewById(R.id.available);
	}
	
	
	@SuppressLint("SetTextI18n")
	public
	void
	set_content(int level, int remaining, int available)
	{
		this.textview_level.setText(this.getContext().getString(R.string.level_N, level));
		this.textview_remaining.setText(Integer.toString(remaining));
		this.textview_available.setText(Integer.toString(available));
	}
}
