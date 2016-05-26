package com.aw.common.task;

import com.aw.document.DocName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * available task types
 *
 *
 *
 */
public class TaskType {

	public TaskType(String typeName, Class<? extends Task> classType) {
		this.typeName = typeName;
		this.classType = classType;
	}

	public TaskType() {
	}

	@DocName
	public String getTypeName() { return this.typeName;  }
	public void setTypeName(String typeName) { this.typeName = typeName; }
	private String typeName;

	//don't ever serialize the class type
	@JsonIgnore
	public Class<? extends Task> getClassType() { return this.classType;  }
	@JsonProperty
	public void setClassType(Class<? extends Task> classType) { this.classType = classType; }
	private Class<? extends Task> classType;

	@Override
	public String toString() {
		return "[taskType=" + typeName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskType other = (TaskType) obj;
		if (typeName == null) {
			if (other.typeName != null)
				return false;
		} else if (!typeName.equals(other.typeName))
			return false;
		return true;
	}

}
