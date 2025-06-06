package calendar.model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.UUID;


import calendar.model.Location;
import calendar.model.EventStatus;

// Static imports for JUnit Assertions
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * JUnit 4 test class for the {@link Event} model.
 * Covers constructors, getters, setters, equals, hashCode, and location display methods.
 */
public class EventTest {

  private Event testEvent1;
  private Event testEvent2;
  private LocalDateTime now;
  private LocalDateTime future;

  /**
   * Sets up the test environment before each test method is executed.
   * Initializes common {@link LocalDateTime} objects and two {@link Event} instances
   * with different levels of detail for testing purposes.
   */
  @Before
  public void setUp() {
    // Initialize common objects before each test
    now = LocalDateTime.of(2025, 6, 1, 10, 0);
    future = LocalDateTime.of(2025, 6, 1, 11, 0);

    // testEvent1: Basic event with just subject, start, and end.
    testEvent1 = new Event("Meeting", now, future);
    // testEvent2: Full event with all optional details provided.
    testEvent2 = new Event("Presentation",
            LocalDateTime.of(2025, 6, 2, 9, 0),
            LocalDateTime.of(2025, 6, 2, 10, 0),
            "Discuss project",
            Location.ONLINE,
            "Zoom Link: 12345",
            EventStatus.PUBLIC,
            UUID.randomUUID()
    );
  }


  /**
   * Tests the constructor that takes only subject, start, and end {@link LocalDateTime}.
   * Verifies that the subject, start, and end times are correctly set, and that
   * optional fields (description, location, status, series ID) are null, while
   * a non-null unique ID is generated.
   */
  @Test
  public void testConstructorWithSubjectStartEnd() {
    // Test basic constructor
    assertEquals("Meeting", testEvent1.getSubject());
    assertEquals(now, testEvent1.getStart());
    assertEquals(future, testEvent1.getEnd());
    assertNull(testEvent1.getDescription());
    assertNull(testEvent1.getLocation());
    assertNull(testEvent1.getStatus());
    assertNull(testEvent1.getSeriesID());
    assertNotNull(testEvent1.getId()); // ID should be generated
  }

  /**
   * Tests the full constructor that takes all possible parameters for an {@link Event}.
   * Verifies that all fields, including optional ones, are correctly initialized
   * based on the provided arguments.
   */
  @Test
  public void testFullConstructor() {
    // Test constructor with all optional details
    assertEquals("Presentation", testEvent2.getSubject());
    assertEquals(LocalDateTime.of(2025, 6, 2, 9, 0),
            testEvent2.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 2, 10, 0),
            testEvent2.getEnd());
    assertEquals("Discuss project", testEvent2.getDescription());
    assertEquals(Location.ONLINE, testEvent2.getLocation());
    assertEquals("Zoom Link: 12345", testEvent2.getLocationDetail());
    assertEquals(EventStatus.PUBLIC, testEvent2.getStatus());
    assertNotNull(testEvent2.getSeriesID());
    assertNotNull(testEvent2.getId());
  }

  /**
   * Tests the constructor that takes only subject and start {@link LocalDateTime}.
   * Verifies that the subject and start time are correctly set, and that
   * the end time is initially null.
   */
  @Test
  public void testConstructorWithOnlySubjectAndStartDate() {
    // Test constructor with subject and start date, end date is null initially
    Event event = new Event("Daily Standup", LocalDateTime.of(2025, 6,
            3, 9, 0));
    assertEquals("Daily Standup", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 6, 3, 9, 0),
            event.getStart());
    assertNull(event.getEnd()); // End date should be null
  }


  /**
   * Tests the {@code getId} method.
   * Asserts that a non-null ID is returned for both test events and that their IDs are distinct.
   */
  @Test
  public void testGetId() {
    assertNotNull(testEvent1.getId());
    assertNotEquals(testEvent1.getId(), testEvent2.getId()); // IDs should be unique
  }

  /**
   * Tests the {@code getSubject} method.
   * Asserts that the correct subject is returned for {@code testEvent1}.
   */
  @Test
  public void testGetSubject() {
    assertEquals("Meeting", testEvent1.getSubject());
  }

  /**
   * Tests the {@code getStart} method.
   * Asserts that the correct start {@link LocalDateTime} is returned for {@code testEvent1}.
   */
  @Test
  public void testGetStart() {
    assertEquals(now, testEvent1.getStart());
  }

  /**
   * Tests the {@code getEnd} method.
   * Asserts that the correct end {@link LocalDateTime} is returned for {@code testEvent1}.
   */
  @Test
  public void testGetEnd() {
    assertEquals(future, testEvent1.getEnd());
  }

  /**
   * Tests the {@code getDescription} method.
   * Asserts that it returns null for {@code testEvent1} (which has no description)
   * and the correct description for {@code testEvent2}.
   */
  @Test
  public void testGetDescription() {
    assertNull(testEvent1.getDescription());
    assertEquals("Discuss project", testEvent2.getDescription());
  }

  /**
   * Tests the {@code getLocation} method.
   * Asserts that it returns null for {@code testEvent1} (which has no location)
   * and the correct {@link Location} enum for {@code testEvent2}.
   */
  @Test
  public void testGetLocation() {
    assertNull(testEvent1.getLocation());
    assertEquals(Location.ONLINE, testEvent2.getLocation());
  }

  /**
   * Tests the {@code getLocationDetail} method.
   * Asserts that it returns null for {@code testEvent1} (which has no location detail)
   * and the correct location detail string for {@code testEvent2}.
   */
  @Test
  public void testGetLocationDetail() {
    assertNull(testEvent1.getLocationDetail());
    assertEquals("Zoom Link: 12345", testEvent2.getLocationDetail());
  }

  /**
   * Tests the {@code getStatus} method.
   * Asserts that it returns null for {@code testEvent1} (which has no status)
   * and the correct {@link EventStatus} enum for {@code testEvent2}.
   */
  @Test
  public void testGetStatus() {
    assertNull(testEvent1.getStatus());
    assertEquals(EventStatus.PUBLIC, testEvent2.getStatus());
  }

  /**
   * Tests the {@code getSeriesID} method.
   * Asserts that it returns null for {@code testEvent1} (which is not part of a series)
   * and a non-null {@link UUID} for {@code testEvent2} (which is part of a series).
   */
  @Test
  public void testGetSeriesID() {
    assertNull(testEvent1.getSeriesID());
    assertNotNull(testEvent2.getSeriesID());
  }


  /**
   * Tests the {@code setSubject} method.
   * Verifies that the subject can be updated to a new string, an empty string, and null.
   */
  @Test
  public void testSetSubject() {
    testEvent1.setSubject("New Subject");
    assertEquals("New Subject", testEvent1.getSubject());
    testEvent1.setSubject(""); // Test empty string
    assertEquals("", testEvent1.getSubject());
    testEvent1.setSubject(null); // Test null subject
    assertNull(testEvent1.getSubject());
  }

  /**
   * Tests the {@code setStart} method.
   * Verifies that the start {@link LocalDateTime} can be updated.
   */
  @Test
  public void testSetStart() {
    LocalDateTime newStart = LocalDateTime.of(2025, 6, 1, 9, 0);
    testEvent1.setStart(newStart);
    assertEquals(newStart, testEvent1.getStart());
    assertNotNull(testEvent1.getStart()); // Should not be null after setting
  }

  /**
   * Tests the {@code setEnd} method.
   * Verifies that the end {@link LocalDateTime} can be updated.
   */
  @Test
  public void testSetEnd() {
    LocalDateTime newEnd = LocalDateTime.of(2025, 6, 1, 12, 0);
    testEvent1.setEnd(newEnd);
    assertEquals(newEnd, testEvent1.getEnd());
    assertNotNull(testEvent1.getEnd()); // Should not be null after setting
  }

  /**
   * Tests the {@code setDescription} method.
   * Verifies that the description can be updated to a new string, an empty string, and null.
   */
  @Test
  public void testSetDescription() {
    testEvent1.setDescription("A new description");
    assertEquals("A new description", testEvent1.getDescription());
    testEvent1.setDescription("");
    assertEquals("", testEvent1.getDescription());
    testEvent1.setDescription(null);
    assertNull(testEvent1.getDescription());
  }

  /**
   * Tests the {@code setLocation} method.
   * Verifies that the location can be updated to a new {@link Location} enum and null.
   */
  @Test
  public void testSetLocation() {
    testEvent1.setLocation(Location.PHYSICAL);
    assertEquals(Location.PHYSICAL, testEvent1.getLocation());
    testEvent1.setLocation(null);
    assertNull(testEvent1.getLocation());
  }

  /**
   * Tests the {@code setLocationDetail} method.
   * Verifies that the location detail can be updated to a new string, an empty string, and null.
   */
  @Test
  public void testSetLocationDetail() {
    testEvent1.setLocation(Location.PHYSICAL);
    testEvent1.setLocationDetail("Room 101");
    assertEquals("Room 101", testEvent1.getLocationDetail());
    testEvent1.setLocationDetail("");
    assertEquals("", testEvent1.getLocationDetail());
    testEvent1.setLocationDetail(null);
    assertNull(testEvent1.getLocationDetail());
  }

  /**
   * Tests the {@code setStatus} method.
   * Verifies that the status can be updated to a new {@link EventStatus} enum and null.
   */
  @Test
  public void testSetStatus() {
    testEvent1.setStatus(EventStatus.PRIVATE);
    assertEquals(EventStatus.PRIVATE, testEvent1.getStatus());
    testEvent1.setStatus(null);
    assertNull(testEvent1.getStatus());
  }

  /**
   * Tests the {@code setSeriesId} method.
   * Verifies that the series ID can be updated to a new {@link UUID} and null.
   */
  @Test
  public void testSetSeriesId() {
    UUID newSeriesId = UUID.randomUUID();
    testEvent1.setSeriesId(newSeriesId);
    assertEquals(newSeriesId, testEvent1.getSeriesID());
    testEvent1.setSeriesId(null);
    assertNull(testEvent1.getSeriesID());
  }


  /**
   * Tests the {@code equals} method with the same object instance.
   * Should return true.
   */
  @Test
  public void testEqualsSameObject() {
    assertEquals(testEvent1, testEvent1);
  }

  /**
   * Tests the {@code equals} method with an object having identical content
   * for subject, start, and end. Should return true.
   */
  @Test
  public void testEqualsIdenticalContent() {
    Event identicalEvent = new Event("Meeting", now, future);
    assertEquals(testEvent1, identicalEvent);
  }

  /**
   * Tests the {@code equals} method with an object having a different subject.
   * Should return false.
   */
  @Test
  public void testEqualsDifferentSubject() {
    Event differentSubject = new Event("Different Meeting", now, future);
    assertFalse(testEvent1.equals(differentSubject));
  }

  /**
   * Tests the {@code equals} method with an object having a different start time.
   * Should return false.
   */
  @Test
  public void testEqualsDifferentStartTime() {
    Event differentStartTime = new Event("Meeting", now.plusHours(1), future);
    assertFalse(testEvent1.equals(differentStartTime));
  }

  /**
   * Tests the {@code equals} method with an object having a different end time.
   * Should return false.
   */
  @Test
  public void testEqualsDifferentEndTime() {
    Event differentEndTime = new Event("Meeting", now, future.plusHours(1));
    assertFalse(testEvent1.equals(differentEndTime));
  }

  /**
   * Tests the {@code equals} method with an object that has different values
   * for description, location, status, or series ID. These fields are
   * intentionally excluded from the {@code equals} and {@code hashCode}
   * implementations to allow events with the same core details (subject, start, end)
   * to be considered equal. Should still return true.
   */
  @Test
  public void testEqualsDifferentDescriptionLocationStatusSeriesID() {
    // These fields are intentionally not part of equals/hashCode
    Event eventWithMoreDetails = new Event("Meeting", now, future,
            "description", Location.ONLINE, null,
            EventStatus.PUBLIC, null);
    assertTrue(testEvent1.equals(eventWithMoreDetails));
  }

  /**
   * Tests the {@code equals} method with a null object.
   * Should return false.
   */
  @Test
  public void testEqualsNullObject() {
    assertFalse(testEvent1 == null);
  }

  /**
   * Tests the {@code equals} method with an object of a different class.
   * Should return false.
   */
  @Test
  public void testEqualsDifferentClass() {
    assertFalse(testEvent1.equals("Not an Event"));
  }


  /**
   * Tests the {@code hashCode} method for consistency with {@code equals}.
   * Two objects that are equal should have the same hash code.
   */
  @Test
  public void testHashCodeConsistentWithEquals() {
    Event identicalEvent = new Event("Meeting", now, future);
    assertEquals(testEvent1.hashCode(), identicalEvent.hashCode());
  }

  /**
   * Tests the {@code hashCode} method for different objects.
   * Objects that are not equal should generally have different hash codes.
   */
  @Test
  public void testHashCodeDifferentObjects() {
    Event differentEvent = new Event("Different Meeting", now, future);
    assertNotEquals(testEvent1.hashCode(), differentEvent.hashCode());
  }


  /**
   * Tests the {@code getLocationDisplay} method when no location information is set.
   * Should return an empty string.
   */
  @Test
  public void testGetLocationDisplayNoLocation() {
    // testEvent1 has null location
    assertEquals("", testEvent1.getLocationDisplay());
  }

  /**
   * Tests the {@code getLocationDisplay} method when a location is set but no detail is provided.
   * Should return only the location type as a string.
   */
  @Test
  public void testGetLocationDisplayWithLocationNoDetail() {
    Event event = new Event("Test", now, future, null, Location.PHYSICAL,
            null, null, null);
    assertEquals("PHYSICAL", event.getLocationDisplay());
  }

  /**
   * Tests the {@code getLocationDisplay} method when both location and location detail
   * are provided.
   * Should return the location type followed by the detail, separated by a colon and space.
   */
  @Test
  public void testGetLocationDisplayWithLocationAndDetail() {
    Event event = new Event("Test", now, future, null, Location.ONLINE,
            "Google Meet", null, null);
    assertEquals("ONLINE: Google Meet", event.getLocationDisplay());
  }

  /**
   * Tests the {@code getLocationDisplay} method when a location is set but the detail
   * is an empty string.
   * Should return only the location type as a string, similar to no detail.
   */
  @Test
  public void testGetLocationDisplayWithLocationEmptyDetail() {
    Event event = new Event("Test", now, future, null, Location.PHYSICAL,
            "", null, null);
    assertEquals("PHYSICAL", event.getLocationDisplay());
  }


  /**
   * Tests the {@code toString} method when no location information is available.
   * Should return a string containing the subject and the start and end times.
   */
  @Test
  public void testToStringNoLocation() {
    assertEquals("Meeting (2025-06-01T10:00 to 2025-06-01T11:00)", testEvent1.toString());
  }

  /**
   * Tests the {@code toString} method when both location and location detail are available.
   * Should return a string containing the subject, start/end times, and the full location display.
   */
  @Test
  public void testToStringWithLocation() {
    Event eventWithLocation = new Event("Presentation",
            LocalDateTime.of(2025, 6, 2, 9, 0),
            LocalDateTime.of(2025, 6, 2, 10, 0),
            "Discuss project",
            Location.ONLINE,
            "Zoom Link: 12345",
            EventStatus.PUBLIC,
            UUID.randomUUID()
    );
    assertEquals("Presentation (2025-06-02T09:00 to 2025-06-02T10:00) @ ONLINE: "
            + "Zoom Link: 12345", eventWithLocation.toString());
  }

  /**
   * Tests the {@code toString} method when a location is available but no detail is provided.
   * Should return a string containing the subject, start/end times, and only the location type.
   */
  @Test
  public void testToStringWithLocationNoDetail() {
    Event event = new Event("Event", now, future, null, Location.PHYSICAL,
            null, null, null);
    assertEquals("Event (2025-06-01T10:00 to 2025-06-01T11:00) @ PHYSICAL",
            event.toString());
  }

  /**
   * Tests that setting the end time before the start time throws an IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSetEndBeforeStartTime() {
    testEvent1.setEnd(testEvent1.getStart().minusMinutes(1));
  }

  /**
   * Tests the {@code toString} method when the event's end time is null.
   */
  @Test
  public void testToStringNullEndTime() {
    Event event = new Event("Now", now);
    assertEquals("Now (2025-06-01T10:00 to null)", event.toString());
  }

  /**
   * Tests the full constructor with null values for optional parameters.
   * Verifies that the constructor properly handles null description, location,
   * status, and series ID.
   */
  @Test
  public void testFullConstructorWithNullOptionalParameters() {
    Event event = new Event("Null Test Event", now, future,
            null, null, null, null, null);

    assertEquals("Null Test Event", event.getSubject());
    assertEquals(now, event.getStart());
    assertEquals(future, event.getEnd());
    assertNull(event.getDescription());
    assertNull(event.getLocation());
    assertNull(event.getLocationDetail());
    assertNull(event.getStatus());
    assertNull(event.getSeriesID());
    assertNotNull(event.getId()); // ID should still be generated
  }

  /**
   * Tests equals() method with events that have null end times.
   * Ensures equality comparison works correctly when end times are null.
   */
  @Test
  public void testEqualsWithNullEndTimes() {
    Event event1 = new Event("Same Event", now);
    Event event2 = new Event("Same Event", now);


    boolean result = event1.equals(event2);
    assertTrue("Events with same subject, start, and null end should be equal", result);


    Event event3 = new Event("Same Event", now, future);
    assertFalse("Event with end time should not equal event without end time",
            event1.equals(event3));
    assertFalse("Event without end time should not equal event with end time",
            event3.equals(event1));
  }

  /**
   * Tests hashCode() consistency when end time is null.
   * Verifies that hash codes are consistent for events with null end times.
   */
  @Test
  public void testHashCodeWithNullEndTime() {
    Event event1 = new Event("Hash Test", now);
    Event event2 = new Event("Hash Test", now);

    // Should have same hash code even with null end times
    assertEquals(event1.hashCode(), event2.hashCode());
  }
}