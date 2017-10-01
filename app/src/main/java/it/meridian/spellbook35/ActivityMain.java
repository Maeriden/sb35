package it.meridian.spellbook35;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	static public final int REQUEST_CODE_OPEN_FILE = 1;
	
	private FragmentCharacters frag_character_list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		this.setSupportActionBar(toolbar);
		
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		
		this.frag_character_list = new FragmentCharacters();
		this.getSupportFragmentManager().beginTransaction()
				.replace(R.id.activity_main_content, this.frag_character_list)
//				.addToBackStack(null)
				.commit();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	}
	
	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if(drawer.isDrawerOpen(GravityCompat.START))
		{
			drawer.closeDrawer(GravityCompat.START);
		}
		else
		{
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		switch(id)
		{
			case R.id.menu_action_settings:
			{
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
			} break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item)
	{
		// Handle navigation view item clicks here.
		int id = item.getItemId();
		switch(id)
		{
			case R.id.nav_character:
			{
			
			}
			break;
			
			case R.id.nav_browser:
			{
			
			}
			break;
		}
		
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE_OPEN_FILE)
		{
			if(resultCode == RESULT_OK)
			{
				Uri uri = data.getData();
				
				try(InputStream reader = this.getContentResolver().openInputStream(uri))
				{
					File output = this.getApplication().getDatabasePath(Application.DATABASE_NAME);
					try(FileOutputStream writer = new FileOutputStream(output, false))
					{
						byte[] buffer = new byte[4 * 1024];
						int read_total = 0;
						int read_count = 0;
						boolean success = false;
						while(true)
						{
							try
							{
								read_count = reader.read(buffer, 0, buffer.length);
							}
							catch(IOException e)
							{
								Toast.makeText(this, "Error reading source file", Toast.LENGTH_SHORT).show();
								break;
							}
							
							if(read_count == -1)
							{
								success = true;
								break;
							}
							
							try
							{
								writer.write(buffer, 0, read_count);
								read_total += read_count;
							}
							catch(IOException e)
							{
								Toast.makeText(this, "Error writing to destination file", Toast.LENGTH_SHORT).show();
								break;
							}
						}
						
						if(success)
						{
							Application app = (Application)this.getApplication();
							if(app.reloadDatabase())
							{
								this.frag_character_list.refresh();
							}
						}
					}
					catch(IOException e)
					{
						Toast.makeText(this, "Error opening destination file", Toast.LENGTH_SHORT).show();
					}
				}
				catch(FileNotFoundException e)
				{
					Toast.makeText(this, "Error: could not find source file", Toast.LENGTH_SHORT).show();
				}
				catch(IOException e)
				{
					Toast.makeText(this, "Error opening source file", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}
