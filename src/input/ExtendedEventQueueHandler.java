package input;

import java.util.ArrayList;
import java.util.List;

import core.Settings;

/** Handler for managing extended event queues. It only supports
 * external events queue, in particular those obtained by input.CPEventsReader. 
 * 
 * In the constructor there's the specification of ExtendedExternalEventsQueue, called in order to 
 * obtain an event queue from the external Contact Plan file */

public class ExtendedEventQueueHandler extends EventQueueHandler
{
	private List<EventQueue> queues;

	public ExtendedEventQueueHandler()
	{
		super();
		
		Settings settings = new Settings(SETTINGS_NAMESPACE);
		int nrof = settings.getInt(NROF_SETTING);
	
		queues = new ArrayList<EventQueue>();
		
		for (int i=1; i <= nrof; i++)
		{
			Settings s = new Settings(SETTINGS_NAMESPACE + i);

			if (s.contains(PATH_SETTING))	// external events file 
			{ 
				int preload = 0;
				String path = "";
				if (s.contains(PRELOAD_SETTING))
				{
					preload = s.getInt(PRELOAD_SETTING);
				}
				
				path = s.getSetting(PATH_SETTING);
				queues.add(new ExtendedExternalEventsQueue(path, preload));
			}
			else if (s.contains(CLASS_SETTING))		// event generator class
			{ 
				String className = CLASS_PACKAGE + "." +s.getSetting(CLASS_SETTING);
				EventQueue eq = (EventQueue)s.createIntializedObject(className);

				queues.add(eq);
			}
		}
	}
}
