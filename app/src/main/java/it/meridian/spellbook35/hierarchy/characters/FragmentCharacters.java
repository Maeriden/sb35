package it.meridian.spellbook35.hierarchy.characters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.hierarchy.characters.character.FragmentCharacter;
import it.meridian.spellbook35.utils.AdapterList;
import it.meridian.spellbook35.utils.Utils;


public class FragmentCharacters extends it.meridian.spellbook35.Fragment
{
	static private final String SELECT_QUERY = "SELECT id, name FROM character";
	
	
	static public
	Fragment
	newInstance()
	{
		FragmentCharacters fragment = new FragmentCharacters();
		return fragment;
	}
	
	
	
	
	private AdapterList adapter;
	private AlertDialog dialog_create_character;
	private NameDialog  dialog_rename_character;
	private NameDialog  dialog_delete_character;
	
	/**
	 * Called when a fragment is first attached to its context.
	 * {@link #onCreate(Bundle)} will be called after this.
	 */
	@CallSuper
	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
	}
	
	
	
	
	/**
	 * Called to do initial creation of a fragment.  This is called after
	 * {@link #onAttach(Activity)} and before
	 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
	 * <p>
	 * <p>Note that this can be called while the fragment's drawer_activity_main is
	 * still in the process of being created.  As such, you can not rely
	 * on things like the drawer_activity_main's content view hierarchy being initialized
	 * at this point.  If you want to do work once the drawer_activity_main itself is
	 * created, see {@link #onActivityCreated(Bundle)}.
	 *
	 * @param savedInstanceState If the fragment is being re-created from
	 *                           a previous saved state, this is the state.
	 */
	@SuppressWarnings("ConstantConditions")
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		Cursor cursor = Application.query(SELECT_QUERY);
		this.adapter = new AdapterList(this::getListItemId,
		                               this::getListItemView,
		                               cursor);
		
		
		this.dialog_create_character = new NameDialog(this.getContext());
		{
			EditText edit = new EditText(this.dialog_create_character.getContext());
			edit.setId(android.R.id.edit);
			edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
			
			this.dialog_create_character.setTitle(R.string.choose_character_name);
			this.dialog_create_character.setView(edit);
			this.dialog_create_character.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok),
			                                       this::on_click_create_character_dialog_button);
			this.dialog_create_character.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.cancel),
			                                       this::on_click_create_character_dialog_button);
			this.dialog_create_character.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
		
		this.dialog_rename_character = new NameDialog(this.getContext());
		{
			EditText edit = new EditText(this.dialog_rename_character.getContext());
			edit.setId(android.R.id.edit);
			edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
			
			this.dialog_rename_character.setTitle(R.string.choose_character_name);
			this.dialog_rename_character.setView(edit);
			this.dialog_rename_character.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok),
			                                       this::on_click_rename_character_dialog_button);
			this.dialog_rename_character.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.cancel),
			                                       this::on_click_rename_character_dialog_button);
			this.dialog_rename_character.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
		
		this.dialog_delete_character = new NameDialog(this.getContext());
		{
			this.dialog_delete_character.setTitle(R.string.are_you_sure);
			this.dialog_delete_character.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok),
			                                       this::on_click_delete_character_dialog_button);
			this.dialog_delete_character.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.cancel),
			                                       this::on_click_delete_character_dialog_button);
		}
	}
	
	
	public @Override
	@Nullable View
	onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ListView list_view = new ListView(this.getContext());
		list_view.setOnItemClickListener(this::onClickListItem);
		list_view.setOnCreateContextMenuListener(this::onCreateListItemContextMenu);
		list_view.setAdapter(this.adapter);
		return list_view;
	}
	
	
	/**
	 * Called when the fragment's drawer_activity_main has been created and this
	 * fragment's view hierarchy instantiated.  It can be used to do final
	 * initialization once these pieces are in place, such as retrieving
	 * views or restoring state.  It is also useful for fragments that use
	 * {@link #setRetainInstance(boolean)} to retain their instance,
	 * as this callback tells the fragment when it is fully associated with
	 * the new drawer_activity_main instance.  This is called after {@link #onCreateView}
	 * and before {@link #onViewStateRestored(Bundle)}.
	 *
	 * @param savedInstanceState If the fragment is being re-created from
	 *                           a previous saved state, this is the state.
	 */
	public @Override @CallSuper
	void
	onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}
	
	
	public @Override
	void
	onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_characters, menu);
	}
	
	
	/**
	 * Called when all saved state has been restored into the view hierarchy
	 * of the fragment.  This can be used to do initialization based on saved
	 * state that you are letting the view hierarchy track itself, such as
	 * whether check box widgets are currently checked.  This is called
	 * after {@link #onActivityCreated(Bundle)} and before
	 * {@link #onStart()}.
	 *
	 * @param savedInstanceState If the fragment is being re-created from
	 *                           a previous saved state, this is the state.
	 */
	@CallSuper
	@Override
	public void onViewStateRestored(Bundle savedInstanceState)
	{
		super.onViewStateRestored(savedInstanceState);
	}
	
	
	public @Override @CallSuper
	void
	onStart()
	{
		super.onStart();
		this.getActivity().setTitle(R.string.app_name);
	}
	
	
	private
	void
	onClickListItem(AdapterView<?> parent, View view, int position, long id)
	{
		String character_name = ((TextView) view).getText().toString();
		Fragment fragment = FragmentCharacter.newInstance(character_name);
		this.activity_main().push_fragment(fragment);
	}
	
	
	private
	void
	onCreateListItemContextMenu(ContextMenu menu, View list_view, ContextMenu.ContextMenuInfo menuInfo)
	{
		this.getActivity().getMenuInflater().inflate(R.menu.context_character, menu);
	}
	
	
	public @Override
	boolean
	onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if(id == R.id.menu_action_add)
		{
			this.dialog_create_character.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public @Override
	boolean
	onContextItemSelected(MenuItem item)
	{
		ListView.AdapterContextMenuInfo info = (ListView.AdapterContextMenuInfo) item.getMenuInfo();
		Cursor cursor = (Cursor) this.adapter.getItem(info.position);
		String character_name = Utils.CursorGetString(cursor, "name");
		
		switch(item.getItemId())
		{
			case R.id.menu_action_update:
			{
				this.dialog_rename_character.show(character_name);
				return true;
			}
			
			case R.id.menu_action_delete:
			{
				this.dialog_delete_character.show(character_name);
				return true;
			}
		}
		return false;
	}
	
	
	public
	void
	refresh()
	{
		Cursor cursor = Application.query(SELECT_QUERY);
		this.adapter.swapCursor(cursor);
	}
	
	
	/**
	 * Get the row id associated with the specified position in the list.
	 *
	 * @param cursor Where to get data from. Already positioned at the correct position
	 * @return The id of the item at the specified position.
	 */
	public
	long
	getListItemId(Cursor cursor)
	{
		int id = Utils.CursorGetInt(cursor, "id");
		return id;
	}
	
	
	/**
	 * Get a View that displays the data at the specified position in the data set. You can either
	 * create a View manually or inflate it from an XML layout file. When the View is inflated, the
	 * parent View (GridView, ListView...) will apply default layout parameters unless you use
	 * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 *
	 * @param cursor      Where to get data from. Already positioned at the correct position
	 * @param convertView The old view to reuse, if possible. Note: You should check that this view
	 *                    is non-null and of an appropriate type before using. If it is not possible to convert
	 *                    this view to display the correct data, this method can create a new view.
	 * @param parent      The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	public
	View
	getListItemView(Cursor cursor, View convertView, ViewGroup parent)
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
	
	
	private
	void
	on_click_create_character_dialog_button(DialogInterface d, int button)
	{
		if(button == DialogInterface.BUTTON_POSITIVE)
		{
			Dialog dialog = (Dialog) d;
			EditText edit = dialog.findViewById(android.R.id.edit);
			
			String name = edit.getText().toString();
			boolean success = Application.create_character(name);
			
			if(success)
				this.refresh();
			else
				Toast.makeText(this.getContext(), "Error creating character", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	private
	void
	on_click_rename_character_dialog_button(DialogInterface d, int button)
	{
		if(button == DialogInterface.BUTTON_POSITIVE)
		{
			NameDialog dialog = (NameDialog) d;
			EditText edit = dialog.findViewById(android.R.id.edit);
			
			String old_name = dialog.character_name;
			String new_name = edit.getText().toString();
			boolean success = Application.rename_character(old_name, new_name);
			
			if(success)
				this.refresh();
			else
				Toast.makeText(this.getContext(), "Error renaming character", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	private
	void
	on_click_delete_character_dialog_button(DialogInterface d, int button)
	{
		if(button == DialogInterface.BUTTON_POSITIVE)
		{
			NameDialog dialog = (NameDialog) d;
			boolean success = Application.delete_character(dialog.character_name);
			
			if(success)
				this.refresh();
			else
				Toast.makeText(this.getContext(), "Error deleting character", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	
	
	private static class NameDialog extends AlertDialog
	{
		private String character_name;
		
		
		NameDialog(Context context)
		{
			super(context, 0);
		}
		
		
		void
		show(@NonNull String character_name)
		{
			this.character_name = character_name;
			super.show();
		}
		
		
		protected @Override
		void
		onStart()
		{
			super.onStart();
			EditText edit = this.findViewById(android.R.id.edit);
			if(edit != null && this.character_name != null)
			{
				edit.setText(this.character_name);
				edit.setSelection(this.character_name.length());
			}
		}
		
		
		protected @Override
		void
		onStop()
		{
			super.onStop();
			this.character_name = null;
		}
	}
}
