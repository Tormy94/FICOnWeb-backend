/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ficonlan.web.backend.test.eventservice;

import es.ficonlan.web.backend.model.activity.Activity;
import es.ficonlan.web.backend.model.activity.Activity.ActivityType;
import es.ficonlan.web.backend.model.activity.ActivityDao;
import es.ficonlan.web.backend.model.event.Event;
import es.ficonlan.web.backend.model.event.EventDao;
import es.ficonlan.web.backend.model.eventservice.EventService;
import es.ficonlan.web.backend.model.registration.Registration;
import es.ficonlan.web.backend.model.registration.Registration.RegistrationState;
import es.ficonlan.web.backend.model.registration.RegistrationDao;
import es.ficonlan.web.backend.model.role.RoleDao;
import es.ficonlan.web.backend.model.usecase.UseCaseDao;
import es.ficonlan.web.backend.model.user.User;
import es.ficonlan.web.backend.model.user.UserDao;
import es.ficonlan.web.backend.model.userservice.UserService;
import es.ficonlan.web.backend.model.util.exceptions.InstanceException;
import es.ficonlan.web.backend.model.util.exceptions.ServiceException;
import es.ficonlan.web.backend.model.util.session.Session;
import static es.ficonlan.web.backend.model.util.GlobalNames.SPRING_CONFIG_FILE;
import static es.ficonlan.web.backend.test.util.GlobalNames.SPRING_CONFIG_TEST_FILE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author David Pereiro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {SPRING_CONFIG_FILE, SPRING_CONFIG_TEST_FILE})
@Transactional
public class EventServiceTest {

    private final String NON_EXISTENT_EVENT_NAME = "Boring event";
    private final String ADMIN_LOGIN = "Admin";
    private final String ADMIN_PASS = "initialAdminPass";

    @Autowired
    private EventDao eventDao;
    
    @Autowired
    private ActivityDao activityDao;
    
    @Autowired
    private RegistrationDao registrationDao;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private UserService userService;
	
    @Autowired
	private UserDao userDao;
	
	@Autowired
	private RoleDao roleDao;
	
	@Autowired
	private UseCaseDao useCaseDao;
	 
	@Before 
	public void initialize() {
	     userService.initialize();
	}
	
    @Test
    public void testCreateEvent() throws ServiceException, InstanceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	Event event = new Event(0,"FicOnLan 2014","FicOnLan 2014",150,Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance());
    	eventService.createEvent(s.getSessionId(), event);
    	assertTrue(eventDao.find(event.getEventId()).getEventId()==event.getEventId());
    	
    }
    
    @Test
    public void testChangeEventData() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	Event event = new Event(0,"FicOnLan 2014","FicOnLan 2014",150,Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance());
    	eventDao.save(event);
    	Calendar newDate = Calendar.getInstance();
    	newDate.set(1, 2, 2015);
    	Event newEventData = new Event(event.getEventId(),"FicOnLan 2015","FicOnLan 2015", 200, newDate, newDate, newDate, newDate);
    	eventService.changeEventData(s.getSessionId(), newEventData);
    	assertTrue(event.getName().contentEquals("FicOnLan 2015"));
    	assertTrue(event.getDescription().contentEquals("FicOnLan 2015"));
    	assertTrue(event.getNumParticipants()==200);
    	assertTrue(event.getStartDate().compareTo(newDate)==0);
    	assertTrue(event.getEndDate().compareTo(newDate)==0);
    	assertTrue(event.getRegistrationOpenDate().compareTo(newDate)==0);
    	assertTrue(event.getRegistrationCloseDate().compareTo(newDate)==0);
    }
    
    @Test
    public void testAddParticipantToEvent() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	Calendar regCloseDate = Calendar.getInstance();
    	regCloseDate.add(Calendar.DAY_OF_YEAR, 1);
    	Event event = new Event(0,"FicOnLan 2014","FicOnLan 2014",150,Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance(),regCloseDate);
    	eventDao.save(event);
    	User user = userService.addUser(anonymousSession.getSessionId(), new User("User1", "login1", "pass", "12345678R", "user1@gmail.com", "690047407", "L"));
    	eventService.addParticipantToEvent(s.getSessionId(), user.getUserId(), event.getEventId());
    	Registration r = registrationDao.findByUserAndEvent(user.getUserId(), event.getEventId());
    	assertTrue(r!=null);
    	assertTrue(r.getUser().getUserId()==user.getUserId());
    	assertTrue(r.getEvent().getEventId()==event.getEventId());
    	assertTrue(r.getState()==RegistrationState.registered);	
    }
    
    @Test
    public void testRemoveParticipantFromEvent() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	Event event = new Event(0,"FicOnLan 2014","FicOnLan 2014",150,Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance());
    	eventDao.save(event);
    	User user = userService.addUser(anonymousSession.getSessionId(), new User("User1", "login1", "pass", "12345678R", "user1@gmail.com", "690047407", "L"));
    	Registration r = new Registration(user,event);
    	registrationDao.save(r);
    	eventService.removeParticipantFromEvent(s.getSessionId(), user.getUserId(), event.getEventId());
    	r = registrationDao.findByUserAndEvent(user.getUserId(), event.getEventId());
    	assertTrue(r==null);
    }
    
    @Test
    public void testChangeRegistrationState() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	Event event = new Event(0,"FicOnLan 2014","FicOnLan 2014",150,Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance());
    	eventDao.save(event);
    	User user = userService.addUser(anonymousSession.getSessionId(), new User("User1", "login1", "pass", "12345678R", "user1@gmail.com", "690047407", "L"));
    	Registration r = new Registration(user,event);
    	registrationDao.save(r);
    	assertTrue(r.getState()==RegistrationState.registered);	
    	eventService.changeRegistrationState(s.getSessionId(), user.getUserId(), event.getEventId(), RegistrationState.inQueue);
    	r = registrationDao.findByUserAndEvent(user.getUserId(), event.getEventId());
    	assertTrue(r.getState()==RegistrationState.inQueue);		
    }
    
    @Test
    public void testFindEventByName() throws ServiceException {
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
        Event event = new Event(0, "Awesome event!", "Curling world cup.", 10, Calendar.getInstance(), Calendar.getInstance(),Calendar.getInstance(), Calendar.getInstance());
        eventDao.save(event);
        Assert.assertEquals(event, eventService.findEventByName(s.getSessionId(), "Awesome event!"));
    }

    @Test(expected = ServiceException.class)
    public void testFindEventByUnexistingName() throws ServiceException {
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
        eventService.findEventByName(s.getSessionId(), NON_EXISTENT_EVENT_NAME);
    }
    
    @Test
    public void testAddActivity() throws ServiceException, InstanceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	Calendar dateStart = Calendar.getInstance();
    	Calendar dateEnd = Calendar.getInstance();
    	dateEnd.add(Calendar.DAY_OF_YEAR, 1);
    	Event event = new Event(0,"FicOnLan 2014","FicOnLan 2014",150,dateStart,dateEnd,dateStart,dateEnd);
    	eventDao.save(event);
    	Activity activity = eventService.addActivity(s.getSessionId(), event.getEventId(), 
    													new Activity(event, "Torneo de Lol", "Torneo de Lol", 10, ActivityType.Tournament, true, dateStart,dateEnd,dateStart,dateEnd));
    	assertTrue(activityDao.find(activity.getActivityId()).getActivityId()==activity.getActivityId());
    }
    
    @Test
    public void testRemoveActivity() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	Calendar dateStart = Calendar.getInstance();
    	Calendar dateEnd = Calendar.getInstance();
    	dateEnd.add(Calendar.DAY_OF_YEAR, 1);
    	Event event = new Event(0,"FicOnLan 2014","FicOnLan 2014",150,dateStart,dateEnd,dateStart,dateEnd);
    	eventDao.save(event);
    	Activity activity = eventService.addActivity(s.getSessionId(), event.getEventId(), new Activity(event, "Torneo de Lol", "Torneo de Lol", 10, ActivityType.Tournament, true, dateStart,dateEnd,dateStart,dateEnd));
    	eventService.removeActivity(s.getSessionId(), activity.getActivityId());
    	try {
			activityDao.find(activity.getActivityId());
            fail("This should have thrown an exception"); 
		} catch (InstanceException e) {}
    }
    
    @Test
    public void testChangeActivityData() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	Calendar dateStart = Calendar.getInstance();
    	Calendar dateEnd = Calendar.getInstance();
    	dateEnd.add(Calendar.DAY_OF_YEAR, 1);
    	Event event = new Event(0,"FicOnLan 2014","FicOnLan 2014",150,dateStart,dateEnd,dateStart,dateEnd);
    	eventDao.save(event);
    	Activity activity = eventService.addActivity(s.getSessionId(), event.getEventId(), new Activity(event, "Torneo de Lol", "Torneo de Lol", 10, ActivityType.Tournament, true, dateStart,dateEnd,dateStart,dateEnd));
    	dateStart.add(Calendar.HOUR, 1);
    	dateEnd.add(Calendar.HOUR, 2);
    	eventService.changeActivityData(s.getSessionId(), new Activity(activity.getActivityId(), "Concurso de hacking", "Concurso de hacking", 20, ActivityType.Production, false, dateStart,dateEnd,dateStart,dateEnd));
    	assertTrue(activity.getName().contentEquals("Concurso de hacking"));
    	assertTrue(activity.getDescription().contentEquals("Concurso de hacking"));
    	assertTrue(activity.getNumParticipants()==20);
    	assertTrue(activity.getType()==ActivityType.Production);
    	assertTrue(!activity.isOficial());
    	assertTrue(activity.getStartDate().compareTo(dateStart)==0);
      	assertTrue(activity.getEndDate().compareTo(dateEnd)==0);
    	assertTrue(activity.getRegDateOpen().compareTo(dateStart)==0);
      	assertTrue(activity.getRegDateClose().compareTo(dateEnd)==0);
    }
    
    @Test
    public void testSetActivityOrganizer() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	User user = userService.addUser(anonymousSession.getSessionId(), new User("User1", "login1", "pass", "12345678R", "user1@gmail.com", "690047407", "L"));
    	Calendar dateStart = Calendar.getInstance();
    	Calendar dateEnd = Calendar.getInstance();
    	dateEnd.add(Calendar.DAY_OF_YEAR, 1);
    	Event event = eventService.createEvent(s.getSessionId(), new Event(0,"FicOnLan 2014","FicOnLan 2014",150,dateStart,dateEnd,dateStart,dateEnd));
    	eventService.addParticipantToEvent(s.getSessionId(), user.getUserId(), event.getEventId());
    	Activity activity = eventService.addActivity(s.getSessionId(), event.getEventId(), new Activity(event, "Torneo de Lol", "Torneo de Lol", 10, ActivityType.Tournament, true, dateStart,dateEnd,dateStart,dateEnd));
    	eventService.setActivityOrganizer(s.getSessionId(), activity.getActivityId(), user.getUserId());
    	assertTrue(activity.getOrganizer().getUserId()==user.getUserId());
    }
    
    @Test
    public void testGetActivitiesByEvent() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	Calendar dateStart = Calendar.getInstance();
    	Calendar dateEnd = Calendar.getInstance();
    	Event event = eventService.createEvent(s.getSessionId(), new Event(0,"FicOnLan 2014","FicOnLan 2014",150,dateStart,dateEnd,dateStart,dateEnd));
    	Activity activity1 = eventService.addActivity(s.getSessionId(), event.getEventId(), new Activity(event, "Torneo de Lol", "Torneo de Lol", 10, ActivityType.Tournament, true, dateStart,dateEnd,dateStart,dateEnd));
    	Activity activity2 = eventService.addActivity(s.getSessionId(), event.getEventId(), new Activity(event, "Concurso de hacking", "Concurso de hacking", 20, ActivityType.Production, false, dateStart,dateEnd,dateStart,dateEnd));
    	List<Activity> result = eventService.getActivitiesByEvent(s.getSessionId(), event.getEventId(), null);
    	assertTrue(result.size()==2);
    	assertTrue(result.get(0).getActivityId()==activity1.getActivityId());
    	assertTrue(result.get(1).getActivityId()==activity2.getActivityId());
    	result = eventService.getActivitiesByEvent(s.getSessionId(), event.getEventId(), ActivityType.Tournament);
    	assertTrue(result.size()==1);
    	assertTrue(result.get(0).getActivityId()==activity1.getActivityId());
    	result = eventService.getActivitiesByEvent(s.getSessionId(), event.getEventId(), ActivityType.Production);
    	assertTrue(result.size()==1);
    	assertTrue(result.get(0).getActivityId()==activity2.getActivityId());
    }
    
    @Test
    public void testAddParticipantToActivity() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	User user = userService.addUser(anonymousSession.getSessionId(), new User("User1", "login1", "pass", "12345678R", "user1@gmail.com", "690047407", "L"));
    	Calendar dateStart = Calendar.getInstance();
    	Calendar dateEnd = Calendar.getInstance();
    	dateEnd.add(Calendar.DAY_OF_YEAR, 1);
    	Event event = eventService.createEvent(s.getSessionId(), new Event(0,"FicOnLan 2014","FicOnLan 2014",150,dateStart,dateEnd,dateStart,dateEnd));
    	eventService.addParticipantToEvent(s.getSessionId(), user.getUserId(), event.getEventId());
    	Activity activity = eventService.addActivity(s.getSessionId(), event.getEventId(), new Activity(event, "Torneo de Lol", "Torneo de Lol", 10, ActivityType.Tournament, true, dateStart,dateEnd,dateStart,dateEnd));
    	eventService.addParticipantToActivity(s.getSessionId(), user.getUserId(), activity.getActivityId());
    	assertTrue(activity.getParticipants().contains(user));
    }
    
    @Test
    public void testRemoveParticipantFromActivity() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	User user = userService.addUser(anonymousSession.getSessionId(), new User("User1", "login1", "pass", "12345678R", "user1@gmail.com", "690047407", "L"));
    	Calendar dateStart = Calendar.getInstance();
    	Calendar dateEnd = Calendar.getInstance();
    	dateEnd.add(Calendar.DAY_OF_YEAR, 1);
    	Event event = eventService.createEvent(s.getSessionId(), new Event(0,"FicOnLan 2014","FicOnLan 2014",150,dateStart,dateEnd,dateStart,dateEnd));
    	eventService.addParticipantToEvent(s.getSessionId(), user.getUserId(), event.getEventId());
    	Activity activity = eventService.addActivity(s.getSessionId(), event.getEventId(), new Activity(event, "Torneo de Lol", "Torneo de Lol", 10, ActivityType.Tournament, true, dateStart,dateEnd,dateStart,dateEnd));
    	eventService.addParticipantToActivity(s.getSessionId(), user.getUserId(), activity.getActivityId());
    	assertTrue(activity.getParticipants().contains(user));
    	eventService.removeParticipantFromActivity(s.getSessionId(), user.getUserId(), activity.getActivityId());
       	assertTrue(!activity.getParticipants().contains(user));
    }
    
    @Test
    public void testGetActivityParticipants() throws ServiceException{
    	Session anonymousSession = userService.newAnonymousSession();
    	Session s = userService.login(anonymousSession.getSessionId(), ADMIN_LOGIN, ADMIN_PASS);
    	User user1 = userService.addUser(anonymousSession.getSessionId(), new User("User1", "login1", "pass", "12345678R", "user1@gmail.com", "690047407", "L"));
    	User user2 = userService.addUser(anonymousSession.getSessionId(), new User("User2", "login2", "pass", "12332218R", "user2@gmail.com", "690047407", "L"));
    	Calendar dateStart = Calendar.getInstance();
    	Calendar dateEnd = Calendar.getInstance();
    	dateEnd.add(Calendar.DAY_OF_YEAR, 1);
    	Event event = eventService.createEvent(s.getSessionId(), new Event(0,"FicOnLan 2014","FicOnLan 2014",150,dateStart,dateEnd,dateStart,dateEnd));
    	eventService.addParticipantToEvent(s.getSessionId(), user1.getUserId(), event.getEventId());
    	eventService.addParticipantToEvent(s.getSessionId(), user2.getUserId(), event.getEventId());
    	Activity activity = eventService.addActivity(s.getSessionId(), event.getEventId(), new Activity(event, "Torneo de Lol", "Torneo de Lol", 10, ActivityType.Tournament, true, dateStart,dateEnd,dateStart,dateEnd));
    	eventService.addParticipantToActivity(s.getSessionId(), user1.getUserId(), activity.getActivityId());
    	eventService.addParticipantToActivity(s.getSessionId(), user2.getUserId(), activity.getActivityId());
    	List<User> result = eventService.getActivityParticipants(s.getSessionId(), activity.getActivityId());
    	assertTrue(result.size()==2);
    	assertTrue(result.get(0).getUserId()==user1.getUserId());
    	assertTrue(result.get(1).getUserId()==user2.getUserId());
    }
    
    
}