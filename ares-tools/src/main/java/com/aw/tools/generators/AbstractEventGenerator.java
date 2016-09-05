package com.aw.tools.generators;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import com.aw.common.exceptions.InitializationException;
import com.aw.platform.Platform;
import com.aw.platform.PlatformClient;

import javax.inject.Provider;

public class AbstractEventGenerator extends AbstractGenerator {

	protected static final String[] USERS = {
		"jlehmann",
		"cbalmforth",
		"shill",
		"aron",
		"donaldduck",
	};

	protected static final double[] PROBABILITY = {
		.2,
		.1,
		.4,
		.25,
		.05
	};

	protected static final double[] PRODUCT_NAME_PROBABILITIES = {
		.01,
		.02,
		.03,
		.04,
		.05,
		.10,
		.15,
		.25,
		.35
	};



	static final String PRODUCT_PYTHON = "python";
	static final String PRODUCT_LIMEWIRE = "Limewire";
	static final String PRODUCT_NVIDIA = "NVidia";
	static final String PRODUCT_FACEBOOK = "Facebook";
	static final String PRODUCT_TWITTER = "Twitter";
	static final String PRODUCT_ADOBE = "Adobe";
	static final String PRODUCT_CHROME ="Google Chrome";
	static final String PRODUCT_FIREFOX = "Firefox";
	static final String PRODUCT_WINDOWS = "Microsoft Windows";
	static final String PRODUCT_UNKNOWN = "unknown";

	protected static final String[] FILES = {
		"uhoh.exe",
		"allyourbase.dll",
		"installvirus.exe",
		"chickenpox.exe",
		"tinder.exe",
		"jaimens_passwords.dll",
		"firefox.exe",
		"chrome.exe",
		"cmd.exe",
		"explorer.exe",
	};

	protected static final Map<String, String> FILE_TO_PRODUCT = Collections.unmodifiableMap(new HashMap<String, String>() {

		{
			put(FILES[0], PRODUCT_PYTHON);
			put(FILES[1], PRODUCT_UNKNOWN);
			put(FILES[2], PRODUCT_UNKNOWN);
			put(FILES[3], PRODUCT_UNKNOWN);
			put(FILES[4], PRODUCT_TWITTER);
			put(FILES[5], PRODUCT_LIMEWIRE);
			put(FILES[6], PRODUCT_FIREFOX);
			put(FILES[7], PRODUCT_CHROME);
			put(FILES[8], PRODUCT_WINDOWS);
			put(FILES[9], PRODUCT_WINDOWS);
		}

	});

	protected static final Map<String, String> PRODUCT_TO_COMPANY = Collections.unmodifiableMap(new HashMap<String, String>() {

		{
			put(PRODUCT_PYTHON, "open source");
			put(PRODUCT_UNKNOWN, "unknown");
			put(PRODUCT_UNKNOWN, "unknown");
			put(PRODUCT_UNKNOWN, "unknown");
			put(PRODUCT_TWITTER, "Twitter, Inc");
			put(PRODUCT_LIMEWIRE, "Limewire, Inc");
			put(PRODUCT_FIREFOX, "Mozilla");
			put(PRODUCT_CHROME, "Google");
			put(PRODUCT_WINDOWS, "Microsoft");
			put(PRODUCT_WINDOWS, "Microsoft");
		}

	});

	//pick a file name that will have a hash anomaly
	protected static String FILE_WITH_ANOMALY = FILES[FILES.length-1];

	protected static final double[] FILE_PROBABILITIES = {
		.02,
		.01,
		.005,
		.001,
		.01,
		.01,
		.3,
		.2,
		.15,
		REMAINDER,
	};

	protected static final double[] HASH_PROBABILITIES_NO_ANOMALY = {
		.5,
		.5,
	};

	protected static final double[] HASH_PROBABILITIES_ANOMALY = {
		.01,
		REMAINDER,
	};

	protected static final Map<String, FileHashes> HASHES = Collections.unmodifiableMap(new HashMap<String, FileHashes>() {

		private static final long serialVersionUID = 1L;

		{

			//generate hashes per file
			for (String file : FILES) {

				double[] probabilities = HASH_PROBABILITIES_NO_ANOMALY;
				if (FILE_WITH_ANOMALY.equals(file)) {
					probabilities = HASH_PROBABILITIES_ANOMALY;
				}
				put(file, new FileHashes(file, probabilities));

			}

		}

	});

	public AbstractEventGenerator(Provider<Platform> platform) {
		setClient(
			new PlatformClient(platform)

		);
	}

	protected String randomUser() {
		return random(USERS);
	}

	protected String randomFile() {
		return random(FILES, FILE_PROBABILITIES);
	}

	protected String getCompany(String product) {
		String ret = PRODUCT_TO_COMPANY.get(product);
		if (ret == null) {
			throw new RuntimeException("could not find company for product " + product);
		}
		return ret;
	}

	protected String getProduct(String filename) {
		String ret = FILE_TO_PRODUCT.get(filename);
		return ret;
	}

	protected String getHash(String filename) {

		FileHashes hashes = HASHES.get(filename);
		return random(hashes.getHashes(), hashes.getProbabilities());

	}

	private static String[] generateHashes(String filename, int count) throws Exception {
		String[] ret = new String[count];

		for (int x=0; x<count; x++) {

			String md5 = DigestUtils.md5Hex(filename + " this " + x + " is a string of junk to build an md5 " + count + " - " + x);
			ret[x] = md5;

		}

		return ret;
	}

	protected String randomMachineId(int maxMachines) {
		return "machine-id-" + randomInt(0, maxMachines);
	}

	protected String getFilename(String md5) {
		for (Map.Entry<String, FileHashes> entry : HASHES.entrySet()) {
			if (entry.getValue().m_hashSet.contains(md5)) {
				return entry.getKey();
			}
		}
		throw new RuntimeException("couldn't find filename for md5 " + md5);
	}

	//settings for anomalous data
	protected String getAnomalousUser() {
		return USERS[USERS.length - 1]; //last user in array by default
	}
	protected int getAnomalousDayOfMonth() {
		return 14;
	}
	protected int getAnomalousHourOfDay() {
		return 10;
	}



	//generated hashes for a given filename
	protected static class FileHashes {

		public FileHashes(String filename, double[] probabilities) {

			try {

				m_filename = filename;
				m_probabilities = probabilities;

				//generate hashes
				m_hashes = generateHashes(filename, probabilities.length);
				m_hashSet = new HashSet<String>(Arrays.asList(m_hashes));

			} catch (Exception e) {
				throw new InitializationException("error creating file hashes", e);
			}

		}

		String getFilename() { return m_filename; }
		private String m_filename = null;

		private String[] getHashes() { return m_hashes; }
		private String[] m_hashes= null;

		double[] getProbabilities() { return m_probabilities; }
		private double[] m_probabilities = null;

		private Set<String> m_hashSet = null;

	}

	public String getTenantID() { return m_tenantID; }
	public void setTenantID(String tenantID) { m_tenantID = tenantID; }
	private String m_tenantID;

	public PlatformClient getClient() { return m_client; }
	public void setClient(PlatformClient client) { m_client = client; }
	private PlatformClient m_client;

}

