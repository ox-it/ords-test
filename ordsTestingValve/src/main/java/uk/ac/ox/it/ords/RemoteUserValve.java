package uk.ac.ox.it.ords;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpSession;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.util.IOTools;
import org.apache.catalina.valves.ValveBase;

/**
 * This valve allows the remote user to be set by the user based on a form 
 * entry. Based on RemoteUserValve by Matthew Buckett
 */
public class RemoteUserValve extends ValveBase {

	private Pattern matcher;

	public RemoteUserValve() {
	}

	public void setRequestURI(String pattern) {
		this.matcher = Pattern.compile(pattern);
	}

	@Override
	public void invoke(final Request request, final Response response)
			throws IOException, ServletException {
				
		if (matcher.matcher(request.getRequestURI()).matches()) {
			String username = request.getParameter("username");
			if (username == null || username.length() == 0) {
				sendPage(response);
			} else {
				final String credentials = "credentials";
				final List<String> roles = Collections.emptyList();
				final Principal principal = new GenericPrincipal(username, credentials, roles);
				request.setUserPrincipal(principal);				
				ServletContext siblingContext = request.getSession().getServletContext().getContext("/app");
				siblingContext.setAttribute("remote_user", principal);
				getNext().invoke(request, response);
			}
		} else {
			
			if (request.getRequestURI().equals("/app/logout.jsp")){
				ServletContext siblingContext = request.getSession().getServletContext().getContext("/app");
				siblingContext.setAttribute("remote_user", null);	
			}
			
			HttpSession session = request.getSession();
			Object remote_user = null;
			
			ServletContext siblingContext = request.getSession().getServletContext().getContext("/app");
			if (siblingContext != null){
				remote_user = siblingContext.getAttribute("remote_user");
			}
			
			request.setUserPrincipal((Principal)remote_user);
			getNext().invoke(request, response);
		}

	}


	private void sendPage(Response response) throws IOException {
		InputStream resourceAsStream = getClass().getResourceAsStream(
				"form.html");
		response.addHeader("content-type", "text/html");
		ServletOutputStream outputStream = response.getOutputStream();
		IOTools.flow(resourceAsStream, outputStream);
	}

}
