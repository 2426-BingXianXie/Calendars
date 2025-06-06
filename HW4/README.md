# Virtual Calendar Application
A command-line calendar application that mimics features found in widely-used 
calendar apps such as Google Calendar. The application supports both single 
events and recurring event series with comprehensive editing capabilities.

# Work Distribution
## Joshua Chan
- Setup a basic structure of files
- revised the methods and debugged for better development
- Mostly Controller, Enum property, Command Patterns package
- Testing Controller packages, figure out the usage of mocks


## BingXian(Eason) Xie
- Implements the methods needed
- Mostly Model and View package
- Testing Model and View

# How to Run the Program
## Prerequisites

- Java 11
- Command line terminal

## Compilation
### Navigate to your project directory
cd path/to/your/project

### Compile all Java files
javac -d out -cp src src/calendar/*.java src/calendar/*/*.java src/calendar/*/*/*.java

## Running the Application
### Interactive Mode
java -cp out calendar.CalendarRunner --mode interactive

- Starts an interactive session where you can type commands
- Type menu to see available commands
- Type exit, quit, or q to terminate the program

### Headless Mode
java -cp out calendar.CalendarRunner --mode headless res/valid_commands.txt

- Executes commands from a text file
- File must end with exit command
- Program will display error if file doesn't end with exit

## Example Commands
#### Single event
create event "Team Meeting" from 2025-06-04T09:00 to 2025-06-04T10:00

#### All-day event
create event "Independence Day" on 2025-07-04

#### Recurring event series (count-based)
create event "Weekly Standup" from 2025-06-02T09:00 to 2025-06-02T10:00 repeats MW for 5 times

#### Recurring event series (date-based)
create event "Daily Workout" on 2025-06-01 repeats MTWRFSU until 2025-06-30

## Feature
### Event Management

Single Event Creation: Create individual events with start/end times
All-Day Events: Events created with on keyword automatically set to 8am-5pm
Event Series: Recurring events on specific days of the week
Flexible Termination: Series can end by count (for N times) or date (until YYYY-MM-DD)
Comprehensive Validation: Prevents duplicate events, validates date/time formats
Weekday Support: Full support for all weekday combinations (M/T/W/R/F/S/U)

### Event Editing

Individual Event Editing: Modify any property of a single event
Series Editing: Three modes of series editing:

- Edit single event in series
- Edit series from specific date onwards
- Edit entire series


Property Support: Edit subject, start time, end time, description, location, status
Series Membership: Events automatically break from series when start/end times are individually modified
Conflict Prevention: Editing validates against existing events to prevent duplicates

### Querying & Display

Date-based Queries: List all events on a specific date
Range Queries: List events within a date/time range
Availability Checking: Check if user is busy at specific date/time
Multi-day Event Support: Events spanning multiple days appear on all relevant dates
Formatted Output: Clean, readable event display with times and locations

### User Interface

Interactive Mode: Full command-line interaction with menu system
Headless Mode: Batch processing from command files
Error Handling: Comprehensive error messages for invalid commands
Command Validation: Robust parsing with clear error feedback
Help System: Built-in menu system with command examples

## Known Limitations
### Multi-day Event Cleanup

- When editing multi-day events to be shorter, the event may remain listed on dates it no longer spans
- This is a mapping cleanup limitation that doesn't affect core functionality
- Events still function correctly; they just appear in extra date listings

### Location System

- Limited to PHYSICAL and ONLINE location types only
- Location details are supported but display formatting is basic
- No validation for location detail format or content

### Time Zone Support

- Application assumes EST timezone as specified in requirements
- No multi-timezone support or daylight saving time handling

### Series Constraints

- Individual events in a series must start and end on the same day
- Series cannot span multiple days per occurrence
- Complex recurrence patterns (e.g., "second Tuesday of each month") not supported

## Technical Implementation Details
### Architecture

- MVC Pattern: Clean separation between Model, View, and Controller
- Command Pattern: Each command type implemented as separate class
- Interface-based Design: Extensible architecture using interfaces

### Data Structures

- HashMap-based Calendar: Events indexed by date for O(1) lookup
- UUID Identification: All events and series have unique identifiers
- Set-based Uniqueness: Prevents duplicate events using equals/hashCode

### Validation & Error Handling

- Comprehensive Input Validation: Date formats, time ranges, duplicate detection
- Graceful Error Recovery: Application continues after command errors
- Clear Error Messages: Specific feedback for different error types

## Command Reference
### Date/Time Formats

- Date: YYYY-MM-DD (e.g., 2025-06-04)
- DateTime: YYYY-MM-DDThh:mm (e.g., 2025-06-04T09:00)
- Weekdays: M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, S=Saturday, U=Sunday

### Supported Properties for Editing

- subject - Event title/name
- start - Start date and time
- end - End date and time
- description - Event description
- location - Event location (physical/online)
- status - Event status (public/private)

### Exit Commands
- The application accepts any of: 'quit' or 'q'

## Troubleshooting
### Common Issues
- "Error: expected '--mode <interactive | headless <filename>>'"

Ensure you're using exactly --mode interactive or --mode headless filename.txt
Check that command line arguments are properly formatted

- "Error: Command file ended without 'exit' command"

Add exit as the last line in your command file
Ensure file is properly saved and accessible

- "Invalid date format" errors

Use ISO format: YYYY-MM-DD for dates, YYYY-MM-DDThh:mm for date-times
Ensure times use 24-hour format (00:00 to 23:59)

- "Event already exists" errors

Events are unique by subject + start time + end time combination
Modify at least one of these properties to create distinct events

## Debug Tips

- Use menu command to see all available options
- Check example files in res/ folder for proper command syntax
- Verify file paths are correct when using headless mode
- Ensure Java classpath includes compiled classes directory
