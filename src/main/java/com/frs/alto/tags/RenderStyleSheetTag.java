package com.frs.alto.tags;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class RenderStyleSheetTag extends SimpleTagSupport implements TagAttributeKeys {
	
	@Override
	public void doTag() throws JspException, IOException {
		
		List<String> paths = (List<String>)getJspContext().getAttribute(CSS, PageContext.REQUEST_SCOPE);
		
		if (paths != null) {
			JspWriter out = getJspContext().getOut();
			for (String script : paths) {				
				out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
				out.write(script);
				out.write("\" />\r\n"); 
			}
		}
		
	}
	
	
	

}
