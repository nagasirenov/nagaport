package com.hbm.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ChatBuilder {

	private TextComponentString text;
	private TextComponentBase last;

	private ChatBuilder(String text) {
		this.text = new TextComponentString(text);
		this.last = this.text;
	}

	public static ChatBuilder start(String text) {
		ChatBuilder builder = new ChatBuilder(text);
		return builder;
	}

	public static ChatBuilder startTranslation(String text, Object... o) {
		ChatBuilder builder = new ChatBuilder("").nextTranslation(text, o);
		return builder;
	}

	public ChatBuilder next(String text) {
		TextComponentString append = new TextComponentString(text);
		this.last.appendSibling(append);
		this.last = append;
		return this;
	}

	public ChatBuilder nextTranslation(String text, Object... o) {
		TextComponentTranslation append = new TextComponentTranslation(text, o);
		this.last.appendSibling(append);
		this.last = append;
		return this;
	}

	public ChatBuilder color(TextFormatting format) {
		Style style = this.last.getStyle();
		style.setColor(format);
		return this;
	}

	/** Will recursively go over all ITextComponents added to the root and then set the style */
	public ChatBuilder colorAll(TextFormatting format) {

		List list = new ArrayList();
		list.add(text);

		ListIterator it = list.listIterator();

		while(it.hasNext()) {
			Object o = it.next();
			ITextComponent component = (ITextComponent) o;
			component.getStyle().setColor(format);
			for(Object s : component.getSiblings()) it.add(s);
		}

		return this;
	}

	public TextComponentString flush() {
		return this.text;
	}
}
