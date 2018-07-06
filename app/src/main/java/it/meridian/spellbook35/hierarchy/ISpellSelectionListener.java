package it.meridian.spellbook35.hierarchy;

import java.util.Collection;

import it.meridian.spellbook35.utils.SpellSelectionInfo;


public interface ISpellSelectionListener
{
	void
	on_spell_selection_accept(Collection<SpellSelectionInfo> selection);
	
	void
	on_spell_selection_cancel();
}
