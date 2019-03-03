package org.orienteer.core.resource;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.tika.Tika;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.orienteer.core.MountPath;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Share dynamic resources such as image, video and other.
 */
@MountPath("/content/${rid}/${field}")
public class OContentShareResource extends AbstractResource {
    
    public static SharedResourceReference getSharedResourceReference() {
    	return new SharedResourceReference(OContentShareResource.class.getName());
    }
    
    public static CharSequence urlFor(ODocument document, String field, String contentType, boolean fullUrl) {
    	return urlFor(getSharedResourceReference(), document, field, contentType, fullUrl);
    }

    protected static CharSequence urlFor(ResourceReference ref, ODocument document, String field, String contentType, boolean fullUrl) {
    	PageParameters params = new PageParameters();
    	params.add("rid", document.getIdentity().toString().substring(1));
    	params.add("field", field);
    	params.add("v", document.getVersion());
    	if(!Strings.isEmpty(contentType)) params.add("type", contentType);
    	CharSequence url = RequestCycle.get().urlFor(ref, params);
    	if(fullUrl) {
    		url = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
    	}
    	return url;
    }

    @Override
    protected ResourceResponse newResourceResponse(IResource.Attributes attributes) {
        final ResourceResponse response = new ResourceResponse();
        response.setLastModified(Time.now());
        if (response.dataNeedsToBeWritten(attributes)) {
            PageParameters params = attributes.getParameters();
            String ridStr = "#"+params.get("rid").toOptionalString();
            ORID orid = ORecordId.isA(ridStr) ? new ORecordId(ridStr) : null;
            if (orid != null) {
                String field = params.get("field").toString();
                final byte [] data = getContent(orid, field);
                if (data != null && data.length > 0) {
                    String contentType = params.get("type").toOptionalString();
                	if (Strings.isEmpty(contentType)) {
                		contentType = new Tika().detect(data);
                	}
                	if(isCacheAllowed()) {
	                	if(params.get("v").isEmpty()) response.disableCaching();
	                	else response.setCacheDurationToMaximum();
                	}
                    response.setContentType(contentType);
                    response.setWriteCallback(createWriteCallback(data));
                }
            }

            if (response.getWriteCallback() == null) {
                response.setError(HttpServletResponse.SC_NOT_FOUND);
            }

        }
        return response;
    }
    
    protected boolean isCacheAllowed() {
    	return false;
    }
    
    protected byte[] getContent(OIdentifiable rid, String field) {
    	ODocument doc = rid.getRecord();
    	if(doc==null) return null;
    	return doc.field(field, byte[].class);
    }

    private WriteCallback createWriteCallback(byte [] data) {
        return new WriteCallback() {
            @Override
            public void writeData(IResource.Attributes attributes) throws IOException {
                attributes.getResponse().write(data);
            }
        };
    }
}