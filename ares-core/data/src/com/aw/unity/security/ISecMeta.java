package com.aw.unity.security;

import com.aw.unity.UnityMetadata;

public interface ISecMeta {

	public UnityMetadata enforceMetaSecurity(UnityMetadata meta, Object secDef) throws Exception;
}
