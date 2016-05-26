package com.aw.unity.security;

public enum ObfuscationRule {

	IS_USER_ANON(null, "User_Name", "User_Domain_Name"),
	IS_COMPUTER_ANON(null, "Machine_Name", "Machine_Domain_Name"),
	IS_EMAIL_ANON("N/A", "SENDER_EMAIL_ADDRESS", null),
	IS_FILE_ANON(null, "DEST_FILE_NAME", "tid"),
	IS_NETWORK_ANON("Network", "NETWORK_ADDRESS", null),
	IS_PRINT_ANON("N/A", "PRINTER_JOBNAME", null);

	ObfuscationRule(String mask, String originalColumn, String replacementColumn){
		this.mask = mask;
		this.originalColumn = originalColumn;
		this.replacementColumn = replacementColumn;
	}

	private final String mask;
	private final String originalColumn;
	private final String replacementColumn;

	public String getMask() {
		return mask;
	}

	public String getOriginalColumn() {
		return originalColumn;
	}

	public String getReplacementColumn() {
		return replacementColumn;
	}



}
