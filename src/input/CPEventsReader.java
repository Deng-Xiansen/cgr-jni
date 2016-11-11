package input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import core.SimError;

/** Class used for ContactPlan reading from an external file, in order to
 * create a list of external events used by the simulation */

public class CPEventsReader extends StandardEventsReader
{
	/** Identifier for ContactPlan Range between nodes */
	public static final String RANGE = "range";
	/** Identifier for ContactPlan Contact between nodes */
	public static final String CONTACT = "contact";
	/** Anticipated distance between nodes in the interval, in Light Seconds (for Earth
	 * transmissions it's always 1) */
	public static final int distance = 1;
	
	/* BufferedReader needed for file's reading */
	private BufferedReader reader;
	
	/** Error messages */
	private static final String timeError = "Start time must be lower than end time of contact";
	private static final String rangeHostError = "Host1 must be lower than Host2 for bidirectional communication";
	
	/** Constructor 
	 * 
	 * @param eventsFile: the file read in order to obtain the Contact Plan.
	 * It's possible to specify either a defined file (with its absolute path) or a 
	 * filePath in the settings.txt file. 
	 * **it calls the superclass constructor** */
	public CPEventsReader(File eventsFile)
	{
		super(eventsFile);	
		try
		{
			this.reader = new BufferedReader(new FileReader(eventsFile));
		} 
		catch (FileNotFoundException e) 
		{
			throw new SimError(e.getMessage(),e);
		}
	}
	
	@Override
	public List<ExternalEvent> readEvents(int nrof) 
	{
		ArrayList<ExternalEvent> events = new ArrayList<ExternalEvent>(nrof);
		int eventsRead = 0;
		
		String line;
		try
		{
			while (((line = this.reader.readLine()) != null) && eventsRead < nrof)
			{
				StringTokenizer stk = new StringTokenizer(line, "\t ");
				
				String letter = stk.nextToken();
				if (!letter.equals("a")) throw new IllegalArgumentException("Must be ADD (a)");
				
				String action = stk.nextToken();
				
				/** Do not perform anything: unused 
				 * TODO use "range" in order to specify further informations about Contact Plan */
				if (action.equals(RANGE))
				{
					String startTimeStr = stk.nextToken();
					String endTimeStr = stk.nextToken();
					String host1Addr = stk.nextToken();
					String host2Addr = stk.nextToken();
					String distStr = stk.nextToken();
					
					startTimeStr = startTimeStr.substring(1);
					endTimeStr = endTimeStr.substring(1);
					
					long startTime = Long.parseLong(startTimeStr);
					long endTime = Long.parseLong(endTimeStr);
					if (startTime > endTime) throw new IllegalArgumentException(timeError);
					
					int host1 = Integer.parseInt(host1Addr);
					int host2 = Integer.parseInt(host2Addr);
					if (host2 < host1) 
						throw new IllegalArgumentException(rangeHostError);
					if (host1 < 0 && host2 < 0)
						throw new SimError("Unknown Hosts");
					
					int dist = Integer.parseInt(distStr);
					if (dist != distance) 
						throw new IllegalArgumentException("Must be 1 for terrestrial transmissions");
				}
				else if (action.equals(CONTACT))
				{
					/** Parameters of a Contact Plan, read with StringTokenizer
					 * StartTime of contact, in simulation time 
					 * EndTime of contact, in simulation time
					 * Host1 and Host2: DTNHosts involved in the connection event
					 * TransmitSpeed of the network interface used (default: bluetooth) */ 
					
					String startTimeStr = stk.nextToken();
					String endTimeStr = stk.nextToken();
					String host1Addr = stk.nextToken();
					String host2Addr = stk.nextToken();
					String transmitSpeedStr = stk.nextToken();
					
					startTimeStr = startTimeStr.substring(1);
					endTimeStr = endTimeStr.substring(1);
					
					long startTime = Long.parseLong(startTimeStr);
					long endTime = Long.parseLong(endTimeStr);
					if (startTime > endTime) throw new IllegalArgumentException(timeError);
					
					int host1 = Integer.parseInt(host1Addr)-1;
					int host2 = Integer.parseInt(host2Addr)-1;
					if (host1 < 0 && host2 < 0)
						throw new SimError("Unknown Hosts");
					
					int transmitSpeed = Integer.parseInt(transmitSpeedStr);
					if (transmitSpeed < 0)
						throw new IllegalArgumentException("TransmitSpeed must be higher than zero");
					
					events.add(new CPConnectionEvent(host1, host2, null, true, startTime, transmitSpeed));
					events.add(new CPConnectionEvent(host1, host2, null, false, endTime, transmitSpeed));
				}
				else throw new SimError("Unknown Action" +action+ " specified");
			}
		}
		catch (IOException e)
		{
			throw new SimError("Reading from external file failed!");
		}
		return events;
	}

	@Override
	public void close()
	{
		try
		{
			this.reader.close();
		}
		catch (IOException e) {}
	}
}
