package com.frs.alto.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class ScriptTag extends SimpleTagSupport implements TagAttributeKeys {
	
	private String path;
	
	
	public void setPath(String path) {
		this.path = path;
	}


	@Override
	public void doTag() throws JspException, IOException {
		
		List<String> paths = (List<String>)getJspContext().getAttribute(SCRIPTS, PageContext.REQUEST_SCOPE);
		
		if (paths == null) {
			paths = new ArrayList<String>();
		}
		paths.add(path);
		getJspContext().setAttribute(SCRIPTS, paths, PageContext.REQUEST_SCOPE);
		
	}
	
	
	

}
