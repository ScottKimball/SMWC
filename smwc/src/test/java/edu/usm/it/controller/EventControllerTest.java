package edu.usm.it.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.usm.config.WebAppConfigurationAware;
import edu.usm.domain.Contact;
import edu.usm.domain.Event;
import edu.usm.service.ContactService;
import edu.usm.service.EventService;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by Andrew on 5/30/2015.
 */
public class EventControllerTest extends WebAppConfigurationAware {

    @Autowired
    private ContactService contactService;

    @Autowired
    private EventService eventService;

    private ObjectWriter writer = new ObjectMapper().writer();

    @After
    public void wipeData(){
        eventService.deleteAll();
        contactService.deleteAll();
    }


    private Event constructEvent(String name, String location, String date) {
        Event event = new Event();
        event.setName(name);
        event.setLocation(location);
        event.setDateHeld(date);
        return event;
    }

    private void persistDummyEvent() {
        //Attendee
        Contact attendee = new Contact();
        attendee.setFirstName("Test");
        attendee.setLastName("Attendee");
        contactService.create(attendee);

        //Event
        Event event = constructEvent("Rally", "Headquarters", "2012-01-01");

        eventService.create(event);

        Set<Event> events = new HashSet<>();
        events.add(event);
        attendee.setAttendedEvents(events);
        contactService.attendEvent(attendee,event);
    }


    @Test
    public void testGetAllEvents() throws Exception {

        persistDummyEvent();

        Contact persistedAttendee = contactService.findAll().iterator().next();
        Event event = eventService.findAll().iterator().next();

        MvcResult result = mockMvc.perform(get("/events").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(event.getId())))
                .andExpect(jsonPath("$.[0].name", is(event.getName())))
                .andExpect(jsonPath("$.[0].location", is(event.getLocation())))
                .andExpect(jsonPath("$.[0].dateHeld", is(event.getDateHeld())))
                .andExpect(jsonPath("$.[0].attendees.[0].id", is(persistedAttendee.getId())))
                .andExpect(jsonPath("$.[0].attendees.[0].firstName", is(persistedAttendee.getFirstName())))
                .andExpect(jsonPath("$.[0].attendees.[0].lastName", is(persistedAttendee.getLastName())))
                .andReturn();

    }

    @Test
    public void testCreateEvent() throws Exception {

        Event newEvent = constructEvent("Test", "Church", "2012-01-01");
        String json = writer.writeValueAsString(newEvent);

        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

    }

    @Test
    public void testDeleteEvent() throws Exception {
        Event event = constructEvent("Rally", "Headquarters", "2012-01-01");
        String eventId = eventService.create(event);

        //Attendee
        Contact attendee = new Contact();
        attendee.setFirstName("Test");
        attendee.setLastName("Attendee");
        contactService.create(attendee);
        attendee.setAttendedEvents(new HashSet<>());
        contactService.attendEvent(attendee, event);

        mockMvc.perform(delete("/events/"+eventId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Event eventFromDb = eventService.findById(eventId);
        assertNull(eventFromDb);

        Contact contactFromDb = contactService.findById(attendee.getId());
        assertEquals(0, contactFromDb.getAttendedEvents().size());

    }

    @Test
    public void testUpdateEventPrimitives() throws Exception {
        Event event = constructEvent("Rally", "Headquarters", "2012-01-01");
        String eventId = eventService.create(event);

        //Attendee
        Contact attendee = new Contact();
        attendee.setFirstName("Test");
        attendee.setLastName("Attendee");
        contactService.create(attendee);
        attendee.setAttendedEvents(new HashSet<>());
        contactService.attendEvent(attendee, event);

        event.setName("Updated Name");

        String json = writer.writeValueAsString(event);

        MvcResult result = mockMvc.perform(put("/events/" + eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Event eventFromDb = eventService.findById(eventId);
        assertTrue(eventFromDb.getAttendees().contains(attendee));
        assertEquals("Updated Name", eventFromDb.getName());

        Contact contactFromDb = contactService.findById(attendee.getId());
        assertTrue(contactFromDb.getAttendedEvents().contains(eventFromDb));

    }

    @Test
    public void testUpdateTruncatedObjectGraph() throws Exception {
        Event event = constructEvent("Rally", "Headquarters", "2012-01-01");
        String eventId = eventService.create(event);

        //Attendee
        Contact attendee = new Contact();
        attendee.setFirstName("Test");
        attendee.setLastName("Attendee");
        contactService.create(attendee);
        attendee.setAttendedEvents(new HashSet<>());
        contactService.attendEvent(attendee, event);

        event.setAttendees(null);
        event.setName("Updated Name");

        String json = writer.writeValueAsString(event);

        MvcResult result = mockMvc.perform(put("/events/" + eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Event eventFromDb = eventService.findById(eventId);
        assertTrue(eventFromDb.getAttendees().contains(attendee));
        assertEquals("Updated Name", eventFromDb.getName());

        Contact contactFromDb = contactService.findById(attendee.getId());
        assertTrue(contactFromDb.getAttendedEvents().contains(eventFromDb));

    }

}
