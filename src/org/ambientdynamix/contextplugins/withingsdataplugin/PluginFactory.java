/*
 * This Plugin is licensed under GNU GPL v3 
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 *Author: Luaks Ruge
 */
package org.ambientdynamix.contextplugins.withingsdataplugin;

import org.ambientdynamix.contextplugin.api.*;

public class PluginFactory extends ContextPluginRuntimeFactory 
{
    public PluginFactory() 
    {
    	super(WithingsDataPluginRuntime.class, null, ConfigurationViewFactory.class);
    }
}