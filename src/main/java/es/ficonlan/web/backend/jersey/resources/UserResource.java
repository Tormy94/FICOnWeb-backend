package es.ficonlan.web.backend.jersey.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import es.ficonlan.web.backend.jersey.util.ApplicationContextProvider;
import es.ficonlan.web.backend.model.user.User;
import es.ficonlan.web.backend.model.userservice.UserService;
import es.ficonlan.web.backend.model.util.exceptions.ServiceException;

/**
 * @author Daniel Gómez Silva
 * @author Miguel Ángel Castillo Bellagona
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
    
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public User adduser(@HeaderParam("sessionId") String sessionId, User user) throws ServiceException {
		return userService.addUser(sessionId, user);
	}
	
	@Path("/{userId}")
	@PUT
	@Consumes({MediaType.APPLICATION_JSON})
	public void changeData(@HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, User user) throws ServiceException {
		if (userId>0) userService.changeUserData(sessionId, userId, user);
		else userService.getCurrenUser(sessionId);
	}
	
	@Path("/{userId}")
	@DELETE
	@Consumes({MediaType.APPLICATION_JSON})
	public void removeUser(@HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, User user) throws ServiceException {
		userService.removeUser(sessionId, userId);
	}
	
	@Path("/changePassword/{userId}")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	public void changePassword(@HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, ChangePasswordData data) throws ServiceException {
		userService.changeUserPassword(sessionId, userId, data.getOldPassword(), data.getNewPassword());
	}
	
}
