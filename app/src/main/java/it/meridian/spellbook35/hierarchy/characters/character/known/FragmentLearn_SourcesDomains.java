package it.meridian.spellbook35.hierarchy.characters.character.known;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.hierarchy.ISourceSelectionListener;
import it.meridian.spellbook35.utils.AdapterList;
import it.meridian.spellbook35.utils.Utils;

public class FragmentLearn_SourcesDomains extends it.meridian.spellbook35.Fragment
{
	static private final String QUERY =
			"  SELECT source.id   AS id,                          " +
			"         source.name AS name                         " +
			"    FROM source_enabled source, source_domain domain " +
			"   WHERE source.name = domain.name                   " +
			"ORDER BY source.name";
	
	
	static public
	Fragment
	newInstance()
	{
		FragmentLearn_SourcesDomains fragment = new FragmentLearn_SourcesDomains();
		return fragment;
	}
	
	
	private AdapterList adapter;
	
	
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
		
		ISourceSelectionListener parent_fragment = (ISourceSelectionListener)this.getParentFragment();
		
		Collection<String> selection = new ArrayList<>(1);
		selection.add(source_name);
		parent_fragment.on_source_selection_accept(selection);
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
			view = (TextView)this.getLayoutInflater().inflate(R.layout.view_sources_item, parent, false);
		}
		String name = Utils.CursorGetString(cursor, "name");
		view.setText(name);
		return view;
	}
}
