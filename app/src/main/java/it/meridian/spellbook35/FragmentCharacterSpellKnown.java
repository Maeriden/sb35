package it.meridian.spellbook35;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewSpellKnownGroup;


public class FragmentCharacterSpellKnown extends android.support.v4.app.Fragment implements AdapterExpandableList.ISupplier, FragmentSpellSource.ILearnSpellChoiceListener
{
	static protected final String QUERY_GROUPS =
			"  SELECT known.rowid  AS id,          " + // TODO: Maybe remove rowid
			"         known.source AS source_name, " +
			"         known.level  AS spell_level, " +
			"         COUNT(*)     AS spell_count  " +
			"    FROM character_spell_known known  " +
			"   WHERE character = ?                " +
			"GROUP BY known.source, known.level    " +
			"ORDER BY known.source, known.level";
	
	static protected final String QUERY_CHILDREN =
			"SELECT known.rowid AS id,          " +
			"       known.spell AS spell_name   " +
			"  FROM character_spell_known known " +
			" WHERE known.character = ?         " +
			"   AND known.source = ?            " +
			"   AND known.level = ?";
	
	static private final String DELETE_WHERE =
			"    character = ?   " +
			"AND source = ? " +
			"AND spell = ?";
	
	
	
	protected AdapterExpandableList adapter;
	protected String character;
	
	protected FragmentSpellInfo frag_spell_info;
	protected FragmentSpellSources frag_spell_sources;
	
	
	
	public FragmentCharacterSpellKnown()
	{
		this.frag_spell_info = new FragmentSpellInfo();
		this.frag_spell_sources = new FragmentSpellSources();
		this.adapter = new AdapterExpandableList(this, null);
	}
	
	
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		String character = (String) this.getArguments().get(FragmentCharacter.ARG_KEY_CHARACTER);
		if(!Objects.equals(this.character, character))
		{
			this.character = character;
			this.refresh();
		}
	}
	
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{
		ExpandableListView list_view = new ExpandableListView(this.getContext());
		list_view.setId(R.id.expandable_list_view);
		list_view.setOnChildClickListener(this::onClickExpandableListItem);
		list_view.setOnCreateContextMenuListener(this::onCreateExpandableListContextMenu);
		list_view.setAdapter(this.adapter);
		return list_view;
	}
	
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_characters, menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_action_add:
			{
				this.frag_spell_sources.setChooserTargetFragment(this);
				// NOTE: Needs to use getParentFragment because this fragment's manager is the ViewPager's childManager
				this.getParentFragment().getFragmentManager().beginTransaction()
						.replace(R.id.activity_main_content, this.frag_spell_sources)
						.addToBackStack(null)
						.commit();
			}
			break;
		}
		return true;
	}
	
	
	protected void onCreateExpandableListContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		// TODO
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		
		int view_type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if(view_type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			this.getActivity().getMenuInflater().inflate(R.menu.context_spell_known, menu);
		}
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		if(!this.getUserVisibleHint())
			return super.onContextItemSelected(item);
		
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		
		int view_type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if(view_type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
			Cursor group_cursor = (Cursor) this.adapter.getGroup(groupPosition);
			Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
			
			String source_name = Utils.CursorGetString(group_cursor, "source_name");
			String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
			
			switch(item.getItemId())
			{
				case R.id.menu_action_delete:
				{
					long affected = Application.delete("character_spell_known",
					                                   DELETE_WHERE,
					                                   this.character,
					                                   source_name,
					                                   spell_name);
					if(affected > 0)
						this.refresh();
					return true;
				}
			}
		}
		return false;
	}
	
	
	
	
	protected boolean onClickExpandableListItem(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		Bundle args = new Bundle(1);
		args.putString(FragmentSpellInfo.ARG_KEY_SPELL, spell_name);
		this.frag_spell_info.setArguments(args);
		
		// NOTE: Needs to use getParentFragment because this fragment's manager is the ViewPager's childManager
		this.getParentFragment().getFragmentManager().beginTransaction()
				.replace(R.id.activity_main_content, this.frag_spell_info)
				.addToBackStack(null)
				.commit();
		return true;
	}
	
	
	private void refresh()
	{
		Cursor groups_cursor = Application.query(QUERY_GROUPS,
		                                         this.character);
		this.adapter.swapGroupsCursor(groups_cursor);
	}
	
	
	
	@Override
	public void onLearnSpellChoiceResult(ArrayList<FragmentSpellSource.LearnedSpell> choices)
	{
		long affected = 0;
		
		for(int i = 0; i < choices.size(); ++i)
		{
			FragmentSpellSource.LearnedSpell choice = choices.get(i);
			
			ContentValues values = new ContentValues(4);
			values.put("character", this.character);
			values.put("source", choice.source_name);
			values.put("spell", choice.spell_name);
			values.put("level", choice.spell_level);
			
			affected += Application.insert("character_spell_known", values);
		}
		
		if(affected > 0)
		{
			this.refresh();
		}
	}
	
	@Override
	public void onLearnSpellChoiceCancel()
	{
	
	}
	
	
	
	@Override
	public long getExpandableListGroupId(Cursor group_cursor, int groupPosition)
	{
//		int source_id = Utils.CursorGetInt(group_cursor, "source_id");
//		int spell_level = Utils.CursorGetInt(group_cursor, "level");
//		source_id = source_id & 0xFFFF0000;
//		spell_level = spell_level & 0xFFFF0000;
//		return (source_id << 16) | spell_level;
		
		String source = Utils.CursorGetString(group_cursor, "source_name");
		int level = Utils.CursorGetInt(group_cursor, "spell_level");
		return (source + level).hashCode();
	}
	
	@Override
	public long getExpandableListChildId(Cursor child_cursor, int groupPosition, int childPosition)
	{
		int known_id = Utils.CursorGetInt(child_cursor, "id");
		return known_id;
	}
	
	@Override
	public Cursor getExpandableListChildrenCursor(Cursor group_cursor)
	{
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		int spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		Cursor children_cursor = Application.query(QUERY_CHILDREN,
		                                           this.character,
		                                           source_name,
		                                           Integer.toString(spell_level));
		return children_cursor;
	}
	
	@Override
	public View getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewSpellKnownGroup view = (ViewSpellKnownGroup) convertView;
		if(view == null)
			view = new ViewSpellKnownGroup(this.getContext());
		
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		int spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		int known_amount = Utils.CursorGetInt(group_cursor, "spell_count");
		
		view.setTextSource(source_name);
		view.setTextLevel(Integer.toString(spell_level));
		view.setTextAmount(Integer.toString(known_amount));
		
		return view;
	}
	
	@Override
	public View getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		TextView view = (TextView) convertView;
		if(view == null)
		{
			view = new TextView(this.getContext());
			view.setTextSize(18f);
			view.setPaddingRelative(8, 0, 8, 0);
		}
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		view.setText(spell_name);
		
		return view;
	}
}
