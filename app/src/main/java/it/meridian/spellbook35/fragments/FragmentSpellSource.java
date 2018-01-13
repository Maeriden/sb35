package it.meridian.spellbook35.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewSpellLearnable;
import it.meridian.spellbook35.views.ViewCharacterSpellSlotGroup;

import static it.meridian.spellbook35.utils.Utils.Assert;


public class FragmentSpellSource extends it.meridian.spellbook35.Fragment implements AdapterExpandableList.ISupplier
{
	static private final String ARG_SOURCE_NAME    = "source_name";
	static private final String ARG_CHARACTER_NAME = "character_name";
	
	static protected final String QUERY_GROUPS =
			"  SELECT spells.source AS source, " +
			"         spells.level  AS level,  " +
			"         COUNT(*)      AS count   " +
			"    FROM source_spells spells     " +
			"   WHERE spells.source = ?        " +
			"GROUP BY spells.level";
	
	static protected final String QUERY_GROUPS_FOR_CHARACTER =
			"  SELECT spells.source AS source, " +
			"         spells.level  AS level,  " +
			"         COUNT(*)      AS count   " +
			"    FROM source_spells spells     " +
			"   WHERE spells.source = ?        " +
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
			"   AND spell.disabled = 0           ";
	
	static protected final String QUERY_CHILDREN_FOR_CHARACTER =
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
	
	
	static public
	Fragment
	newInstance(@NonNull String source_name, @NonNull String character_name, ISpellChoiceListener callback)
	{
		FragmentSpellSource fragment = new FragmentSpellSource();
		{
			Bundle args = new Bundle(2);
			args.putString(ARG_SOURCE_NAME, source_name);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
			fragment.callback = callback;
		}
		return fragment;
	}
	
	
	private String                  source_name;
	private String                  character_name;
	private ISpellChoiceListener    callback;
	private AdapterExpandableList   adapter;
	private ArrayList<LearnedSpell> choices;
	
	
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
		
		Cursor groups_cursor;
		if(this.character_name == null)
			groups_cursor = Application.query(QUERY_GROUPS, this.source_name);
		else
			groups_cursor = Application.query(QUERY_GROUPS_FOR_CHARACTER, this.source_name, this.character_name);
		this.adapter = new AdapterExpandableList(this, groups_cursor);
		this.choices = new ArrayList<>();
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
		list_view.setOnChildClickListener(this::onClickExpandableListItem);
		list_view.setAdapter(this.adapter);
		list_view.setOnKeyListener(this::onKeyListener);
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
		if(item.getItemId() == R.id.menu_action_done)
		{
			this.onClickDone();
		}
		return true;
	}
	
	
	private
	boolean
	onClickExpandableListItem(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name   = Utils.CursorGetString(child_cursor, "spell_name");
		
		Fragment fragment = FragmentSpellInfo.newInstance(spell_name);
		this.activity_main().push_fragment(fragment);
		return true;
	}
	
	
	private
	void
	onClickCheckbox(ViewSpellLearnable view, int groupPosition, int childPosition, boolean isChecked)
	{
		Cursor group_cursor = (Cursor) this.adapter.getGroup(groupPosition);
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		int    level      = Utils.CursorGetInt(group_cursor, "level");
		
		LearnedSpell item = new LearnedSpell(this.source_name, level, spell_name);
		if(isChecked)
		{
			this.choices.add(item);
		}
		else
		{
			this.choices.remove(item);
		}
	}
	
	
	private
	void
	onClickDone()
	{
		if(this.callback == null)
			return;
		if(this.choices.isEmpty())
			return;
		
		// TODO: Look into doing this with intents
		ISpellChoiceListener listener = this.callback;
		listener.onLearnSpellChoiceResult(this.choices);
	}
	
	
	// Hack to call onCancelFragmentAssignSpell when the user presses BACK
	private
	boolean
	onKeyListener(View view, int keyCode, KeyEvent event)
	{
		if(this.getTargetFragment() == null)
			return false;
		if(event.getAction() != KeyEvent.ACTION_UP || keyCode != KeyEvent.KEYCODE_BACK)
			return false;

		Assert(this.getTargetFragment() instanceof ISpellChoiceListener);
		ISpellChoiceListener listener = (ISpellChoiceListener)this.getTargetFragment();
		listener.onLearnSpellChoiceCancel();
		return false;
	}
	
	
	@Override
	public
	long
	getExpandableListGroupId(Cursor group_cursor, int groupPosition)
	{
		return Utils.CursorGetInt(group_cursor, "level");
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
		int level = Utils.CursorGetInt(group_cursor, "level");
		if(this.character_name == null)
		{
			return Application.query(QUERY_CHILDREN,
			                         this.source_name,
			                         Integer.toString(level));
		}
		else
		{
			return Application.query(QUERY_CHILDREN_FOR_CHARACTER,
			                         this.source_name,
			                         Integer.toString(level),
			                         this.character_name);
		}
	}
	
	
	@Override
	public
	View
	getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewCharacterSpellSlotGroup view = (ViewCharacterSpellSlotGroup) convertView;
		if(view == null)
			view = new ViewCharacterSpellSlotGroup(this.getContext());
		
		int level = Utils.CursorGetInt(group_cursor, "level");
		int count = Utils.CursorGetInt(group_cursor, "count");
		
		String level_text = this.getContext().getResources().getString(R.string.level_N, level);
		String count_text = Integer.toString(count);
		
		view.setLevelText(level_text);
		view.setCountText(count_text);
		return view;
	}
	
	
	@Override
	public
	View
	getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewSpellLearnable view = (ViewSpellLearnable) convertView;
		if(view == null)
		{
			view = new ViewSpellLearnable(this.getContext());
			view.setCheckboxStateChangeListener(this::onClickCheckbox);
		}
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		String spell_desc = Utils.CursorGetString(child_cursor, "spell_desc");
		
		view.setTextName(spell_name);
		view.setTextDesc(spell_desc);
		view.setPosition(groupPosition, childPosition);
		view.setCheckable(this.character_name != null);
		
		boolean is_checked = false;
		for(LearnedSpell choice : this.choices)
		{
			if(choice.spell_name.equals(spell_name))
			{
				is_checked = true;
				break;
			}
		}
		view.setCheckboxState(is_checked);
		
		return view;
	}
	
	
	
	
	class LearnedSpell
	{
		final String source_name;
		final int    spell_level;
		final String spell_name;
		
		
		LearnedSpell(String source_name, int spell_level, String spell_name1)
		{
			this.source_name = source_name;
			this.spell_level = spell_level;
			this.spell_name  = spell_name1;
		}
	}
	
	
	
	
	public interface ISpellChoiceListener
	{
		void onLearnSpellChoiceResult(ArrayList<LearnedSpell> choices);
		void onLearnSpellChoiceCancel();
	}
}
