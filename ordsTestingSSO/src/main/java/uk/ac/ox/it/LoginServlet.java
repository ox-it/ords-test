package uk.ac.ox.it;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {
	
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	    //response.addHeader("REMOTE_USER", request.getRemoteUser());
	    //RequestDispatcher view = request.getRequestDispatcher("http://localhost:8080/ordsFrontEnd/");
	    //view.forward(request, response);
		response.sendRedirect("/app");
	}
	
}