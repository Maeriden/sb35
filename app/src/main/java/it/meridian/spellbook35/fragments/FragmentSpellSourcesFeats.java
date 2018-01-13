package it.meridian.spellbook35.fragments;


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

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.utils.AdapterList;
import it.meridian.spellbook35.utils.Utils;

public class FragmentSpellSourcesFeats extends it.meridian.spellbook35.Fragment
{
	static private final String ARG_CHARACTER_NAME = "character_name";
	
	static private final String QUERY =
			"  SELECT source.id   AS id,                      " +
			"         source.name AS name                     " +
			"    FROM source_enabled source, source_feat feat " +
			"   WHERE source.name = feat.name                 " +
			"ORDER BY source.name";
	
	
	static public
	Fragment
	newInstance()
	{
		Fragment fragment = new FragmentSpellSourcesFeats();
		return fragment;
	}
	
	
	private AdapterList adapter;
	
	
	@SuppressWarnings("ConstantConditions")
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		Cursor cursor = Application.query(QUERY);
		this.adapter = new AdapterList(this::getListItemId,
		                               this::getListItemView,
		                               cursor);
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
		Cursor cursor       = (Cursor)this.adapter.getItem(position);
		String source_name  = Utils.CursorGetString(cursor, "name");
		
		ISpellSourceChoiceListener parent_fragment = (ISpellSourceChoiceListener)this.getParentFragment();
		parent_fragment.on_source_selected(source_name);
//		Bundle args = new Bundle(1);
//		args.putString(FragmentSpellSource.ARG_SOURCE_NAME, source_name);
//		this.frag_spell_source.setArguments(args);
//
//		this.getParentFragment().getFragmentManager().beginTransaction()
//				.replace(R.id.activity_main_content, this.frag_spell_source)
//				.addToBackStack(null)
//				.commit();
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
			view.setMinHeight(FragmentSpellSources.LIST_ITEM_HEIGHT);
			view.setTextSize(FragmentSpellSources.LIST_ITEM_TEXT_HEIGHT);
			view.setGravity(Gravity.CENTER_VERTICAL);
		}
		String name = Utils.CursorGetString(cursor, "name");
		view.setText(name);
		return view;
	}
}
