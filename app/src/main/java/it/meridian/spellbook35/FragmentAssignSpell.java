package it.meridian.spellbook35;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewSpellAssign;


public class FragmentAssignSpell extends FragmentCharacterSpellKnown
{
	static protected final String QUERY_CHILDREN =
			"SELECT known.rowid AS known_id, known.spell_name AS spell_name, spell.summary AS spell_desc " +
			"  FROM character_spell_known known, spell " +
			" WHERE known.spell_name = spell.name " +
			"   AND known.character = ? " +
			"   AND known.source_name = ? " +
			"   AND known.level = ?";
	
	
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		view.setOnKeyListener(this::onKeyListener);
		ExpandableListView list_view = (ExpandableListView)view;
		list_view.setOnCreateContextMenuListener(null);
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		this.getActivity().setTitle(this.character_name);
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// Do not create a menu
	}
	
	
	
	@Override
	protected boolean onClickExpandableListItem(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		Bundle args = new Bundle(1);
		args.putString(FragmentSpellInfo.ARG_KEY_SPELL, spell_name);
		this.frag_spell_info.setArguments(args);
		
		// NOTE: Needs to use getParentFragment because this fragment's manager is the ViewPager's childManager
		this.getFragmentManager().beginTransaction()
				.replace(R.id.activity_main_content, this.frag_spell_info)
				.addToBackStack(null)
				.commit();
		return true;
	}
	
	
	@Override
	public Cursor getExpandableListChildrenCursor(Cursor group_cursor)
	{
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		int spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		Cursor children_cursor = Application.query(QUERY_CHILDREN,
		                                           this.character_name,
		                                           source_name,
		                                           Integer.toString(spell_level));
		return children_cursor;
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
		Cursor child_cursor = (Cursor) super.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		try
		{
			ISpellChoiceListener listener = (ISpellChoiceListener)this.getTargetFragment();
			listener.onSpellChoiceResult(spell_name);
		}
		catch(ClassCastException ignored)
		{
			Toast.makeText(this.getContext(),
			               "ERROR: Target fragment does not implement IResultListener",
			               Toast.LENGTH_SHORT).show();
		}
		
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
		void onSpellChoiceResult(String spell_name);
		void onSpellChoiceCancel();
	}
}
