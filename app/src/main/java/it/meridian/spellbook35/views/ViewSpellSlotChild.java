package it.meridian.spellbook35.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;


public class ViewSpellSlotChild extends RelativeLayout
{
	public TextView textview_name;
	public TextView textview_desc;
	public int color_expended;
	
	
	public ViewSpellSlotChild(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	public ViewSpellSlotChild(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}
	
	public ViewSpellSlotChild(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}
	
	
	protected
	void
	init(Context context, AttributeSet attrs)
	{
		TypedArray attr_array = context.obtainStyledAttributes(attrs, R.styleable.ViewSpellSlotChild);
		if(attr_array != null)
		{
			this.color_expended = attr_array.getColor(R.styleable.ViewSpellSlotChild_backgroundColorExpended, 0x50_80_00_00);
			attr_array.recycle();
		}
		
		LayoutInflater.from(context).inflate(R.layout.view_spell_slot_child, this, true);
		this.textview_name = this.findViewById(R.id.spell_name);
		this.textview_desc = this.findViewById(R.id.spell_desc);
	}
	
	
	public
	void
	set_content(String spell_name, String spell_desc, boolean is_domain, boolean expended)
	{
		if(is_domain)
			spell_name = "[D] " + spell_name;
		this.textview_name.setText(spell_name);
		this.textview_desc.setText(spell_desc);
		this.setBackgroundColor(expended ? this.color_expended : 0); // ARGB
	}
}
