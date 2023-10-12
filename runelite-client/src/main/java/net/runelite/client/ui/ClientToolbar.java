/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.NavigationButtonAdded;
import net.runelite.client.events.NavigationButtonRemoved;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Plugin toolbar buttons holder.
 */
@Singleton
@Slf4j
public class ClientToolbar
{
	private final EventBus eventBus;

	private final PluginManager pluginManager;
	private final Set<NavigationButton> buttons = new HashSet<>();

	@Inject
	private ClientToolbar(final EventBus eventBus, final PluginManager pluginManager)
	{
		this.eventBus = eventBus;
		this.pluginManager = pluginManager;
	}

	/**
	 * Add navigation.
	 *
	 * @param button the button
	 */
	public void addNavigation(final NavigationButton button, Class<? extends Plugin> pluginClazz)
	{
		button.setOwningPlugin(pluginClazz);
		if (buttons.contains(button))
		{
			return;
		}

		if (buttons.add(button))
		{
			if (!pluginManager.arePluginPanelsHidden(pluginClazz)) {
				eventBus.post(new NavigationButtonAdded(button));
			}
		}
	}

	public void addNavigation(final NavigationButton button)
	{
		// Get the plugin class from the stack (eww)
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			try {
				Class<?> elementClazz = Class.forName(e.getClassName());
				if (Plugin.class.isAssignableFrom(elementClazz)) {
					addNavigation(button, (Class<? extends Plugin>) elementClazz);
					return;
				}
			} catch (ClassNotFoundException ignored) {
			}
		}
		addNavigation(button, null);
	}

	/**
	 * Remove navigation.
	 *
	 * @param button the button
	 */
	public void removeNavigation(final NavigationButton button)
	{
		if (buttons.remove(button))
		{
			eventBus.post(new NavigationButtonRemoved(button));
		}
	}

	/**
	 * Get all current navigation buttons
	 */
	public Set<NavigationButton> getNavigationButtons() {
		return buttons;
	}

	/**
	 * Hide all nav buttons associated with the provided plugin
	 *
	 * @param owningPlugin the Plugin class of the plugin
	 */
	public void togglePanelGroup(Class<? extends Plugin> owningPlugin) {
		if (owningPlugin == null) {
			return;
		}
		for (NavigationButton button : buttons) {
			if (owningPlugin.equals(button.getOwningPlugin())) {
				button.setHidden(!button.isHidden());
				eventBus.post(button.isHidden() ? new NavigationButtonRemoved(button) : new NavigationButtonAdded(button));
			}
		}
	}
}
