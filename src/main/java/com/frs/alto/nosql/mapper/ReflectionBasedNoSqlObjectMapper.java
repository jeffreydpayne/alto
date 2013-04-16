package com.frs.alto.nosql.mapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.frs.alto.domain.BaseDomainObject;

public class ReflectionBasedNoSqlObjectMapper implements NoSqlObjectMapper, InitializingBean {

	private ClassMappingConfigurationSource configurationSource = null;
	private Map<Class, NoSqlClassMapping> configuration = null;
	private TypeTransformer typeTransformer = new PassthroughTypeTransformer();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		configuration = configurationSource.configure();
		
	}
	
	@Override
	public String getHashKeyProperty(Class clazz) {
		NoSqlClassMapping mapping = configuration.get(clazz);
		if (mapping == null) {
			throw new IllegalStateException("No class mapping found for class: " + clazz.getName());
		}
		
		return mapping.getHashKeyProperty();
	}

	
	@Override
	public String getHashKeyAttribute(Class clazz) {
		NoSqlClassMapping mapping = configuration.get(clazz);
		if (mapping == null) {
			throw new IllegalStateException("No class mapping found for class: " + clazz.getName());
		}
		
		return mapping.getHashKeyAttribute();
	}



	@Override
	public String getRangeKeyAttribute(Class clazz) {
		NoSqlClassMapping mapping = configuration.get(clazz);
		if (mapping == null) {
			throw new IllegalStateException("No class mapping found for class: " + clazz.getName());
		}
		
		return mapping.getRangeKeyAttribute();
	}
	
	@Override
	public String getRangeKeyProperty(Class clazz) {
		NoSqlClassMapping mapping = configuration.get(clazz);
		if (mapping == null) {
			throw new IllegalStateException("No class mapping found for class: " + clazz.getName());
		}
		
		return mapping.getRangeKeyProperty();
	}



	@Override
	public String getTableName(Class clazz) {
		NoSqlClassMapping mapping = configuration.get(clazz);
		if (mapping == null) {
			throw new IllegalStateException("No class mapping found for class: " + clazz.getName());
		}
		
		return mapping.getTableName();
	}



	@Override
	public String getCacheRegion(Class clazz) {
		
		NoSqlClassMapping mapping = configuration.get(clazz);
		if (mapping == null) {
			throw new IllegalStateException("No class mapping found for class: " + clazz.getName());
		}
		
		return mapping.getCacheRegion();
		
	}

	
	
	@Override
	public Class getRangeKeyType(Class clazz) {
		
		NoSqlClassMapping mapping = configuration.get(clazz);
		if (mapping == null) {
			throw new IllegalStateException("No class mapping found for class: " + clazz.getName());
		}
		
		return mapping.getRangeKeyType();
		
	}




	@Override
	public NoSqlKey getKey(Class clazz, String serializedKey) {
		
		NoSqlClassMapping mapping = configuration.get(clazz);
		
		try {
			if (mapping.getRangeKeyProperty() == null) {
				return new HashKeyOnly(serializedKey);
			}
			String[] tokens = StringUtils.split(serializedKey, "#");
			
			if (Number.class.isAssignableFrom(mapping.getRangeKeyType())) {
								
				Class rangeKeyType = mapping.getRangeKeyType();
				Number num = (Number)rangeKeyType.getConstructor(String.class).newInstance(tokens[1]);
								
				return new HashKeyWithNumericRangeKey(
						tokens[0], 
						num
						);
			}
			else if (String.class.isAssignableFrom(mapping.getRangeKeyType())) {
				return new HashKeyWithStringRangeKey(
						tokens[0], 
						tokens[1]
						);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
			
		return null;
	}




	@Override
	public NoSqlKey getKey(BaseDomainObject instance) {
		
		NoSqlClassMapping mapping = configuration.get(instance.getClass());
		
		try {
			if (mapping.getRangeKeyProperty() == null) {
				return new HashKeyOnly((String)PropertyUtils.getProperty(instance, mapping.getHashKeyProperty()));
			}
			else if (Number.class.isAssignableFrom(mapping.getRangeKeyType())) {
				return new HashKeyWithNumericRangeKey(
						(String)PropertyUtils.getProperty(instance, mapping.getHashKeyProperty()), 
						(Number)PropertyUtils.getProperty(instance, mapping.getRangeKeyProperty())
						);
			}
			else if (Date.class.isAssignableFrom(mapping.getRangeKeyType())) {
				Date rangeValue = (Date)PropertyUtils.getProperty(instance, mapping.getRangeKeyProperty());
				return new HashKeyWithStringRangeKey(
						(String)PropertyUtils.getProperty(instance, mapping.getHashKeyProperty()), 
						typeTransformer.formatTimeStamp(rangeValue)
						);
			}
			else if (String.class.isAssignableFrom(mapping.getRangeKeyType())) {
				return new HashKeyWithStringRangeKey(
						(String)PropertyUtils.getProperty(instance, mapping.getHashKeyProperty()), 
						(String)PropertyUtils.getProperty(instance, mapping.getRangeKeyProperty())
						);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
			
		return null;
	}

	@Override
	public Map<String, Object> toAttributes(BaseDomainObject object) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		NoSqlClassMapping mapping = configuration.get(object.getClass());
		
		if (mapping == null) {
			throw new IllegalStateException("No class mapping found for class: " + object.getClass().getName());
		}
		
		try {
			map.put(mapping.getHashKeyAttribute(), typeTransformer.toAttributeValue(PropertyUtils.getProperty(object, mapping.getHashKeyProperty()), PropertyUtils.getPropertyType(object, mapping.getHashKeyProperty())));
			if (mapping.getRangeKeyProperty() != null) {
				map.put(mapping.getRangeKeyAttribute(), typeTransformer.toAttributeValue(PropertyUtils.getProperty(object, mapping.getRangeKeyProperty()), PropertyUtils.getPropertyType(object, mapping.getRangeKeyProperty())));
				
			}
			for (Entry<String, String> entry : mapping.getPropertyToAttributeNameMap().entrySet()) {
				
					map.put(entry.getValue(), typeTransformer.toAttributeValue(PropertyUtils.getProperty(object, entry.getKey()), PropertyUtils.getPropertyType(object, entry.getKey())));
				}
				
			}
		
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return map;
	}
	
	@Override
	public BaseDomainObject instantiate(Class<? extends BaseDomainObject> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public BaseDomainObject fromAttributes(BaseDomainObject instance, Map<String, Object> attributes) {
				
		NoSqlClassMapping mapping = configuration.get(instance.getClass());
		
		if (mapping == null) {
			throw new IllegalStateException("No class mapping found for class: " + instance.getClass().getName());
		}
		try {
			Object propValue = typeTransformer.toDomainValue(attributes.get(mapping.getHashKeyAttribute()), PropertyUtils.getPropertyType(instance, mapping.getHashKeyProperty()));
			PropertyUtils.setProperty(instance, mapping.getHashKeyProperty(), propValue);
			if (mapping.getRangeKeyProperty() != null) {
				propValue = typeTransformer.toDomainValue(attributes.get(mapping.getRangeKeyAttribute()), PropertyUtils.getPropertyType(instance, mapping.getRangeKeyProperty()));
				PropertyUtils.setProperty(instance, mapping.getRangeKeyProperty(), propValue);
			}
			for (Entry<String, String> entry : mapping.getPropertyToAttributeNameMap().entrySet()) {
				
					propValue = typeTransformer.toDomainValue(attributes.get(entry.getValue()), PropertyUtils.getPropertyType(instance, entry.getKey()));
					PropertyUtils.setProperty(instance, entry.getKey(), propValue);
				}
				
			}
		
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public ClassMappingConfigurationSource getConfigurationSource() {
		return configurationSource;
	}

	public void setConfigurationSource(
			ClassMappingConfigurationSource configurationSource) {
		this.configurationSource = configurationSource;
	}

	public TypeTransformer getTypeTransformer() {
		return typeTransformer;
	}

	public void setTypeTransformer(TypeTransformer typeTransformer) {
		this.typeTransformer = typeTransformer;
	}
	
	
	

}
