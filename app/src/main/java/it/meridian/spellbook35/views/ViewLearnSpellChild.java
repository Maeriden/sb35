package it.meridian.spellbook35.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;

public class ViewLearnSpellChild extends RelativeLayout
{
	public TextView textview_spell_name;
	public TextView textview_spell_desc;
	public CheckBox checkbox;
	protected int group_position = -1;
	protected int child_position = -1;
	
	
	public ViewLearnSpellChild(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	
	public ViewLearnSpellChild(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}
	
	
	public ViewLearnSpellChild(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}
	
	
	protected
	void
	init(Context context, AttributeSet attrs)
	{
		LayoutInflater.from(context).inflate(R.layout.view_learn_spell_child, this, true);
		this.textview_spell_name = this.findViewById(R.id.spell_name);
		this.textview_spell_desc = this.findViewById(R.id.spell_desc);
		this.checkbox = this.findViewById(R.id.checkbox);
		this.checkbox.setFocusable(false);
		
//		this.checkbox = new CheckBox(context);
//		{
//			this.checkbox.setId(R.id.button_1);
//
//			final int PADDING = 4;
//			this.checkbox.setPadding(PADDING, PADDING, PADDING, PADDING);
//			this.checkbox.setBackgroundColor(Color.TRANSPARENT);
////			this.checkbox.setBackgroundResource(R.drawable.button_frame);
//			this.checkbox.setFocusable(false);
//
//			this.addView(this.checkbox);
//			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.checkbox.getLayoutParams();
//			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
//			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//			this.checkbox.setLayoutParams(layoutParams);
//		}
//
//		this.textview_spell_name = new TextView(context);
//		{
//			this.textview_spell_name.setId(R.id.text_view_1);
//			this.textview_spell_name.setTypeface(null, Typeface.BOLD);
//
//			this.addView(this.textview_spell_name);
//			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.textview_spell_name.getLayoutParams();
//			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//			layoutParams.addRule(RelativeLayout.START_OF, R.id.button_1);
//			this.textview_spell_name.setLayoutParams(layoutParams);
//		}
//
//		this.textview_spell_desc = new TextView(context);
//		{
//			this.textview_spell_desc.setId(R.id.text_view_2);
//
//			this.addView(this.textview_spell_desc);
//			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.textview_spell_desc.getLayoutParams();
//			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//			layoutParams.addRule(RelativeLayout.BELOW, R.id.text_view_1);
//			layoutParams.addRule(RelativeLayout.START_OF, R.id.button_1);
//			this.textview_spell_desc.setLayoutParams(layoutParams);
//		}
	}
	
	
	public
	void
	set_content(String spell_name, String spell_desc)
	{
		this.textview_spell_name.setText(spell_name);
		this.textview_spell_desc.setText(spell_desc);
	}
	
	
	public
	void
	set_position(int group_position, int child_position)
	{
		this.group_position = group_position;
		this.child_position = child_position;
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
			listener.on_check_state_changed(this, this.group_position, this.child_position, isChecked)
		);
	}
	
	
	
	
	public interface ICheckStateChangeListener
	{
		void on_check_state_changed(ViewLearnSpellChild view, int group_position, int child_position, boolean is_checked);
	}
}
