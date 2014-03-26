package org.opendrac.automationtool;

import jargs.gnu.CmdLineParser;

/**
 * 
 * @author Erno
 */
public class CmdParser {

	private CmdLineParser parser;
	private boolean makeSched = false;

	private boolean queryScheds = false;
	private String schedIdCancel = null;
	private String schedIdStatus = null;
	private boolean runNow = false;

	private String listScheds = null;
	private String listvals[] = new String[2];
	private String configFile = null;
	private String extensionMinutesStr = null;
	private boolean startService = false;
	private boolean stopService = false;
	private boolean pathInfo = false;
	
	private boolean isQueryPathAvailability = false;
	
	// Method to print how to use the DRAC Automation Tool
	public void printUsage() {
		System.out.println("\nSimple usage: [--info] [--stop] [--start] [{--help}]\n"
				+ "--info    Get info about a service defined by name, user and source and target endpoints from config file.\n"                
                + "--stop Terminate a service defined by name, user and source and target endpoints from config file.\n"
                + "--start (Re)create a service defined by name, user and source and target endpoints from config file.\n"				
                + "-h -? --help    Display this helpfile.");
		System.out.println("\n=========================================================================\n");
		System.out
		        .println("Advanced usage: [--now][--create] [{--cancel [value]}] [--extend <nr. minutes>]\n"
		                + "      [{--status}[value] [{--list} [value1] [value2]] [--info] [{--terminate}] [{--resume}] \n"
		                + "      [{--file} [value]] [{-h,--help}]\n"
		                + "--now     Make a reservation with the current time as startTime.\n"
		                + "--create    Make a reservation with the settings in the opendrac.properties file.\n"
		                + "--cancel  Cancel a  reservation based on the schedule ID value.\n"
		                + "--status  Retrieve the status of a reservation based on the schedule ID value.\n"
		                + "--list    Query a list of reservations based on the startTime and endTime you specify.\n"
		                + "             at the command line, or \"propsfile\" if start and stoptime are used from the properties file.\n"
		                + "--info    Get info about a service defined by name, user and source and target endpoints from config file.\n"		                
		                + "--stop Stop a service defined by name, user and source and target endpoints from config file.\n"
		                + "--start  Start a service defined by name, user and source and target endpoints from config file.\n"
		                + "--extend Specify the nr. of minutes by which you want to extend the service defined by name, user and source and target endpoints from config file.\n"
		                + "--file    Specify a configuration file that you wish to use.\n"
		                + "--query   Query a path availability based on startTime, endTime and lot's of other thingies you specify.\n"
		                + "-h -? --help    Display this helpfile.");
	}

	// Method to parse commandline arguments
	public void parse(String[] args) throws CmdLineParser.OptionException {

		// create parser and options we recognize for parsing
		parser = new CmdLineParser();
		CmdLineParser.Option makeScheduleOption = parser.addBooleanOption("create");
		CmdLineParser.Option cancelScheduleOption = parser.addStringOption("cancel");
		CmdLineParser.Option scheduleStatusOption = parser.addStringOption("status");
		CmdLineParser.Option helpOption = parser.addBooleanOption('h', "help");
		CmdLineParser.Option helpAltOption = parser.addBooleanOption('?', "Help");

		CmdLineParser.Option listSchedulesOption = parser.addStringOption("list");
		
		CmdLineParser.Option startPathOption = parser.addBooleanOption("start");
		CmdLineParser.Option stopPathOption = parser.addBooleanOption("stop");
		
		CmdLineParser.Option pathInfoOption = parser.addBooleanOption("info");
		
		CmdLineParser.Option setConfigOption = parser.addStringOption("file");
		CmdLineParser.Option extendPathOption = parser.addStringOption("extend");		
		CmdLineParser.Option makeScheduleNow = parser.addBooleanOption("now");
		
		CmdLineParser.Option queryPathAvailabilityOption = parser.addBooleanOption("query");
		
		

		// Attempt to parse the command line arguments
		try {
			parser.parse(args);
			// Create standard objects from commandline arguments which will be
			// used
			// to
			// compare if they were used(true) or not.
			Object makeTimedScheduleCommand = parser.getOptionValue(makeScheduleOption);
			Object helpCommand = parser.getOptionValue(helpOption);
			Object helpCommandAlt = parser.getOptionValue(helpAltOption);
			Object makeScheduleNowCommand = parser.getOptionValue(makeScheduleNow);
			
			Object startPathCommand = parser.getOptionValue(startPathOption);
			Object stopPathCommand = parser.getOptionValue(stopPathOption);
			//Object extendPathCommand = parser.getOptionValue(extendPathOption);
			Object pathInfoCommand = parser.getOptionValue(pathInfoOption);
			
			
			Object queryPathAvailabilitycommand = parser.getOptionValue(queryPathAvailabilityOption);
			
			// Object ls2 =(Object)parser.getOptionValue(zeta);
			// Compare the arguments value to check if they were uses or not.
			// Based on this we make a choice whether to make a schedule, list
			// schedules
			// check schedule status, and or cancel a schedule
			try {
				if (makeTimedScheduleCommand.equals(true)) {
					// make this boolean true
					makeSched = true;
				}
			} catch (Exception e) {
				//ignore exception: argument was null and thus not used.
			}

			schedIdCancel = (String) parser.getOptionValue(cancelScheduleOption);
			schedIdStatus = (String) parser.getOptionValue(scheduleStatusOption);
			configFile = (String) parser.getOptionValue(setConfigOption);
			extensionMinutesStr = (String) parser.getOptionValue(extendPathOption);			
			listScheds = (String) parser.getOptionValue(listSchedulesOption);

			try {
				if (listScheds != null) {
					listvals = listScheds.split("_");
				}
			} catch (Exception e) {
				System.out.println();
			}
			try {
				if (helpCommand.equals(true) || helpCommandAlt.equals(true)) {
					printUsage();
				}
			} catch (Exception e) {
				//ignore exception: argument was null and thus not used.
			}

			try {
				if (makeScheduleNowCommand.equals(true)) {
					runNow = true;
				}
			} catch (Exception e) {
				//ignore exception: argument was null and thus not used.
			}
			
			try {
				if (startPathCommand.equals(true)) {
					startService = true;
				}
			} catch (Exception e) {
				//ignore exception: argument was null and thus not used.
			}	
			try {
				if (stopPathCommand.equals(true)) {
					stopService = true;
				}
			} catch (Exception e) {
				//ignore exception: argument was null and thus not used.
			}
		
			
			try {
				if (pathInfoCommand.equals(true)) {
					pathInfo = true;
				}
			} catch (Exception e) {
				//ignore exception: argument was null and thus not used.
			}
			
			try {
        if (queryPathAvailabilitycommand.equals(true)) {
          isQueryPathAvailability = true;
        }
      } catch (Exception e) {
        //ignore exception: argument was null and thus not used.
      }   
			
			

		}
		// Catch a CMDLineParser exception & display the error & call the
		// printUsage
		// method to display how the application should be used, then terminate
		// app.
		catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			printUsage();
			System.exit(1);
		}
	}

	public boolean isMakeSched() {
		return makeSched;
	}

	public boolean isQueryScheds() {
		return queryScheds;
	}

	public String getSchedIdCancel() {
		return schedIdCancel;
	}

	public String getSchedIdStatus() {
		return schedIdStatus;
	}

	public boolean isRunNow() {
		return runNow;
	}

	public String getListScheds() {
		return listScheds;
	}

	public String[] getListvals() {
		return listvals;
	}

	public String getConfigFile() {
		return configFile;
	}

	public boolean isResumeService() {
    	return startService;
    }

	public boolean isTerminateService() {
    	return stopService;
    }

	public boolean isPathInfo() {
    	return pathInfo;
    }
	public boolean isExtendService() {
		return getExtensionMinutes()>0;
	}
	public int getExtensionMinutes(){
		if(extensionMinutesStr == null){
			return -1;
		}else{
			return Integer.parseInt(extensionMinutesStr);
		}
	}

  public boolean isQueryPathAvailability() {
    return isQueryPathAvailability;
  }
}
