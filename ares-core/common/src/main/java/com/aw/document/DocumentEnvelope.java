package com.aw.document;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.aw.common.Tag;
import com.aw.common.Taggable;
import com.aw.common.rest.security.PlatformSecurityContext;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.system.scope.ResourceScope;

import org.codehaus.jettison.json.JSONObject;

import com.aw.common.util.JSONUtils;
import com.aw.common.util.JSONable;
import com.aw.document.security.DocumentPermission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class used as single point of interaction with a Document envelope (document sans body)
 */


public class DocumentEnvelope implements JSONable, Taggable {

	private final Set<Tag> tags = new HashSet<>();
	private String grouping;

	//empty envelope
	public DocumentEnvelope() {
	}

	public DocumentEnvelope(DocumentType type, String name, String displayName, String tenantID, String author) {
		m_type = type;
		m_name = name;
		m_display_name = displayName;
		m_tenant_id = tenantID;
		m_author = author;
	}

    public String toString() {
    	return toJSON();
    }

    public String toJSON()  {
        try {
            JSONObject jsonObject = new JSONObject(JSONUtils.objectToString(this, false, true));
            PlatformSecurityContext securityContext = ThreadLocalStore.get();
            if ( securityContext != null ) {
                String tenantID = securityContext.getTenantID();
                if ( "0".equals(tenantID) ) {
                    jsonObject.put("body_class", getBodyClass() );
                }
            }
            return jsonObject.toString(4);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing document " + getName() + " to json", e);
        }


    }


    //TODO: allow JSON object ingestion
    public DocumentEnvelope(String json) throws Exception {
        this(new JSONObject(json));
    }

    //TODO -- make better
    public DocumentEnvelope(JSONObject o) throws Exception {
        build(o);
    }

    public void initialize(String json) throws Exception {
    	build(new JSONObject(json));
    }

    protected void build(JSONObject json) throws Exception {

        //set our properties
        JSONUtils.updateFromString(json.toString(), this);

        validate();

    }




    //TODO -- make abstract/factory or some other way to allow type-specific validation
    private void validate() {
        //TODO -- define exact validation rules


        return;

    }


    /**
     * @return Document Type
     */
    @JsonProperty("type")
    public DocumentType getDocumentType() { return m_type; }
    public void setType(DocumentType type) { m_type = type; }
    private DocumentType m_type;

    /**
     * @return Document body class
     */
   // @JsonIgnore
    public String getBodyClass() { return m_body_class; }
    @JsonProperty
    public void setBodyClass(String body_class) { m_body_class = body_class; }
    private String m_body_class;


    /**
     * @return String
     */
   // @JsonIgnore
    public String getTenantID() { return m_tenant_id; }
    @JsonProperty
    public void setTenantID(String tenant_id) { m_tenant_id = tenant_id; }
    private String m_tenant_id;

    /**
     * @return Document GUID
     */
    public String getID() { return m_id; }
    public void setID(String id) { m_id = id; }
    private String m_id;

    /**
     * @return Document Name (Semantic)
     */
    public String getName() { return m_name; }
    public void setName(String name) { m_name = name; }
    private String m_name;


    /**
     * @return Document Author
     */
    public String getAuthor() { return m_author; }
    public void setAuthor(String author) { m_author = author; }
    private String m_author;

    /**
     * @return Document Version
     */
    public int getVersion() { return m_version; }
    public void setVersion(int version) { m_version = version; }
    private int m_version = 1;

    /**
     * @return Document Display Name
     */
    @JsonProperty("display_name")
    public String getDisplayName() { return m_display_name; }
    public void setDisplayName(String display_name) { m_display_name = display_name; }
    private String m_display_name;

    /**
     * @return Document Description
     */
    public String getDescription() { return m_description; }
    public void setDescription(String description) { m_description = description; }
    private String m_description;


    /**
     * @return LUD user
     */
    @JsonProperty("version_author")
    public String getVersionAuthor() { return m_version_author; }
    public void setVersionAuthor(String lud_user) { m_version_author = lud_user; }
    private String m_version_author;


    /**
     * @return Created
     */
    @JsonProperty("version_date")
    public Date getVersionDate() { return m_version_date; }
    public void setVersionDate(Date version_date) { m_version_date = version_date; }
    private Date m_version_date = new Date(); //by default document instantiation time


    /**
     * @return boolean
     * Flag to mark object deleted as opposed to physical delete
     */
    @JsonIgnore
    public Boolean getDeleted() { return m_deleted; }
    @JsonProperty
    public void setDeleted(Boolean deleted) {
		m_deleted = deleted;
	}
    private Boolean m_deleted = false;

    @JsonProperty("is_current")
    public Boolean getIsCurrent() { return m_current; }
    public void setIsCurrent(Boolean current) { m_current = current; }
    private Boolean m_current;

	@JsonProperty("grouping")
	public String getGrouping() {
		return grouping;
	}

	public void setGrouping(String grouping) {
		this.grouping = grouping;
	}

	/**
	 * @return DocumentPermission
	 */
	@JsonProperty("perm_read")
	public DocumentPermission getReadPerm() { return m_perm_read; }
	public void setReadPerm(DocumentPermission perm_read) { m_perm_read = perm_read; }
    private DocumentPermission m_perm_read;

    /**
     * @return DocumentPermission
     */
    @JsonProperty("perm_write")
    public DocumentPermission getWritePerm() { return m_perm_write; }
    public void setWritePerm(DocumentPermission perm_write) { m_perm_write = perm_write; }
    private DocumentPermission m_perm_write;

    /**
     * @return String[]
     */
    @JsonProperty("dependencies_guid")
    public String[] getDependenciesGUID() { return m_dependencies_guid; }
    public void setDependenciesGUID(String[] dependencies_guid) { m_dependencies_guid = dependencies_guid; }
    private String[] m_dependencies_guid;

    /**
     * @return String[]
     */
    @JsonProperty("dependencies_name")
    public String[] getDependenciesName() { return m_dependencies_name; }
    public void setDependenciesName(String[] dependencies_name) { m_dependencies_name = dependencies_name; }
    private String[] m_dependencies_name;


	@JsonProperty("op_sequence")
	public long getOpSequence() {return m_op_sequence;}
	public void setOpSequence(long m_op_sequence) {this.m_op_sequence = m_op_sequence;}
	private long m_op_sequence;

	/**
	 * The scope of a document tells the system whether the specific document should be propagated to tenants when a tenant is provisioned.
	 * The document type scope will be checked first, and if that scope allows the document to be propagated to tenants, the scope property
	 * on the document itself will be checked.
	 *
	 * @return the scope of this document
	 */
	public ResourceScope getScope() { return this.scope;  }
	public void setScope(ResourceScope scope) { this.scope = scope; }
	private ResourceScope scope = ResourceScope.ALL;

	/**
	 * @return the originating document handler for this envelope
	 */
    @JsonIgnore
    public DocumentHandler getDocHandler() { return m_docHandler; }
    @JsonIgnore
	public void setDocHandler(DocumentHandler docHandler) { m_docHandler = docHandler; }
	private DocumentHandler m_docHandler;

	@Override
	public Set<Tag> getTags() {
		return Collections.unmodifiableSet(tags);
	}

	public void setTags(Set<Tag> tags) {
		this.tags.addAll(tags);
	}

	@Override
	public void addTag(Tag tag) {
		tags.add(tag);
	}

	@Override
	public void removeTag(Tag tag) {
		tags.remove(tag);
	}

	@Override
	public void addAll(Tag... tags) {
		if (tags != null) {
			for (Tag tag : tags) {
				this.tags.add(tag);
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DocumentEnvelope that = (DocumentEnvelope) o;
		return m_type == that.m_type &&
			Objects.equals(m_id, that.m_id) &&
			Objects.equals(m_name, that.m_name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_type, m_id, m_name);
	}

	@JsonIgnore
	public String getKey() {
		return getTenantID() + "-" + getDocumentType() + "-" + getName() + "-" +  getVersion();
	}

}



