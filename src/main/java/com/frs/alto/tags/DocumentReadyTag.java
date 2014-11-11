package com.frs.alto.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class DocumentReadyTag extends SimpleTagSupport implements TagAttributeKeys {
	
	private String functionCall;
	
	


	public void setFunctionCall(String functionCall) {
		this.functionCall = functionCall;
	}




	@Override
	public void doTag() throws JspException, IOException {
		
		List<String> paths = (List<String>)getJspContext().getAttribute(DOCUMENT_READY_FUNCTIONS, PageContext.REQUEST_SCOPE);
		
		if (paths == null) {
			paths = new ArrayList<String>();
		}
		paths.add(functionCall);
		getJspContext().setAttribute(DOCUMENT_READY_FUNCTIONS, paths, PageContext.REQUEST_SCOPE);
		
	}
	
	
	

}
