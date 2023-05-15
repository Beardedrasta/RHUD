package RastaXP;

import com.RastaXP.RastaXPPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RastaXPPlugin.class);
		RuneLite.main(args);
	}
}