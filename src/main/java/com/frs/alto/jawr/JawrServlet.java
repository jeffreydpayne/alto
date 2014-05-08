package com.frs.alto.jawr;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class JawrServlet extends HttpServlet {
	
	/** The serial version UID */
	private static final long serialVersionUID = -4551240917172286444L;

	private JawrService jawrService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {
		
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		
		String serviceBeanId = this.getInitParameter("jawrService");
		
		jawrService = context.getBean(serviceBeanId, JawrService.class);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			jawrService.handleRequest(request, response);
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public void destroy() {
	}

	

}
