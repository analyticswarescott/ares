package com.aw.document;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.tenant.Tenant;
import com.aw.common.util.JSONUtils;
import com.aw.document.body.IBodyInitializable;
import com.aw.document.exceptions.DocumentFormatException;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * Document envelope with body available
 */
public class Document extends DocumentEnvelope {

    public static final Logger logger = LoggerFactory.getLogger(Document.class);

    public static final String BODY = "body";

	/**
	 * Order by date, oldest first
	 */
	public static final Comparator<Document> ORDER_BY_DATE = new Comparator<Document>() {
		@Override
		public int compare(Document o1, Document o2) {
			return o1.getVersionDate().compareTo(o2.getVersionDate());
		}
	};

	/**
	 * Create a blank document
	 */
	public Document() {

	}

	public Document(DocumentType type, String name, String displayName, String tenantID, String author, JSONObject body) {
		super(type, name, displayName, tenantID, author);
		m_body = body;
	}

    public Document(String json) throws Exception {
    	this(new JSONObject(json));
    }

    public Document(JSONObject o) throws Exception {
        super(o);
    }

    @Override
    protected void build(JSONObject json) throws Exception {
    	super.build(json);
		Object o = json.get(BODY);
		if (o instanceof JSONObject) {
			m_body = json.getJSONObject(BODY);
		}
		else {
			m_body = new JSONObject(o.toString());
		}
    }


    /**
     * @return Document Body
     */
    @JsonIgnore
    public JSONObject getBody() {
    	return m_body;
    }

    @Override
    public String toJSON() {
    	try {
        	//manually add the body
        	JSONObject ret = new JSONObject(super.toJSON());
        	ret.put(BODY, m_body);
        	return ret.toString(4);
    	} catch (Exception e) {
    		throw new DocumentFormatException("error building JSON for document " + getDocumentType() + "/" + getName(), e);
    	}
    }

    @JsonIgnore
    public <T> T getBodyAsObject() throws Exception {
        return (T)getBodyAsObject(getDocHandler());
    }

    @JsonIgnore
    public <T> T getBodyAsObject(Class<T> type) throws Exception {
        return (T)getBodyAsObject(getDocHandler(), type);
    }

    /**
     *
     * @param  docs Optional DocumentHandler for initializable body classes
     * @return
     * @throws Exception
     */
    @JsonIgnore
    public <T> T getBodyAsObject(DocumentHandler docs) throws  Exception{
    	return getBodyAsObject(docs, getBodyClassObject());
    }

    private <T> Class<T> getBodyClassObject() throws Exception {
        if (getBodyClass()  == null || getBodyClass().length() == 0) {
        	return null;
        } else {
        	return (Class<T>)Class.forName(getBodyClass());
        }
    }

    public <T> T getBodyAsObject(DocumentHandler docs, Class<T> type) throws Exception {

    	T ret = null;

    	//return JSONObject if no other information is available
        if (type == null) {
            ret =(T)getBody();
        }

        else {


           Object o = type.newInstance();
            if (o instanceof IBodyInitializable) {

                ((IBodyInitializable) o).initialize(getBodyAsString(), docs);
                ret = (T)o;

            }
            else {
                ret = (T)JSONUtils.objectFromString(getBodyAsString(), o.getClass());
            }

        }

        //initialize the object
        initialize(ret);

        return ret;

    }

    //initialize the body object after creation
    void initialize(Object bodyObject) throws Exception {

    	BeanInfo info = Introspector.getBeanInfo(bodyObject.getClass());
    	for (PropertyDescriptor prop : info.getPropertyDescriptors()) {

    		//if no way to write, continue
    		if (prop.getWriteMethod() == null) {
    			continue;
    		}

    		//else check for annotations

    		//bind doc name
    		if (hasAnnotation(DocName.class, prop.getReadMethod(), prop.getWriteMethod())) {
    			prop.getWriteMethod().invoke(bodyObject, getName());
    		}

    		//bind doc display name
    		else if (hasAnnotation(DocDisplayName.class, prop.getReadMethod(), prop.getWriteMethod())) {
    			prop.getWriteMethod().invoke(bodyObject, getDisplayName());
    		}

    		//bind tenant
    		else if (hasAnnotation(DocTenant.class, prop.getReadMethod(), prop.getWriteMethod())) {
    			prop.getWriteMethod().invoke(bodyObject, Tenant.forId(getTenantID()));
    		}

    	}

    }

    private boolean hasAnnotation(Class<? extends Annotation> annotation, AnnotatedElement... list) {

    	for (AnnotatedElement annotated : list) {
    		if (annotated != null && annotated.isAnnotationPresent(annotation)) {
    			return true;
    		}
    	}

    	return false;

    }

    @JsonIgnore
    public void setBodyFromObject(Object object) throws Exception { m_body = new JSONObject(JSONUtils.objectToString(object)); }

    @JsonIgnore
    public void setBody(JSONObject body) { m_body = body; }
    private JSONObject m_body;

    @JsonIgnore
    public String getBodyAsString() {
    	return m_body == null ? null : m_body.toString();
    }

    @JsonIgnore
    public boolean isBodyEnabled() {
    	return m_body.optBoolean("enabled", true);
    }

	//keys for op sequencing and locking -- to be extracted during base handler logic


	@JsonIgnore
	public String getLockKey() {
		return  getDocumentType() + "-" + getName();
	}

}

