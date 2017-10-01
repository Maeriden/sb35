package it.meridian.spellbook35;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
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

import it.meridian.spellbook35.utils.AdapterList;
import it.meridian.spellbook35.utils.Utils;


public class FragmentCharacters extends android.support.v4.app.Fragment
{
	static private final String SELECT_QUERY = "SELECT id, name FROM character";
	static private final String UPDATE_WHERE = "name = ?";
	static private final String DELETE_WHERE = "name = ?";
	
	
	
	
	private AdapterList adapter;
	private FragmentCharacter frag_character;
	private AlertDialog alert_create_character;
	
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
	 * <p>Note that this can be called while the fragment's activity is
	 * still in the process of being created.  As such, you can not rely
	 * on things like the activity's content view hierarchy being initialized
	 * at this point.  If you want to do work once the activity itself is
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
		
		this.frag_character = new FragmentCharacter();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
		{
			EditText edit = new EditText(builder.getContext());
			edit.setId(android.R.id.edit);
			edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
			
			builder.setTitle(R.string.choose_character_name);
			builder.setView(edit);
			builder.setPositiveButton(R.string.ok, this::onClickCreateCharacterAlertButton);
			builder.setNegativeButton(R.string.cancel, this::onClickCreateCharacterAlertButton);
		}
		
		this.alert_create_character = builder.create();
		this.alert_create_character.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
	
	
	
	
	/**
	 * Called to have the fragment instantiate its user interface view.
	 * This is optional, and non-graphical fragments can return null (which
	 * is the default implementation).  This will be called between
	 * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
	 * <p>
	 * <p>If you return a View from here, you will later be called in
	 * {@link #onDestroyView} when the view is being released.
	 *
	 * @param inflater           The LayoutInflater object that can be used to inflate
	 *                           any views in the fragment,
	 * @param container          If non-null, this is the parent view that the fragment's
	 *                           UI should be attached to.  The fragment should not add the view itself,
	 *                           but this can be used to generate the LayoutParams of the view.
	 * @param savedInstanceState If non-null, this fragment is being re-constructed
	 *                           from a previous saved state as given here.
	 * @return Return the View for the fragment's UI, or null.
	 */
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ListView list_view = new ListView(this.getContext());
		list_view.setOnItemClickListener(this::onClickListItem);
		list_view.setOnCreateContextMenuListener(this::onCreateListItemContextMenu);
		list_view.setAdapter(this.adapter);
		return list_view;
	}
	
	
	
	
	/**
	 * Called when the fragment's activity has been created and this
	 * fragment's view hierarchy instantiated.  It can be used to do final
	 * initialization once these pieces are in place, such as retrieving
	 * views or restoring state.  It is also useful for fragments that use
	 * {@link #setRetainInstance(boolean)} to retain their instance,
	 * as this callback tells the fragment when it is fully associated with
	 * the new activity instance.  This is called after {@link #onCreateView}
	 * and before {@link #onViewStateRestored(Bundle)}.
	 *
	 * @param savedInstanceState If the fragment is being re-created from
	 *                           a previous saved state, this is the state.
	 */
	@CallSuper
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}
	
	
	
	
	/**
	 * Initialize the contents of the Activity's standard options menu.  You
	 * should place your menu items in to <var>menu</var>.  For this method
	 * to be called, you must have first called {@link #setHasOptionsMenu}.  See
	 * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
	 * for more information.
	 *
	 * @param menu The options menu in which you place your items.
	 * @see #setHasOptionsMenu
	 * @see #onPrepareOptionsMenu
	 * @see #onOptionsItemSelected
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
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
	
	
	
	
	/**
	 * Called when the Fragment is visible to the user.  This is generally
	 * tied to {@link Activity#onStart() Activity.onStart} of the containing
	 * Activity's lifecycle.
	 */
	@CallSuper
	@Override
	public void onStart()
	{
		super.onStart();
		this.getActivity().setTitle(R.string.app_name);
		
		Application.current_character = null;
	}
	
	
	
	
	/**
	 * Callback method to be invoked when an item in this AdapterView has
	 * been clicked.
	 * <p>
	 * Implementers can call getItemAtPosition(position) if they need
	 * to access the data associated with the selected item.
	 *
	 * @param parent   The AdapterView where the click happened.
	 * @param view     The view within the AdapterView that was clicked (this
	 *                 will be a view provided by the adapter)
	 * @param position The position of the view in the adapter.
	 * @param id       The row id of the item that was clicked.
	 */
	private void onClickListItem(AdapterView<?> parent, View view, int position, long id)
	{
		String character = ((TextView) view).getText().toString();
		
		Bundle args = new Bundle(1);
		args.putString(FragmentCharacter.ARG_KEY_CHARACTER, character);
		this.frag_character.setArguments(args);
		
		this.getFragmentManager().beginTransaction()
				.replace(R.id.activity_main_content, this.frag_character)
				.addToBackStack(null)
				.commit();
		
		Application.current_character = character;
	}
	
	
	
	private void onCreateListItemContextMenu(ContextMenu menu, View list_view, ContextMenu.ContextMenuInfo menuInfo)
	{
		this.getActivity().getMenuInflater().inflate(R.menu.context_character, menu);
	}
	
	
	
	
	/**
	 * This hook is called whenever an item in your options menu is selected.
	 * The default implementation simply returns false to have the normal
	 * processing happen (calling the item's Runnable or sending a message to
	 * its Handler as appropriate).  You can use this method for any items
	 * for which you would like to do processing without those other
	 * facilities.
	 * <p>
	 * <p>Derived classes should call through to the base class for it to
	 * perform the default menu handling.
	 *
	 * @param item The menu item that was selected.
	 * @return boolean Return false to allow normal menu processing to
	 * proceed, true to consume it here.
	 * @see #onCreateOptionsMenu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if(id == R.id.menu_action_add)
		{
			this.alert_create_character.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		ListView.AdapterContextMenuInfo info = (ListView.AdapterContextMenuInfo) item.getMenuInfo();
		Cursor cursor = (Cursor) this.adapter.getItem(info.position);
		String name = Utils.CursorGetString(cursor, "name");
		
		switch(item.getItemId())
		{
			case R.id.menu_action_update:
			{
				// TODO: Create rename dialog
				Toast.makeText(this.getContext(), "Not implemented", Toast.LENGTH_SHORT).show();
				return true;
			}
			
			case R.id.menu_action_delete:
			{
				boolean success = this.delete_character(name);
				if(success)
					this.refresh();
				
				return true;
			}
		}
		return false;
	}
	
	
	
	
	private boolean create_character(String name)
	{
		if(name.length() > 0)
		{
			ContentValues values = new ContentValues(1);
			values.put("name", name);
			long rowid = Application.insert("character", values);
			if(rowid != -1)
			{
				this.refresh();
				return true;
			}
		}
		Toast.makeText(this.getContext(), "Error creating character", Toast.LENGTH_SHORT).show();
		return false;
	}
	
	
	private boolean rename_character(String old_name, String new_name)
	{
		if(old_name != null && old_name.length() > 0)
		{
			if(new_name != null && new_name.length() > 0)
			{
				ContentValues values = new ContentValues(1);
				values.put("name", new_name);
				int affected = Application.update("character", values, UPDATE_WHERE, old_name);
				return affected > 0;
			}
		}
		return false;
	}
	
	
	
	private boolean delete_character(String name)
	{
		if(name != null && name.length() > 0)
		{
			long affected = Application.delete("character",
			                                   DELETE_WHERE,
			                                   name);
			return affected > 0;
		}
		return false;
	}
	
	
	
	
	public void refresh()
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
	public long getListItemId(Cursor cursor)
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
	public View getListItemView(Cursor cursor, View convertView, ViewGroup parent)
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
	
	
	
	
	@SuppressWarnings("ConstantConditions")
	public void onClickCreateCharacterAlertButton(DialogInterface dialog, int which)
	{
		if(which == DialogInterface.BUTTON_POSITIVE)
		{
			AlertDialog alert = (AlertDialog) dialog;
			EditText edit = (EditText) alert.findViewById(android.R.id.edit);
			String name = edit.getText().toString();
			this.create_character(name);
		}
	}
}
