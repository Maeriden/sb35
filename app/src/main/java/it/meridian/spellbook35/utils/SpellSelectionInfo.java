package it.meridian.spellbook35.utils;


public class SpellSelectionInfo
{
	public final String source_name;
	public final int    spell_level;
	public final String spell_name;
	public final int    caster_level;
	
	
	public SpellSelectionInfo(String source_name, int spell_level, String spell_name)
	{
		this.source_name  = source_name;
		this.spell_level  = spell_level;
		this.spell_name   = spell_name;
		this.caster_level = 0;
	}
}
