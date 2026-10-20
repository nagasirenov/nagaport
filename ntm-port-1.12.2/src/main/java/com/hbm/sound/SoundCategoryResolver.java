package com.hbm.sound;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.audio.SoundListSerializer;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Retains the original sounds.json category metadata that native SoundHandler no longer reads. */
public final class SoundCategoryResolver implements IResourceManagerReloadListener {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson SOUND_LISTS = new GsonBuilder().registerTypeAdapter(SoundList.class, new SoundListSerializer()).create();
	private static volatile Map<ResourceLocation, SoundCategory> categories = Collections.emptyMap();

	public static SoundCategory get(ResourceLocation sound) {
		SoundCategory category = categories.get(sound);
		return category == null ? SoundCategory.MASTER : category;
	}

	@Override
	public void onResourceManagerReload(IResourceManager manager) {
		Map<ResourceLocation, SoundCategory> loaded = new LinkedHashMap<>();
		// Diagnostic state is reload-local, never consulted during playback.
		Map<ResourceLocation, String> aliasSources = new LinkedHashMap<>();
		Set<ResourceLocation> warnedAliases = new HashSet<>();
		for(String namespace : manager.getResourceDomains()) {
			ResourceLocation source = new ResourceLocation(namespace, "sounds.json");
			try {
				for(IResource resource : manager.getAllResources(source)) {
					try(IResource closeable = resource;
						InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
						JsonObject entries = new JsonParser().parse(reader).getAsJsonObject();
						Map<ResourceLocation, SoundCategory> next = new LinkedHashMap<>(loaded);
						Map<ResourceLocation, String> nextSources = new LinkedHashMap<>(aliasSources);
						Set<ResourceLocation> nextWarned = new HashSet<>(warnedAliases);
						List<Object[]> aliasWarnings = new ArrayList<>();
						for(Map.Entry<String, JsonElement> entry : entries.entrySet()) {
							// Keep metadata from a malformed pack out of the snapshot too.
							// Native parsing validates the sound definitions; this listener never loads samples.
							SOUND_LISTS.fromJson(entry.getValue(), SoundList.class);
							JsonObject definition = JsonUtils.getJsonObject(entry.getValue(), entry.getKey());
							SoundCategory category = SoundCategory.getByName(JsonUtils.getString(definition, "category", "master"));
							if(category == null) throw new IllegalArgumentException("Invalid sound category: " + entry.getKey());
							ResourceLocation sound = new ResourceLocation(namespace, entry.getKey());
							if(isVanillaCoordinateAlias(sound)) {
								String previous = nextSources.get(sound);
								if(previous == null) nextSources.put(sound, resource.getResourcePackName());
								else if(nextWarned.add(sound)) aliasWarnings.add(new Object[] {sound, previous,
									resource.getResourcePackName(), JsonUtils.getBoolean(definition, "replace", false) ? "replace" : "append"});
							}
							// Original SoundHandler retained the first category when appending samples.
							if(!next.containsKey(sound) || JsonUtils.getBoolean(definition, "replace", false)) next.put(sound, category);
						}
						loaded = next;
						aliasSources = nextSources;
						warnedAliases = nextWarned;
						for(Object[] warning : aliasWarnings) LOGGER.warn(
							"Duplicate vanilla coordinate sound alias {} from packs {} and {} ({}); preserving normal resource order, verify pack/mod compatibility", warning);
					} catch(RuntimeException invalid) {
						LOGGER.warn("Invalid sound category metadata in {} from {}", source, resource.getResourcePackName(), invalid);
					}
				}
			} catch(IOException missing) {
				// A resource domain need not contain sounds.json, just as in SoundHandler.
			}
		}
		categories = Collections.unmodifiableMap(loaded);
	}

	/** Only the fifteen absent vanilla resource aliases introduced by this port. */
	private static boolean isVanillaCoordinateAlias(ResourceLocation sound) {
		if(!"minecraft".equals(sound.getNamespace())) return false;
		switch(sound.getPath()) {
			case "ambient.weather.thunder":
			case "random.explode":
			case "random.fizz":
			case "random.click":
			case "fire.fire":
			case "random.break":
			case "mob.zombie.woodbreak":
			case "mob.endermen.portal":
			case "fireworks.blast":
			case "fire.ignite":
			case "tile.piston.out":
			case "tile.piston.in":
			case "minecart.base":
			case "game.neutral.swim.splash":
			case "random.orb":
				return true;
			default: return false;
		}
	}
}
