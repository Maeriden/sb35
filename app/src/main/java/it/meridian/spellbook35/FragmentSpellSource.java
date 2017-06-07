package it.meridian.spellbook35;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.Objects;

import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewSpellAssign;
import it.meridian.spellbook35.views.ViewSpellSlotGroup;


public class FragmentSpellSource extends android.support.v4.app.Fragment implements AdapterExpandableList.ISupplier
{
	static public final String ARG_KEY_SOURCE = "source";
	
	static protected final String QUERY_GROUPS =
			"  SELECT source   AS source, " +
			"         level    AS level, " +
			"         COUNT(*) AS count " +
			"    FROM spell_lists " +
			"   WHERE source = ? " +
			"GROUP BY level";
	
	static protected final String QUERY_CHILDREN =
			"SELECT spell.rowid   AS spell_id, " +
			"       spell.name    AS spell_name, " +
			"       spell.summary AS spell_desc " +
	        "  FROM spell_lists list, spell" +
	        " WHERE list.spell_name = spell.name " +
	        "   AND list.source = ? " +
	        "   AND list.level = ? " +
			"   AND spell.disabled = 0";
	
	
	private String source_name;
	private AdapterExpandableList adapter;
	private FragmentSpellInfo frag_spell_info;
	
	
	
	public FragmentSpellSource()
	{
		this.frag_spell_info = new FragmentSpellInfo();
		this.adapter = new AdapterExpandableList(this, null);
	}
	
	
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		String source_name = (String) this.getArguments().get(ARG_KEY_SOURCE);
		if(!Objects.equals(this.source_name, source_name))
		{
			this.source_name = source_name;
			Cursor groups_cursor = Application.query(QUERY_GROUPS, this.source_name);
			this.adapter.swapGroupsCursor(groups_cursor);
		}
	}
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{
		ExpandableListView list_view = new ExpandableListView(this.getContext());
		list_view.setId(R.id.expandable_list_view);
		list_view.setOnChildClickListener(this::onClickExpandableListItem);
		list_view.setAdapter(this.adapter);
		list_view.setOnKeyListener(this::onKeyListener);
		return list_view;
	}
	
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		view.setOnKeyListener(this::onKeyListener);
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		this.getActivity().setTitle(this.source_name);
	}
	
	
	
	private boolean onClickExpandableListItem(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		Bundle args = new Bundle(1);
		args.putString(FragmentSpellInfo.ARG_KEY_SPELL, spell_name);
		this.frag_spell_info.setArguments(args);
		
		this.getFragmentManager().beginTransaction()
				.replace(R.id.activity_main_content, this.frag_spell_info)
				.addToBackStack(null)
				.commit();
		return true;
	}
	
	
	@Override
	public long getExpandableListGroupId(Cursor group_cursor, int groupPosition)
	{
		int level = Utils.CursorGetInt(group_cursor, "level");
		return level;
	}
	
	@Override
	public long getExpandableListChildId(Cursor child_cursor, int groupPosition, int childPosition)
	{
		int spell_id = Utils.CursorGetInt(child_cursor, "spell_id");
		return spell_id;
	}
	
	@Override
	public Cursor getExpandableListChildrenCursor(Cursor group_cursor)
	{
		int level = Utils.CursorGetInt(group_cursor, "level");
		Cursor children_cursor = Application.query(QUERY_CHILDREN,
		                                           this.source_name,
		                                           Integer.toString(level));
		return children_cursor;
	}
	
	
	@Override
	public View getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewSpellSlotGroup view = (ViewSpellSlotGroup) convertView;
		if(view == null)
			view = new ViewSpellSlotGroup(this.getContext());
		
		int level = Utils.CursorGetInt(group_cursor, "level");
		int count = Utils.CursorGetInt(group_cursor, "count");
		
		String level_text = this.getContext().getResources().getString(R.string.level_N, level);
		String count_text = Integer.toString(count);
		
		view.setLevelText(level_text);
		view.setCountText(count_text);
		return view;
	}
	
	@Override
	public View getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewSpellAssign view = (ViewSpellAssign) convertView;
		if(view == null)
		{
			view = new ViewSpellAssign(this.getContext());
			view.setButtonOnClickListener(this::onClickButtonAssign);
		}
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		String spell_desc = Utils.CursorGetString(child_cursor, "spell_desc");
		
		view.setTextName(spell_name);
		view.setTextDesc(spell_desc);
		view.setPosition(groupPosition, childPosition);
		
		return view;
	}
	
	private void onClickButtonAssign(ViewSpellAssign view, int groupPosition, int childPosition)
	{
		Cursor group_cursor = (Cursor) this.adapter.getGroup(groupPosition);
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		int level = Utils.CursorGetInt(group_cursor, "level");
		
		try
		{
			ISpellChoiceListener listener = (ISpellChoiceListener)this.getTargetFragment();
			listener.onSpellChoiceResult(this.source_name, level, spell_name);
		}
		catch(ClassCastException ignored)
		{
			Toast.makeText(this.getContext(),
			               "ERROR: Target fragment does not implement IResultListener",
			               Toast.LENGTH_SHORT).show();
		}
		
		this.getFragmentManager().popBackStack();
		this.getFragmentManager().popBackStack();
	}
	
	
	
	// Hack to call onCancelFragmentAssignSpell when the user presses BACK
	private boolean onKeyListener(View view, int keyCode, KeyEvent event)
	{
//		Log.d("FragmentAssignSpell", "onKeyListener");
		if(event.getAction() == KeyEvent.ACTION_UP)
		{
			if(keyCode == KeyEvent.KEYCODE_BACK)
			{
				try
				{
					ISpellChoiceListener listener = (ISpellChoiceListener)this.getTargetFragment();
					listener.onSpellChoiceCancel();
				}
				catch(ClassCastException ignored)
				{
					Toast.makeText(this.getContext(),
					               "ERROR: Target fragment does not implement IResultListener",
					               Toast.LENGTH_SHORT).show();
				}
			}
		}
		return false;
	}
	
	
	
	public interface ISpellChoiceListener
	{
		void onSpellChoiceResult(String source_name, int level, String spell_name);
		void onSpellChoiceCancel();
	}
}
