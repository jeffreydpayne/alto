package com.frs.alto.nosql.mapper.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import com.frs.alto.nosql.mapper.ClassMappingConfigurationSource;
import com.frs.alto.nosql.mapper.NoSqlClassMapping;
import com.frs.alto.nosql.mapper.NoSqlClassMappingImpl;
import com.frs.alto.xml.bindings.AttributeMapping;
import com.frs.alto.xml.bindings.ClassMapping;
import com.frs.alto.xml.bindings.NosqlMappings;
import com.frs.alto.xml.bindings.PackageMapping;

public class XmlBasedConfigurationSource implements ClassMappingConfigurationSource {

	private List<String> configurationFiles = null;

	@Override
	public Map<Class, NoSqlClassMapping> configure() throws Exception {
		
		Map<Class, NoSqlClassMapping> results = new HashMap<Class, NoSqlClassMapping>();
		
		InputStream inStream;
		for (String config : configurationFiles) {
			inStream = resolveConfigurationFile(config);
			if (inStream != null) {
				addConfiguration(results, inStream);
			}
		}
		return results;
	}
	
	protected InputStream resolveConfigurationFile(String fileName) throws Exception {
				
		if (StringUtils.contains(fileName, "/") || StringUtils.contains(fileName, "\\") ) {
			File file = new File(fileName);
			if (!file.exists()) {
				throw new IllegalStateException("Unable to locate configuration file: " + file.getAbsolutePath());
			}
			return new FileInputStream(file);
		}
		else {
			return this.getClass().getClassLoader().getResourceAsStream(fileName);
		}

		
	}
	
	protected void addConfiguration(ClassMapping mapping, Map<Class, NoSqlClassMapping> mappings) throws Exception {
		
		Class clazz = Class.forName(mapping.getClassName());
		Object dummyInstance = clazz.newInstance();
		NoSqlClassMappingImpl nosql = new NoSqlClassMappingImpl();
		nosql.setClassName(clazz.getName());
		nosql.setTableName(mapping.getTableName());
		if (mapping.getCache() != null) {
			nosql.setCacheRegion(mapping.getCache().getRegion());
		}
		nosql.setHashKeyProperty(mapping.getHashKey().getDomainProperty());
		nosql.setHashKeyAttribute(mapping.getHashKey().getTableAttribute());
		if (mapping.getRangeKey() != null) {
			nosql.setRangeKeyProperty(mapping.getRangeKey().getDomainProperty());
			nosql.setRangeKeyAttribute(mapping.getRangeKey().getTableAttribute());
			nosql.setRangeKeyType(PropertyUtils.getPropertyType(dummyInstance, nosql.getRangeKeyProperty()));
		}
		if (mapping.getAttribute() != null) {
			for (AttributeMapping attr : mapping.getAttribute()) {
				nosql.addPropertyToAttributeMapping(attr.getDomainProperty(), attr.getTableAttribute());						
			}
		}
		
		mappings.put(clazz, nosql);
		
	}
	
	protected void addConfiguration(Map<Class, NoSqlClassMapping> mappings, InputStream inStream) throws Exception {
		
		JAXBContext context = JAXBContext.newInstance(NosqlMappings.class);
		Unmarshaller m = context.createUnmarshaller();
		
		NosqlMappings root = (NosqlMappings)m.unmarshal(inStream);
		
		if (root != null) {
			if (root.getPackage() != null) {
				for (PackageMapping pkg : root.getPackage()) {
					if (pkg.getClazz() != null) {
						for (ClassMapping mapping : pkg.getClazz()) {
							mapping.setClassName(pkg.getName() + "." + mapping.getClassName());
							addConfiguration(mapping, mappings);	
						}
					}
				}
			}
			if (root.getClazz() != null) {
				for (ClassMapping mapping : root.getClazz()) {
					addConfiguration(mapping, mappings);	
				}
			}
			
		}
		
	}

	public List<String> getConfigurationFiles() {
		return configurationFiles;
	}

	public void setConfigurationFiles(List<String> configurationFiles) {
		this.configurationFiles = configurationFiles;
	}
	
	
	
	
}
