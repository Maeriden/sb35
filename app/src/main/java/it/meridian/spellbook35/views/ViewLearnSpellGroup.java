package it.meridian.spellbook35.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;


public class ViewLearnSpellGroup extends RelativeLayout
{
	public TextView textview_source;
	public TextView textview_level;
	public TextView textview_amount;
	public CheckBox checkbox;
	protected int group_position = -1;
	
	
	public ViewLearnSpellGroup(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	
	public ViewLearnSpellGroup(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}
	
	
	public ViewLearnSpellGroup(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}
	
	
	protected
	void
	init(Context context, AttributeSet attrs)
	{
		LayoutInflater.from(context).inflate(R.layout.view_learn_spell_group, this, true);
		this.textview_source = this.findViewById(R.id.source);
		this.textview_level  = this.findViewById(R.id.level);
		this.textview_amount = this.findViewById(R.id.amount);
		this.checkbox = this.findViewById(R.id.checkbox);
		this.checkbox.setFocusable(false);
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
	
	
	public
	void
	set_position(int group_position)
	{
		this.group_position = group_position;
	}
	
	
	public
	void
	set_checked(boolean is_checked)
	{
		this.checkbox.setChecked(is_checked);
	}
	
	
	public
	void
	set_check_state_change_listener(ICheckStateChangeListener listener)
	{
		this.checkbox.setOnCheckedChangeListener( (checkbox, isChecked) ->
			listener.on_check_state_changed(this, this.group_position, isChecked)
		);
	}
	
	
	
	
	public interface ICheckStateChangeListener
	{
		void on_check_state_changed(ViewLearnSpellGroup view, int group_position, boolean is_checked);
	}
}
