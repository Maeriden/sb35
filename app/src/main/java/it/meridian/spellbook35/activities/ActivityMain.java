package it.meridian.spellbook35.activities;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Stack;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.hierarchy.characters.FragmentCharacters;
import it.meridian.spellbook35.hierarchy.FragmentSpellInfo;

import static it.meridian.spellbook35.utils.Utils.*;


public class ActivityMain extends android.support.v7.app.AppCompatActivity
{
	static public final int REQUEST_CODE_OPEN_FILE = 1;
	
	
//	public FragNavController  fragnav_controller;
	private Stack<Fragment> fragment_stack = new Stack<>();
	
	
	protected @Override
	void
	onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
//		this.setContentView(R.layout.activity_main_drawer);
		this.setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		this.setSupportActionBar(toolbar);
		
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		if(drawer != null)
		{
			ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
			drawer.addDrawerListener(toggle);
			toggle.syncState();
		}
		
		NavigationView navigationView = findViewById(R.id.nav_view);
		if(navigationView != null)
		{
			navigationView.setNavigationItemSelectedListener(this::onNavigationDrawerItemSelected);
		}
		
//		this.fragnav_controller = FragNavController.newBuilder(savedInstanceState,
//		                                                       this.getSupportFragmentManager(),
//		                                                       R.id.activity_main_content)
//		                                           .rootFragmentListener(this::getNavigationRootFragments, 2)
//		                                           .build();
		Fragment root = FragmentCharacters.newInstance();
		this.getSupportFragmentManager()
	        .beginTransaction()
	        .addToBackStack(null)
	        .add(R.id.activity_main_content, root)
	        .commit();
		this.fragment_stack.push(root);
	}
	
	
	protected @Override
	void
	onNewIntent(Intent intent)
	{
		this.setIntent(intent);
		
		String action = intent.getAction();
		if(Objects.equals(action, Intent.ACTION_SEARCH))
		{
			// Do nothing: only respond to search if it is performed by selecting a suggestion
//			String spell_name = intent.getStringExtra(SearchManager.QUERY);
//			Fragment fragment = FragmentSpellInfo.newInstance(spell_name);
//			this.push_fragment(fragment);
		}
		else
		if(Objects.equals(action, Intent.ACTION_VIEW))
		{
			String spell_name = intent.getData().getLastPathSegment();
			Fragment fragment = FragmentSpellInfo.newInstance(spell_name);
			this.push_fragment(fragment);
		}
	}
	
	
	public @Override
	void
	onBackPressed()
	{
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		if(drawer != null && drawer.isDrawerOpen(GravityCompat.START))
		{
			drawer.closeDrawer(GravityCompat.START);
		}
		else if(this.get_stack_size() > 1)
		{
			this.pop_fragment();
		}
//		else if(this.fragnav_controller.getCurrentStack().size() > 1)
//		{
//			this.pop_fragment();
//		}
//		else if(this.fragnav_controller.getCurrentStackIndex() != 0)
//		{
//			this.fragnav_controller.switchTab(0);
//		}
		else
		{
			super.onBackPressed();
		}
	}
	
	
	public @Override
	boolean
	onCreateOptionsMenu(Menu menu)
	{
		this.getMenuInflater().inflate(R.menu.options_activity_main, menu);
		
		// Get the SearchView and set the searchable configuration
		SearchView     searchView     = (SearchView) menu.findItem(R.id.menu_action_search).getActionView();
		SearchManager  searchManager  = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
		SearchableInfo searchableInfo = searchManager.getSearchableInfo(this.getComponentName());
		// Assumes current activity is the searchable activity
		searchView.setSearchableInfo(searchableInfo);
		searchView.setIconifiedByDefault(true);
		searchView.setSubmitButtonEnabled(true);
		return true;
	}
	
	
	public @Override
	boolean
	onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent drawer_activity_main in AndroidManifest.xml
		switch(item.getItemId())
		{
			case R.id.menu_action_search:
			{
				// This will execute only if there is not enough space to
				// show the SearchView inside the Action Bar
				this.onSearchRequested();
			} break;
			
			case R.id.menu_action_backup:
			{
				Application.Character[] characters = Application.deserialize_characters();
				if(characters == null)
				{
					Toast.makeText(this, "No database to backup", Toast.LENGTH_SHORT).show();
					break;
				}

				DateFormat df  = DateFormat.getDateTimeInstance();
				String     now = df.format(new Date());
				Application.serialize_characters(now, characters);
			} break;
			
			case R.id.menu_action_init:
			{
				Intent intent = new Intent();
				intent.setType("*/*");
//				intent.setType("application/x-sqlite3");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				
				Intent chooser = Intent.createChooser(intent, "Select a file");
				this.startActivityForResult(chooser, REQUEST_CODE_OPEN_FILE);
			} break;
			
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
		
		return true;
	}
	
	
	private
	boolean
	onNavigationDrawerItemSelected(@NonNull MenuItem item)
	{
		int id = item.getItemId();
		switch(id)
		{
			case R.id.nav_character:
			{
//				this.fragnav_controller.switchTab(0);
			} break;
			
			case R.id.nav_browser:
			{
//				this.fragnav_controller.switchTab(1);
			} break;
		}
		
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
	
	
	@Override
	protected
	void
	onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE_OPEN_FILE && resultCode == RESULT_OK)
		{
			Uri uri = data.getData();
			this.import_database(uri);
		}
	}
	
	
	private
	void
	import_database(Uri database_uri)
	{
		SparseArray<String> error_messages = new SparseArray<>(4);
		error_messages.put(-1, "Error opening source file");
		error_messages.put(-2, "Error creating destination file");
		error_messages.put( 1, "Error reading from source file");
		error_messages.put( 2, "Error writing to destination file");
		
		Application app = (Application) this.getApplication();
		int error;
		try(InputStream reader = this.getContentResolver().openInputStream(database_uri))
		{
			Application.Character[] characters = Application.deserialize_characters();
			
			File output = app.getDatabasePath();
			try(OutputStream writer = new FileOutputStream(output, false))
			{
				error = copy_file(reader, writer);
				if(error == 0)
				{
					if(app.reloadDatabase())
					{
						if(characters != null && characters.length > 0)
							Application.import_characters(characters);
						this.get_current_fragment().refresh();
					}
				}
			}
			catch(IOException e)
			{
				error = -2;
			}
		}
		catch(IOException e)
		{
			error = -2;
		}
		
		if(error != 0)
		{
			Toast.makeText(this, error_messages.get(error), Toast.LENGTH_SHORT).show();
		}
	}
	
	
	public
	int
	get_stack_size()
	{
//		return this.fragnav_controller.getCurrentStack().size();
		return this.fragment_stack.size();
	}
	
	
	public
	Fragment
	get_current_fragment()
	{
//		return (Fragment)this.fragnav_controller.getCurrentFrag();
		if(this.fragment_stack.size() > 0)
			return this.fragment_stack.peek();
		return null;
	}
	
	
	public
	void
	push_fragment(Fragment fragment)
	{
//		this.fragnav_controller.pushFragment(fragment);
		this.getSupportFragmentManager()
		    .beginTransaction()
	        .addToBackStack(null)
	        .replace(R.id.activity_main_content, fragment)
	        .commit();
		this.fragment_stack.push(fragment);
	}
	
	
	public
	void
	pop_fragment()
	{
//		this.pop_fragment(1);
		if(this.get_stack_size() > 0)
		{
			this.getSupportFragmentManager()
		        .popBackStack();
			this.fragment_stack.pop();
		}
	}
	
	
//	public
//	void
//	pop_fragment(int count)
//	{
//		this.fragnav_controller.popFragments(count);
//	}
	
	
//	protected @Override
//	void
//	onSaveInstanceState(Bundle outState)
//	{
//		super.onSaveInstanceState(outState);
//		if(this.fragnav_controller != null)
//			this.fragnav_controller.onSaveInstanceState(outState);
//	}
	
	
//	private
//	android.support.v4.app.Fragment
//	getNavigationRootFragments(int index)
//	{
//		if(index == 0)
//		{
//			return FragmentCharacters.newInstance();
//		}
//		if(index == 1)
//		{
//			return FragmentBrowser.newInstance();
//		}
//		return null;
//	}
	
	
	
	
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState)
//	{
//		super.onRestoreInstanceState(savedInstanceState);
//		Toast.makeText(this, "onRestoreInstanceState(Bundle)", Toast.LENGTH_SHORT).show();
//	}
}
