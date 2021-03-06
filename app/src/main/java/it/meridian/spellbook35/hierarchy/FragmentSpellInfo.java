package it.meridian.spellbook35.hierarchy;

import android.app.ActionBar;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.utils.Utils;


public class FragmentSpellInfo extends it.meridian.spellbook35.Fragment
{
	static private final String ARG_SPELL_NAME = "spell_name";
	
	static private final String SELECT_QUERY =
			"SELECT spell.name          AS name,         " +
			"       spell.book          AS book,         " +
			"       spell.page          AS page,         " +
			"       spell.school        AS school,       " +
			"       spell.subschool     AS subschool,    " +
			"       spell.descriptors   AS descriptors,  " +
			"       spell.components    AS components,   " +
			"       spell.cast_time     AS cast_time,    " +
			"       spell.range         AS range,        " +
			"       spell.effect_type   AS effect_type,  " +
			"       spell.effect        AS effect,       " +
			"       spell.duration      AS duration,     " +
			"       spell.saving_throw  AS saving_throw, " +
			"       spell.resistance    AS resistance,   " +
			"       spell.fluff         AS fluff,        " +
			"       spell.description   AS description   " +
			"  FROM spell_detail spell                   " +
			" WHERE name = ?";
	
	static private final String SELECT_LEVELS_QUERY =
			"SELECT source.name  AS source,     " +
			"       spells.level AS level       " +
			"  FROM source        source,       " +
			"       source_spells spells        " +
			" WHERE source.name = spells.source " +
			"   AND source.disabled = 0         " +
			"   AND spells.spell = ?";
	
	static private final String HTML =
			"<html>                           " +
			"   <head>                        " +
			"       <style type=\"text/css\"> " +
			"           html, body {          " +
			"               width:100%%;      " +
			"               height: 100%%;    " +
			"               margin: 0px;      " +
			"               padding: 0px;     " +
			"           }                     " +
			"       </style>                  " +
			"   </head>                       " +
			"   <body>                        " +
			"       %s                        " +
			"   </body>                       " +
			"</html>";
	
	
	static public Fragment
	newInstance(@NonNull String spell_name)
	{
		FragmentSpellInfo frag = new FragmentSpellInfo();
		{
			Bundle args = new Bundle(1);
			args.putString(ARG_SPELL_NAME, spell_name);
			frag.setArguments(args);
		}
		return frag;
	}
	
	
	private String spell_name;
	
	private TextView textview_book;
	private TextView textview_source;
	private TextView textview_school;
	private TextView textview_descriptors;
	private TextView textview_components;
	private TextView textview_cast_time;
	private TextView textview_range;
	private TextView textview_effect_type;
	private TextView textview_effect;
	private TextView textview_duration;
	private TextView textview_save;
	private TextView textview_resistance;
	private TextView textview_flavor;
	private WebView  webview_desc;
	
	private TableRow row_book;
	private TableRow row_source;
	private TableRow row_school;
	private TableRow row_descriptor;
	private TableRow row_components;
	private TableRow row_cast_time;
	private TableRow row_range;
	private TableRow row_effect;
	private TableRow row_duration;
	private TableRow row_save;
	private TableRow row_resistance;
	
	
	
	
	@Override public
	void
	onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		this.spell_name = this.getArguments().getString(ARG_SPELL_NAME);
	}
	
	
	@Override public
	@Nullable View
	onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{
		NestedScrollView scroll_view = (NestedScrollView)inflater.inflate(R.layout.fragment_spell_info, container, false);
		
		this.textview_book        = scroll_view.findViewById(R.id.spell_book);
		this.textview_source      = scroll_view.findViewById(R.id.spell_levels);
		this.textview_school      = scroll_view.findViewById(R.id.spell_school);
		this.textview_descriptors = scroll_view.findViewById(R.id.spell_descriptor);
		this.textview_components  = scroll_view.findViewById(R.id.spell_components);
		this.textview_cast_time   = scroll_view.findViewById(R.id.spell_cast_time);
		this.textview_range       = scroll_view.findViewById(R.id.spell_range);
		this.textview_effect_type = scroll_view.findViewById(R.id.spell_effect_type);
		this.textview_effect      = scroll_view.findViewById(R.id.spell_effect);
		this.textview_duration    = scroll_view.findViewById(R.id.spell_duration);
		this.textview_save        = scroll_view.findViewById(R.id.spell_saving_throw);
		this.textview_resistance  = scroll_view.findViewById(R.id.spell_resistance);
		this.textview_flavor      = scroll_view.findViewById(R.id.spell_flavor);
		this.webview_desc         = scroll_view.findViewById(R.id.spell_desc);
		
		this.row_book       = scroll_view.findViewById(R.id.row_spell_0);
		this.row_source     = scroll_view.findViewById(R.id.row_spell_B);
		this.row_school     = scroll_view.findViewById(R.id.row_spell_1);
		this.row_descriptor = scroll_view.findViewById(R.id.row_spell_2);
		this.row_components = scroll_view.findViewById(R.id.row_spell_3);
		this.row_cast_time  = scroll_view.findViewById(R.id.row_spell_4);
		this.row_range      = scroll_view.findViewById(R.id.row_spell_5);
		this.row_effect     = scroll_view.findViewById(R.id.row_spell_7);
		this.row_duration   = scroll_view.findViewById(R.id.row_spell_8);
		this.row_save       = scroll_view.findViewById(R.id.row_spell_9);
		this.row_resistance = scroll_view.findViewById(R.id.row_spell_A);
		
		WebViewClient webview_client = new WebViewClient(this);
		this.webview_desc.setWebViewClient(webview_client);
		this.webview_desc.getSettings().setTextZoom(80);
		this.webview_desc.setBackgroundColor(Color.TRANSPARENT);
		
		return scroll_view;
	}
	
	
	@Override
	public
	void
	onStart()
	{
		super.onStart();
		if(this.spell_name == null)
		{
			this.clear_view();
			Toast.makeText(this.getContext(), "ERROR: spell is NULL", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Cursor cursor = Application.query(SELECT_QUERY, this.spell_name);
		cursor.moveToFirst();
		if(cursor.getCount() < 1)
		{
			this.clear_view();
			Toast.makeText(this.getContext(), "Spell " + this.spell_name + " does not exist", Toast.LENGTH_SHORT).show();
			return;
		}
		
		this.getActivity().setTitle(this.spell_name);
		
		{
			StringBuilder levels = new StringBuilder();
			Cursor sources = Application.query(SELECT_LEVELS_QUERY, this.spell_name);
			for(int i = 0; i < sources.getCount(); ++i)
			{
				sources.moveToNext();
				String source = Utils.CursorGetString(sources, "source");
				int    level  = Utils.CursorGetInt(sources, "level");
				levels.append(source).append(" ").append(level);
				if(i < sources.getCount() - 1)
					levels.append(", ");
			}
			sources.close();
			this.textview_source.setText(levels);
		}
		
		{
			String book = Utils.CursorGetString(cursor, "book");
			String page = Utils.CursorGetString(cursor, "page");
			if(page != null)
				book += " " + page;
			this.row_book.setVisibility(book != null ? View.VISIBLE : View.GONE);
			this.textview_book.setText(book);
		}
		
		{
			String school = Utils.CursorGetString(cursor, "school");
			String subsch = Utils.CursorGetString(cursor, "subschool");
			if(subsch != null)
				school += " (" + subsch + ")";
			this.row_school.setVisibility(school != null ? View.VISIBLE : View.GONE);
			this.textview_school.setText(school);
		}
		
		{
			String descriptor = Utils.CursorGetString(cursor, "descriptors");
			this.row_descriptor.setVisibility(descriptor != null ? View.VISIBLE : View.GONE);
			this.textview_descriptors.setText(descriptor);
		}
		
		{
			String components = Utils.CursorGetString(cursor, "components");
			this.row_components.setVisibility(components != null ? View.VISIBLE : View.GONE);
			this.textview_components.setText(components);
		}
		
		{
			String cast_time = Utils.CursorGetString(cursor, "cast_time");
			this.row_cast_time.setVisibility(cast_time != null ? View.VISIBLE : View.GONE);
			this.textview_cast_time.setText(cast_time);
		}
		
		{
			String range = Utils.CursorGetString(cursor, "range");
			this.row_range.setVisibility(range != null ? View.VISIBLE : View.GONE);
			this.textview_range.setText(range);
		}
		
		{
			String eff_type = Utils.CursorGetString(cursor, "effect_type");
			String effect   = Utils.CursorGetString(cursor, "effect");
			this.row_effect.setVisibility(eff_type != null && effect != null ? View.VISIBLE : View.GONE);
			this.textview_effect.setText(effect);
			this.textview_effect_type.setText(eff_type);
		}
		
		{
			String duration = Utils.CursorGetString(cursor, "duration");
			this.row_duration.setVisibility(duration != null ? View.VISIBLE : View.GONE);
			this.textview_duration.setText(duration);
		}
		
		{
			String save = Utils.CursorGetString(cursor, "saving_throw");
			this.row_save.setVisibility(save != null ? View.VISIBLE : View.GONE);
			this.textview_save.setText(save);
		}
		
		{
			String resist = Utils.CursorGetString(cursor, "resistance");
			this.row_resistance.setVisibility(resist != null ? View.VISIBLE : View.GONE);
			this.textview_resistance.setText(resist);
		}
		
		{
			String fluff = Utils.CursorGetString(cursor, "fluff");
			this.textview_flavor.setVisibility(fluff != null ? View.VISIBLE : View.GONE);
			this.textview_flavor.setText(Html.fromHtml(fluff != null ? fluff : ""));
		}
		
		{
			String desc = Utils.CursorGetString(cursor, "description");
			if(desc != null && desc.length() > 0)
			{
//					desc = desc.replace("\n", "<br/>");
				desc = String.format(HTML, desc);
			}
			
			this.webview_desc.setVisibility(desc != null ? View.VISIBLE : View.GONE);
			this.webview_desc.loadDataWithBaseURL("spellbook://0.0.0.0/", desc, null, "UTF-8", null);
		}
		
		cursor.close();
	}
	
	
	private void clear_view()
	{
		ActionBar action_bar = this.getActivity().getActionBar();
		if(action_bar != null)
			action_bar.setTitle("ERROR");
		
		this.textview_book.setText("");
		this.textview_source.setText("");
		this.textview_school.setText("");
		this.textview_descriptors.setText("");
		this.textview_components.setText("");
		this.textview_cast_time.setText("");
		this.textview_range.setText("");
		this.textview_effect_type.setText("");
		this.textview_effect.setText("");
		this.textview_duration.setText("");
		this.textview_save.setText("");
		this.textview_resistance.setText("");
		this.textview_flavor.setText("");
		this.webview_desc.loadData("", "text/html; charset=utf-8", "utf-8");
	}
	
	
	
	
	static private class WebViewClient extends android.webkit.WebViewClient
	{
		private FragmentSpellInfo fragment;
		
		WebViewClient(FragmentSpellInfo fragment)
		{
			this.fragment = fragment;
		}
		
		@SuppressWarnings("deprecation")
		public @Override
		boolean
		shouldOverrideUrlLoading(WebView view, String url)
		{
			String spell_name = url.substring("spellbook://0.0.0.0/".length());
			try
			{
				spell_name = URLDecoder.decode(spell_name, "UTF-8");
			}
			catch(UnsupportedEncodingException ignored) {}
			
			Cursor spells = Application.query("SELECT 1 FROM spell WHERE name = ? LIMIT 1", spell_name);
			if(spells.getCount() > 0)
			{
				Fragment fragment = FragmentSpellInfo.newInstance(spell_name);
				
				this.fragment.activity_main().push_fragment(fragment);
			}
			else
			{
				Toast.makeText(this.fragment.getContext(), "Spell " + spell_name + " does not exist", Toast.LENGTH_SHORT).show();
			}
			
			return true;
		}
	}
}
