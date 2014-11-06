package com.frs.alto.tags;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class RenderScriptsTag extends SimpleTagSupport implements TagAttributeKeys {
	
	@Override
	public void doTag() throws JspException, IOException {
		
		List<String> paths = (List<String>)getJspContext().getAttribute(SCRIPTS);
		
		if (paths != null) {
			JspWriter out = getJspContext().getOut();
			for (String script : paths) {
				out.write("<script type=\"text/javascript\" src=\"");
				out.write(script);
				out.write("\"></script>\r\n");
			}
		}
		
	}
	
	
	

}
