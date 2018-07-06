package it.meridian.spellbook35.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;


public class ViewSpellKnownGroup extends RelativeLayout
{
	public TextView textview_source;
	public TextView textview_level;
	public TextView textview_amount;
	
	
	public ViewSpellKnownGroup(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	
	public ViewSpellKnownGroup(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}
	
	
	public ViewSpellKnownGroup(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}
	
	
	protected
	void
	init(Context context, AttributeSet attrs)
	{
		LayoutInflater.from(context).inflate(R.layout.view_spell_known_group, this, true);
		this.textview_source = this.findViewById(R.id.source);
		this.textview_level  = this.findViewById(R.id.level);
		this.textview_amount = this.findViewById(R.id.amount);
	}
	
	
	@SuppressLint("SetTextI18n")
	public
	void
	set_content(String source_name, int spell_level, int known_amount)
	{
		this.textview_source.setText(source_name);
		this.textview_level.setText(Integer.toString(spell_level));
		this.textview_amount.setText(Integer.toString(known_amount));
	}
}
