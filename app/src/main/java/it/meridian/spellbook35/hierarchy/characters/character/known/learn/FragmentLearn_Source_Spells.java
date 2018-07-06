package it.meridian.spellbook35.hierarchy.characters.character.known.learn;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.HashMap;
import java.util.Map;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.hierarchy.FragmentSpellInfo;
import it.meridian.spellbook35.hierarchy.ISpellSelectionListener;
import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.SpellSelectionInfo;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewLearnSpellChild;
import it.meridian.spellbook35.views.ViewLearnSpellGroup;


public class FragmentLearn_Source_Spells extends it.meridian.spellbook35.Fragment
		implements AdapterExpandableList.ISupplier
{
	static private final String ARG_SOURCE_NAME    = "source_name";
	static private final String ARG_CHARACTER_NAME = "character_name";
	
	static protected final String QUERY_GROUPS =
			"  SELECT spells.source AS source,       " +
			"         spells.level  AS spell_level,  " +
			"         COUNT(*)      AS count         " +
			"    FROM source_spells spells           " +
			"   WHERE spells.source = ?              " +
			"   AND NOT EXISTS (SELECT 1                               " +
			"                     FROM character_spell_known known     " +
			"                    WHERE known.character = ?             " +
			"                      AND known.spell     = spells.spell  " +
			"                      AND known.source    = spells.source " +
			"                      AND known.level     = spells.level) " +
			"GROUP BY spells.level";
	
	static protected final String QUERY_CHILDREN =
			"SELECT spell.id      AS spell_id,   " +
			"       spell.name    AS spell_name, " +
			"       spell.summary AS spell_desc  " +
			"  FROM source_spells spells,        " +
			"       spell         spell          " +
			" WHERE spells.spell   = spell.name  " +
			"   AND spells.source  = ?           " +
			"   AND spells.level   = ?           " +
			"   AND spell.disabled = 0           " +
			"   AND NOT EXISTS (SELECT 1                               " +
			"                     FROM character_spell_known known     " +
			"                    WHERE known.character = ?             " +
			"                      AND known.spell     = spells.spell  " +
			"                      AND known.source    = spells.source " +
			"                      AND known.level     = spells.level) ";
	
	static protected final String QUERY_GROUPS_NOCHARACTER = // For spell browser
			"  SELECT spells.source AS source, " +
			"         spells.level  AS level,  " +
			"         COUNT(*)      AS count   " +
			"    FROM source_spells spells     " +
			"   WHERE spells.source = ?        " +
			"GROUP BY spells.level";
	
	static protected final String QUERY_CHILDREN_NOCHARACTER = // For spell browser
			"SELECT spell.id      AS spell_id,   " +
			"       spell.name    AS spell_name, " +
			"       spell.summary AS spell_desc  " +
			"  FROM source_spells spells,        " +
			"       spell         spell          " +
			" WHERE spells.spell   = spell.name  " +
			"   AND spells.source  = ?           " +
			"   AND spells.level   = ?           " +
			"   AND spell.disabled = 0           ";
	
	
	static public
	Fragment
	newInstance(@NonNull String source_name, @NonNull String character_name, ISpellSelectionListener callback)
	{
		FragmentLearn_Source_Spells fragment = new FragmentLearn_Source_Spells();
		{
			Bundle args = new Bundle(2);
			args.putString(ARG_SOURCE_NAME, source_name);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
			fragment.callback = callback;
		}
		return fragment;
	}
	
	
	protected String                          character_name;
	protected String                          source_name;
	protected ISpellSelectionListener         callback;
	protected AdapterExpandableList           adapter;
	protected Map<String, SpellSelectionInfo> choices;
	protected boolean[]                       checked_groups;
	
	
	@CallSuper
	@Override
	public
	void
	onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		this.source_name    = this.getArguments().getString(ARG_SOURCE_NAME);
		this.character_name = this.getArguments().getString(ARG_CHARACTER_NAME);
		
		Cursor groups_cursor = Application.query(QUERY_GROUPS, this.source_name, this.character_name);
		this.adapter = new AdapterExpandableList(this, groups_cursor);
		this.choices = new HashMap<>();
		this.checked_groups = new boolean[10];
	}
	
	
	@Nullable
	@Override
	public
	View
	onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{
		ExpandableListView list_view = new ExpandableListView(this.getContext());
		list_view.setId(R.id.expandable_list_view);
		list_view.setGroupIndicator(null);
		list_view.setOnChildClickListener(this::on_click_expandable_list_item);
		list_view.setAdapter(this.adapter);
//		list_view.setOnKeyListener(this::onKeyListener);
		return list_view;
	}
	
	
	@Override
	public
	void
	onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_spell_learn, menu);
		MenuItem item = menu.findItem(R.id.menu_action_done);
		item.setEnabled(this.character_name != null);
		item.setVisible(this.character_name != null);
	}
	
	
	@Override
	public
	void
	onStart()
	{
		super.onStart();
		this.getActivity().setTitle(this.source_name);
	}
	
	
	@Override
	public
	boolean
	onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_action_done:
			{
				if(!this.choices.isEmpty() && this.callback != null)
				{
					// TODO: Look into doing this with intents
					this.callback.on_spell_selection_accept(this.choices.values());
				}
			} break;
		}
		return true;
	}
	
	
	private
	boolean
	on_click_expandable_list_item(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name   = Utils.CursorGetString(child_cursor, "spell_name");
		
		Fragment fragment = FragmentSpellInfo.newInstance(spell_name);
		this.activity_main().push_fragment(fragment);
		return true;
	}
	
	
	private
	void
	on_group_check_state_changed(ViewLearnSpellGroup view, int group_position, boolean is_checked)
	{
		Cursor group_cursor = (Cursor) this.adapter.getGroup(group_position);
		Cursor children_cursor = this.getExpandableListChildrenCursor(group_cursor);
		
		int spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		for(int i = 0; i < children_cursor.getCount(); ++i)
		{
			children_cursor.moveToPosition(i);
			String spell_name = Utils.CursorGetString(children_cursor, "spell_name");
			if(is_checked)
			{
				SpellSelectionInfo item = new SpellSelectionInfo(this.source_name, spell_level, spell_name);
				this.choices.put(spell_name, item);
			}
			else
			{
				this.choices.remove(spell_name);
			}
		}
		children_cursor.close();
		this.checked_groups[group_position] = is_checked;
		this.refresh();
	}
	
	
	private
	void
	on_child_check_state_changed(ViewLearnSpellChild view, int groupPosition, int childPosition, boolean isChecked)
	{
		Cursor group_cursor = (Cursor) this.adapter.getGroup(groupPosition);
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		
		String spell_name   = Utils.CursorGetString(child_cursor, "spell_name");
		int    spell_level  = Utils.CursorGetInt(group_cursor, "spell_level");
		
		if(isChecked)
		{
			SpellSelectionInfo item = new SpellSelectionInfo(this.source_name, spell_level, spell_name);
			this.choices.put(spell_name, item);
		}
		else
		{
			this.choices.remove(spell_name);
		}
	}
	
	
	private
	boolean
	onKeyListener(View view, int keyCode, KeyEvent event)
	{
		// Call on_spell_selection_cancel() when the user presses BACK
		if(event.getAction() != KeyEvent.ACTION_UP || keyCode != KeyEvent.KEYCODE_BACK)
			return false;
		
		this.callback.on_spell_selection_cancel();
		return false;
	}
	
	
	public
	void
	refresh()
	{
		Cursor groups_cursor = Application.query(QUERY_GROUPS, this.source_name, this.character_name);
		this.adapter.swapGroupsCursor(groups_cursor);
	}
	
	
	@Override
	public
	long
	getExpandableListGroupId(Cursor group_cursor, int groupPosition)
	{
		return Utils.CursorGetInt(group_cursor, "spell_level");
	}
	
	
	@Override
	public
	long
	getExpandableListChildId(Cursor child_cursor, int groupPosition, int childPosition)
	{
		return Utils.CursorGetInt(child_cursor, "spell_id");
	}
	
	
	@Override
	public
	Cursor
	getExpandableListChildrenCursor(Cursor group_cursor)
	{
		int spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		return Application.query(QUERY_CHILDREN,
		                         this.source_name,
		                         Integer.toString(spell_level),
		                         this.character_name);
	}
	
	
	@Override
	public
	View
	getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewLearnSpellGroup view = (ViewLearnSpellGroup) convertView;
		if(view == null)
		{
			view = new ViewLearnSpellGroup(this.getContext());
			view.set_check_state_change_listener(this::on_group_check_state_changed);
		}
		
		int spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		int spell_count = Utils.CursorGetInt(group_cursor, "count");
		view.set_content(this.source_name, spell_level, spell_count);
		view.set_position(groupPosition);
		view.set_checked(this.checked_groups[groupPosition]);
		return view;
	}
	
	
	@Override
	public
	View
	getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewLearnSpellChild view = (ViewLearnSpellChild) convertView;
		if(view == null)
		{
			view = new ViewLearnSpellChild(this.getContext());
			view.set_check_state_change_listener(this::on_child_check_state_changed);
		}

		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		String spell_desc = Utils.CursorGetString(child_cursor, "spell_desc");
		view.set_content(spell_name, spell_desc);
		view.set_position(groupPosition, childPosition);
		view.set_checked(this.choices.containsKey(spell_name));
		return view;
	}
}
