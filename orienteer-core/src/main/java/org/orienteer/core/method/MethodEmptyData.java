package org.orienteer.core.method;

import org.apache.wicket.model.IModel;
import org.orienteer.core.component.property.DisplayMode;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * 
 * Empty Method environment data. Not parameterized, always return null or false  
 *
 */
public class MethodEmptyData implements IMethodEnvironmentData{

	@Override
	public IModel<?> getDisplayObjectModel() {
		return null;
	}
}
