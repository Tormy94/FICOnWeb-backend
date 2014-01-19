package es.ficonlan.web.backend.model.util.session;

import java.util.concurrent.ConcurrentHashMap;

import es.ficonlan.web.backend.model.role.Role;
import es.ficonlan.web.backend.model.usecase.UseCase;
import es.ficonlan.web.backend.model.util.exceptions.ServiceException;

public class SessionManager {
	
	private static ConcurrentHashMap<Long, Session> openSessions = new ConcurrentHashMap<Long, Session>();
	
	public static boolean exists(long sessionId){
		return openSessions.containsKey(sessionId);
	}
	
	public static void addSession(Session s){
		openSessions.put(s.getSessionId(), s);
	}
	
	public static Session getSession(long sessionId){
		return openSessions.get(sessionId);
	}
	
	public static void removeSession(long sessionId){
		openSessions.remove(sessionId);
	}
	
	/**
	 * If the target user of the operation is the session owner -> Operation allowed<br>
	 * If the target user of the operation isn't the session owner -> Check permissions
	 * 
	 * @param sessionId
	 * @param userId Id of the target user of the operation.
	 * @param useCase
	 * @throws ServiceException
	 */
	public static void checkPermissions(long sessionId, int userId, String useCase) throws ServiceException{
		if (!exists(sessionId)) throw new ServiceException(01,useCase);
		Session session = getSession(sessionId);
		if (session.getUser().getUserId() != userId) checkPermissions(session, useCase);
	}
	
	public static void checkPermissions(long sessionId, String useCase) throws ServiceException {
		if (!exists(sessionId)) throw new ServiceException(01,useCase);
		checkPermissions(openSessions.get(sessionId), useCase);	
	}
	
	public static  void checkPermissions(Session session, String useCase) throws ServiceException{
		for(Role r:session.getUser().getRoles()){
		    for (UseCase uc: r.getUseCases()){
		    	if (uc.getUseCaseName().contentEquals(useCase)) return;
		    }
		}
		throw new ServiceException(02, useCase);	
	}
}