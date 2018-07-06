package it.meridian.spellbook35.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;


public class ViewScrollGroup extends RelativeLayout
{
	public TextView textview_type;
	public TextView textview_level;
	public TextView textview_count;
	
	
	public ViewScrollGroup(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	
	public ViewScrollGroup(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}
	
	
	public ViewScrollGroup(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}
	
	
	protected
	void
	init(Context context, AttributeSet attrs)
	{
		LayoutInflater.from(context).inflate(R.layout.view_scroll_group, this, true);
		this.textview_type  = this.findViewById(R.id.scroll_type);
		this.textview_level = this.findViewById(R.id.scroll_level);
		this.textview_count = this.findViewById(R.id.scroll_count);
	}
	
	
	@SuppressLint("SetTextI18n")
	public
	void
	set_content(String scroll_type, int scroll_caster_level, int scroll_count)
	{
		this.textview_type.setText(scroll_type);
		this.textview_level.setText(scroll_caster_level != 0 ? ("CL " + scroll_caster_level) : "");
		this.textview_count.setText(Integer.toString(scroll_count));
	}
}
