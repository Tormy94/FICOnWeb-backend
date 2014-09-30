package es.ficonlan.web.backend.jersey.resources;

import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import es.ficonlan.web.backend.jersey.util.ApplicationContextProvider;
import es.ficonlan.web.backend.model.registration.Registration.RegistrationState;
import es.ficonlan.web.backend.model.role.Role;
import es.ficonlan.web.backend.model.user.User;
import es.ficonlan.web.backend.model.userservice.UserService;
import es.ficonlan.web.backend.model.util.exceptions.ServiceException;

/**
 * @author Daniel Gómez Silva
 */
@Path("user")
public class UserResource {
	
	static class ChangePasswordData {
		private String oldPassword;
		private String newPassword;

		public ChangePasswordData(){}
		public ChangePasswordData(String oldPassword, String newPassword) {
			super();
			this.oldPassword = oldPassword;
			this.newPassword = newPassword;
		}
		public String getOldPassword() {
			return oldPassword;
		}
		public void setOldPassword(String oldPassword) {
			this.oldPassword = oldPassword;
		}
		public String getNewPassword() {
			return newPassword;
		}
		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}	
	}


	@Autowired
    private UserService userService;
    
	public UserResource(){
		userService = ApplicationContextProvider.getApplicationContext().getBean(UserService.class);
	}
    
	@PUT
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public User adduser(@HeaderParam("sessionId") String sessionId, User user) throws ServiceException {
		return userService.addUser(sessionId, user);
	}
	
	@Path("/changeData")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	public void changeData(@HeaderParam("sessionId") String sessionId, User user) throws ServiceException {
		userService.changeUserData(sessionId, user);
	}
	
	@Path("/changePassword/{userId}")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	public void changePassword(@HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, ChangePasswordData data) throws ServiceException {
		userService.changeUserPassword(sessionId, userId, data.getOldPassword(), data.getNewPassword());
	}
	
	@Path("/all/{statrtIndex}/{maxResults}")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public List<User> getAll(@HeaderParam("sessionId") String sessionId, @PathParam("startIndex") int startIndex,  @PathParam("maxResults") int maxResults) throws ServiceException {
		return userService.getAllUsers(sessionId, startIndex, maxResults);

	}
	
	@Path("/findByName/{name}/{statrtIndex}/{maxResults}")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public List<User> findByName(@HeaderParam("sessionId") String sessionId, @PathParam("name") String name, @PathParam("startIndex") int startIndex,  
			                     @PathParam("maxResults") int maxResults) throws ServiceException {
		return userService.findUsersByName(sessionId, name, startIndex, maxResults);

	}
	
	@Path("/byEvent/{eventId}/{state}/{statrtIndex}/{maxResults}")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public List<User> getByEvent(@HeaderParam("sessionId") String sessionId, @PathParam("eventId") int eventId, @PathParam("state") String state,  
			                     @PathParam("startIndex") int startIndex,  @PathParam("maxResults") int maxResults) throws ServiceException {
		RegistrationState st;
		if(state==null) throw new ServiceException(ServiceException.MISSING_FIELD,"state");
    	if(state.toLowerCase().contentEquals("registered"))  st=RegistrationState.registered;
    	else if(state.toLowerCase().contentEquals("inqueue")) st=RegistrationState.inQueue;
    	else if(state.toLowerCase().contentEquals("paid")) st=RegistrationState.paid;
    	else if(state.toLowerCase().contentEquals("all")) st=null;
    	else throw new ServiceException(ServiceException.INCORRECT_FIELD,"state");
		return userService.getUsersByEvent(sessionId, eventId, st, startIndex, maxResults);
	}
	
	@Path("/addToBlackList/{userId}")
	@POST
	public void addToBlackList(@HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) throws ServiceException {
		userService.addUserToBlackList(sessionId, userId);
	}
	
	@Path("/removeFromBlackList/{userId}")
	@POST
	public void removeFromBlackList(@HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) throws ServiceException {
		userService.removeUserFromBlackList(sessionId, userId);
	}
	
	@Path("/getBlackList/{statrtIndex}/{maxResults}")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public List<User> getBlackList(@HeaderParam("sessionId") String sessionId, @PathParam("startIndex") int startIndex,  @PathParam("maxResults") int maxResults) throws ServiceException {
		return userService.getBlacklistedUsers(sessionId, startIndex, maxResults);

	}
	
	@Path("/addRole/{userId}/{roleId}")
	@POST
	public void addRole(@HeaderParam("sessionId") String sessionId, @PathParam("roleId") int roleId, @PathParam("userId") int userId) throws ServiceException{
		userService.addRole(sessionId, roleId, userId);
	}
	
	@Path("/removeRole/{userId}/{roleId}")
	@POST
	public void removeRole(@HeaderParam("sessionId") String sessionId, @PathParam("roleId") int roleId, @PathParam("userId") int userId) throws ServiceException{
		userService.removeRole(sessionId, roleId, userId);
	} 
	
	@Path("/roles/{userId}")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Set<Role> getUserRoles(@HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) throws ServiceException {
		return userService.getUserRoles(sessionId, userId);
	}
}
