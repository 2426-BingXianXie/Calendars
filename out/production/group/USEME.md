# GUI Application Guide
This guide provides information on how to properly utilize the GUI for the multi-calendar application.

## Launch Application
- Run the CalendarRunner main class without any arguments.
- Alternatively, navigate to the project directory and run java -jar res/calendar_jar.jar

## Navigating the Calendar
- Your main view shows your current calendar and up to 10 events that are present in the current week.
- Use the Previous Week and Next Week buttons to navigate between weeks.
- Click the "Go to Date" button to visit a specific date in time.

## Managing Calendars
### Create a New Calendar
- Click the "New Calendar" button.
- Enter in the calendar name and select a timezone.
- Click "Create Calendar". The calendar will be automatically created and set as the active calendar.

### Switch Between Calendars
- Use the dropdown next to the "Current:" label to select from your list of created calendars.
- The view will automatically update to show the events for the newly selected calendar.

## Event Management
### Create a Single Event
- Click the "Create Event" button.
- Fill in the required fields (Subject, Start/End Date, Start/End Time) and any optional details in the pop-up.
- Click "Create Event". The event will be added to the calendar, and the schedule view will update to show the new event.

### Create an Event Series
- Click the "Create Series" button.
- Enter details as prompted. 
- Click "Create Series". The series of events will be added to the calendar and the view will update itself accordingly.

### View Event Details
- Double-click any event shown in the schedule and a pop-up will appear and show the event's full details.

### Edit an Event
- Right-click any event shown in the schedule and click "Edit Event", a pop-up will appear, allowing you to edit the selected event.

## Querying & Misc.
### Refresh Schedule
- Click the "Refresh" button to manually reload the events for the currently displayed week. 

### Check Availability Status
- Click the "Check Status" button. A pop-up will appear asking for a specific date and time.
- The application will inform you if you are busy or available during that selected moment in time.

### Event Search
- Click the "Search Events" button.
- Enter a date to start searching from. The application will show a list of the next 10 upcoming events from that date. 
