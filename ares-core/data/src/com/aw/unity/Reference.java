package com.aw.unity;

/**
 * Created by scott on 14/06/16.
 */
public class Reference {


	public String getReference_value() {return reference_value;}

	public void setReference_value(String reference_value) {this.reference_value = reference_value;}

	private String reference_value;

	public String getReference_type() {return reference_type;}

	public void setReference_type(String reference_type) {this.reference_type = reference_type;}

	public String getReference_key() {return reference_key;}

	public void setReference_key(String reference_key) {this.reference_key = reference_key;}

	public String getLocal_key() {return local_key;}

	public void setLocal_key(String local_key) {this.local_key = local_key;}

	public boolean isRequired() {return required;}

	public void setRequired(boolean required) {this.required = required;}

	private String reference_type;
	private String reference_key;
	private String local_key;
	private boolean required;

}
