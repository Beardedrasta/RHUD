package RastaXP;

import RHUD.RHUD_Plugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RHUD_Plugin.class);
		RuneLite.main(args);
	}
}