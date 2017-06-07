package it.meridian.spellbook35.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.meridian.spellbook35.R;


public class ViewIntPicker extends RelativeLayout implements View.OnClickListener
{
	private int min = Integer.MIN_VALUE;
	private int max = Integer.MAX_VALUE;
	private int val = 0;

	private Button button_decrement;
	private Button button_increment;
	private TextView textview_value;


	static private int clamp(int val, int min, int max)
	{
		val = Math.max(min, Math.min(val, max));
		return val;
	}


	public ViewIntPicker(Context context)
	{
		super(context);
		this.init(context, null);
	}

	public ViewIntPicker(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context, attrs);
	}

	public ViewIntPicker(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs)
	{
		TypedArray attr_array = context.obtainStyledAttributes(attrs, R.styleable.IntPicker);
		{
			this.max = attr_array.getInt(R.styleable.IntPicker_maxValue, this.max);
			this.min = attr_array.getInt(R.styleable.IntPicker_minValue, this.min);
			this.val = attr_array.getInt(R.styleable.IntPicker_value, this.val);

			this.max = Math.max(this.min, this.max);
			this.min = Math.min(this.min, this.max);
			this.val = Math.max(this.min, Math.min(this.val, this.max));
		}
		attr_array.recycle();

		LayoutInflater.from(context).inflate(R.layout.view_int_picker, this, true);
		this.button_decrement = (Button) this.findViewById(R.id.int_picker_dec);
		this.button_increment = (Button) this.findViewById(R.id.int_picker_inc);
		this.textview_value = (TextView) this.findViewById(R.id.int_picker_value);

		this.button_decrement.setOnClickListener(this);
		this.button_increment.setOnClickListener(this);

		this.refresh();
	}

	@Override
	public void onClick(View v)
	{
		if(v == this.button_decrement)
		{
			this.setValue(this.val - 1);
		}
		else
		if(v == this.button_increment)
		{
			this.setValue(this.val + 1);
		}
	}

	public int getValue()
	{
		return this.val;
	}

	public void setValue(int value)
	{
		value = clamp(value, this.min, this.max);
		if(this.val != value)
		{
			this.val = value;
			this.refresh();
		}
	}

	public int getMaxValue()
	{
		return this.max;
	}

	public void setMaxValue(int max)
	{
		if(this.max != max)
		{
			this.max = max;
			this.setValue(this.val);
		}
	}

	public int getMinValue()
	{
		return this.min;
	}

	public void setMinValue(int min)
	{
		if(this.min != min)
		{
			this.min = min;
			this.setValue(this.val);
		}
	}

	public void refresh()
	{
		this.textview_value.setText(Integer.toString(this.val));
		this.button_decrement.setEnabled(this.min < this.val);
		this.button_increment.setEnabled(this.val < this.max);
	}
}
