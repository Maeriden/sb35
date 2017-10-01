package it.meridian.spellbook35;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import it.meridian.spellbook35.utils.AdapterList;
import it.meridian.spellbook35.utils.Utils;

public class FragmentSpellSourcesDomains extends Fragment
{
	static private final String QUERY =
			"  SELECT domain.id   AS id,  " +
			"         domain.name AS name " +
			"    FROM source_domain domain " +
			"ORDER BY domain.name";
	
	
	private AdapterList adapter;
	private FragmentSpellSource frag_spell_source;
	
	
	
	public FragmentSpellSourcesDomains()
	{
		this.frag_spell_source = new FragmentSpellSource();
		this.adapter = new AdapterList(this::getListItemId,
		                               this::getListItemView,
		                               null);
	}
	
	
	public void setChooserTargetFragment(Fragment target)
	{
		this.frag_spell_source.setTargetFragment(target, 0);
	}
	
	
	@SuppressWarnings("ConstantConditions")
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		Cursor cursor = Application.query(QUERY);
		this.adapter.swapCursor(cursor);
	}
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ListView list_view = new ListView(this.getContext());
		list_view.setOnItemClickListener(this::onClickListItem);
		list_view.setAdapter(this.adapter);
		return list_view;
	}
	
	
	private void onClickListItem(AdapterView<?> parent, View view, int position, long id)
	{
		if(this.frag_spell_source.getTargetFragment() == null)
			return;
		
		Cursor cursor = (Cursor)this.adapter.getItem(position);
		String source_name = Utils.CursorGetString(cursor, "name");
		
		Bundle args = new Bundle(1);
		args.putString(FragmentSpellSource.ARG_KEY_SOURCE, source_name);
		this.frag_spell_source.setArguments(args);
		
		this.getParentFragment().getFragmentManager().beginTransaction()
				.replace(R.id.activity_main_content, this.frag_spell_source)
				.addToBackStack(null)
				.commit();
	}
	
	
	
	private long getListItemId(Cursor cursor)
	{
		int id = Utils.CursorGetInt(cursor, "id");
		return id;
	}
	
	private View getListItemView(Cursor cursor, View convertView, ViewGroup parent)
	{
		TextView view = (TextView) convertView;
		if(view == null)
		{
			view = new TextView(this.getContext());
			view.setMinHeight(60);
			view.setTextSize(14f);
			view.setGravity(Gravity.CENTER_VERTICAL);
		}
		String name = Utils.CursorGetString(cursor, "name");
		view.setText(name);
		return view;
	}
}
