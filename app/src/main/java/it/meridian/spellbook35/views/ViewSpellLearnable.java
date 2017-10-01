package it.meridian.spellbook35.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;

public class ViewSpellLearnable extends RelativeLayout
{
	private TextView text_view_spell_name;
	private TextView text_view_spell_desc;
	private CheckBox checkbox;
	private int groupPosition = -1;
	private int childPosition = -1;
	
	
	public ViewSpellLearnable(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	private void init(Context context, AttributeSet attributeSet)
	{
		this.checkbox = new CheckBox(context);
		{
			this.checkbox.setId(R.id.button_1);
			
			final int PADDING = 4;
			this.checkbox.setPadding(PADDING, PADDING, PADDING, PADDING);
			this.checkbox.setBackgroundColor(Color.TRANSPARENT);
//			this.checkbox.setBackgroundResource(R.drawable.button_frame);
			this.checkbox.setFocusable(false);
			
			this.addView(this.checkbox);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.checkbox.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			this.checkbox.setLayoutParams(layoutParams);
		}
		
		this.text_view_spell_name = new TextView(context);
		{
			this.text_view_spell_name.setId(R.id.text_view_1);
			this.text_view_spell_name.setTypeface(null, Typeface.BOLD);
			
			this.addView(this.text_view_spell_name);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.text_view_spell_name.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			layoutParams.addRule(RelativeLayout.START_OF, R.id.button_1);
			this.text_view_spell_name.setLayoutParams(layoutParams);
		}
		
		this.text_view_spell_desc = new TextView(context);
		{
			this.text_view_spell_desc.setId(R.id.text_view_2);
			
			this.addView(this.text_view_spell_desc);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.text_view_spell_desc.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
			layoutParams.addRule(RelativeLayout.BELOW, R.id.text_view_1);
			layoutParams.addRule(RelativeLayout.START_OF, R.id.button_1);
			this.text_view_spell_desc.setLayoutParams(layoutParams);
		}
	}
	
	public void setPosition(int groupPosition, int childPosition)
	{
		this.groupPosition = groupPosition;
		this.childPosition = childPosition;
	}
	
	public void setTextName(String text)
	{
		this.text_view_spell_name.setText(text);
	}
	
	public void setTextDesc(String text)
	{
		this.text_view_spell_desc.setText(text);
	}
	
	public void setCheckboxState(boolean isChecked)
	{
		this.checkbox.setChecked(isChecked);
	}
	
	public void setCheckboxStateChangeListener(ICheckboxStateChangeListener listener)
	{
		CheckBox.OnCheckedChangeListener wrapper =
				(checkbox, isChecked) -> listener.onCheckboxStateChanged(this, this.groupPosition, this.childPosition, isChecked);
		this.checkbox.setOnCheckedChangeListener(wrapper);
	}
	
	
	
	public interface ICheckboxStateChangeListener
	{
		void onCheckboxStateChanged(ViewSpellLearnable view, int groupPosition, int childPosition, boolean isChecked);
	}
}
