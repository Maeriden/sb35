package it.meridian.spellbook35.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ncapdevi.fragnav.FragNavController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.fragments.FragmentCharacters;
import it.meridian.spellbook35.fragments.FragmentSpellInfo;
import it.meridian.spellbook35.fragments.FragmentSpellSources;

import static it.meridian.spellbook35.utils.Utils.*;


public class ActivityMain extends android.support.v7.app.AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	static public final int REQUEST_CODE_OPEN_FILE = 1;
	
	
	public FragNavController  fragnav_controller;
	
	
	@Override
	protected
	void
	onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main_drawer);
		Toolbar toolbar = findViewById(R.id.toolbar);
		this.setSupportActionBar(toolbar);
		
		
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		
		
		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		
		String browser_title = this.getString(R.string.browse_spells);
		List<Fragment> roots = Arrays.asList(FragmentCharacters.newInstance(),
		                                     FragmentSpellSources.newInstance(browser_title, null, null, -1));
		this.fragnav_controller = FragNavController.newBuilder(savedInstanceState,
		                                                       this.getSupportFragmentManager(),
		                                                       R.id.activity_main_content)
		                                           .rootFragments(roots)
		                                           .build();
//		this.getSupportFragmentManager()
//		    .beginTransaction()
//		    .replace(R.id.activity_main_content, this.frag_character_list)
//		    .commit();
	}
	
	
	@Override
	protected
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
	
	
	@Override
	protected
	void
	onStart()
	{
		super.onStart();
	}
	
	
	@Override
	public
	void
	onBackPressed()
	{
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		if(drawer.isDrawerOpen(GravityCompat.START))
		{
			drawer.closeDrawer(GravityCompat.START);
		}
		else
		if(this.fragnav_controller.getCurrentStack().size() > 1)
		{
			this.pop_fragment();
		}
		else
		{
			super.onBackPressed();
		}
	}
	
	
	@Override
	public
	boolean
	onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
//		this.getMenuInflater().inflate(R.menu.options_activity_main, menu);
		return true;
	}
	
	
	@Override
	public
	boolean
	onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent drawer_activity_main in AndroidManifest.xml.
		switch(item.getItemId())
		{
			case R.id.menu_action_settings:
			{
				Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
				return true;
			}
			
			case R.id.menu_action_import:
			{
				Intent intent = new Intent();
				intent.setType("*/*");
//				intent.setType("application/x-sqlite3");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				
				Intent chooser = Intent.createChooser(intent, "Select a file");
				this.startActivityForResult(chooser, REQUEST_CODE_OPEN_FILE);
			}
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public
	boolean
	onNavigationItemSelected(@NonNull MenuItem item)
	{
		// Handle navigation view item clicks here.
		int id = item.getItemId();
		switch(id)
		{
			case R.id.nav_character:
			{
				this.fragnav_controller.switchTab(0);
			} break;
			
			case R.id.nav_browser:
			{
				this.fragnav_controller.switchTab(1);
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
			Application.Character[] characters = Application.backup_characters();
			
			File output = app.getDatabasePath();
			try(OutputStream writer = new FileOutputStream(output, false))
			{
				error = copy_file(reader, writer);
				if(error == 0)
				{
					if(app.reloadDatabase())
					{
						if(characters != null && characters.length > 0)
							Application.restore_characters(characters);
						
						// TODO: Test stack reset
						String browser_title = this.getString(R.string.browse_spells);
						List<Fragment> roots = Arrays.asList(FragmentCharacters.newInstance(),
						                                     FragmentSpellSources.newInstance(browser_title, null, null, -1));
						this.fragnav_controller.clearStack();
						this.fragnav_controller = FragNavController.newBuilder(null,
						                                                       this.getSupportFragmentManager(),
						                                                       R.id.activity_main_content)
						                                           .rootFragments(roots)
						                                           .build();
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
	void
	push_fragment(Fragment fragment)
	{
		this.fragnav_controller.pushFragment(fragment);
	}
	
	
	public
	void
	pop_fragment()
	{
		this.pop_fragment(1);
	}
	
	
	public
	void
	pop_fragment(int count)
	{
		this.fragnav_controller.popFragments(count);
	}
	
	
	@Override
	protected
	void
	onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(this.fragnav_controller != null)
			this.fragnav_controller.onSaveInstanceState(outState);
	}
}
