package it.meridian.spellbook35.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;


public class ViewAssignSlotChild extends RelativeLayout
{
	protected TextView    textview_spell_name;
	protected TextView    textview_spell_desc;
	protected ImageButton button;
	protected int group_position = -1;
	protected int child_position = -1;
	
	
	public ViewAssignSlotChild(Context context)
	{
		super(context);
		this.init(context, null);
	}
	
	
	public ViewAssignSlotChild(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}
	
	
	public ViewAssignSlotChild(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}
	
	protected void init(Context context, AttributeSet attributeSet)
	{
		LayoutInflater.from(context).inflate(R.layout.view_assign_slot_child, this, true);
		this.textview_spell_name = this.findViewById(R.id.spell_name);
		this.textview_spell_desc = this.findViewById(R.id.spell_desc);
		this.button = this.findViewById(R.id.button);
		this.button.setFocusable(false);
		
//		this.button = new ImageButton(context);
//		{
//			this.button.setId(R.id.button_1);
//
//			final int PADDING = 4;
//			this.button.setPadding(PADDING, PADDING, PADDING, PADDING);
//			this.button.setImageResource(android.R.drawable.ic_input_add);
//			this.button.setBackgroundResource(R.drawable.button_frame);
//			this.button.setFocusable(false);
//
//			this.addView(this.button);
//			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.button.getLayoutParams();
//			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
//			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//			this.button.setLayoutParams(layoutParams);
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
	set_button_click_listener(IButtonClickListener listener)
	{
		this.button.setOnClickListener(button -> listener.on_button_click(this, this.group_position, this.child_position));
	}
	
	
	
	
	public interface IButtonClickListener
	{
		void on_button_click(ViewAssignSlotChild view, int group_position, int child_position);
	}
}
