package com.aw.platform.monitoring.os;

import com.aw.platform.NodeRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.math3.util.Precision;


import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * POJO for stats obtained from sysstat
 */
public class OSPerfStats
{

	public static final String UNITY_TYPE = "perf_stat";
	@JsonProperty("dg_utype")
	public String getUnityType() { return OSPerfStats.UNITY_TYPE; }

	@JsonProperty("dg_guid")
	public String getGuid(){
		return getHostname() + "-" + getDgtime().toEpochMilli();
	}

	//cpu used % as a get
	@JsonProperty("stat_pct_cpu_used")
	public float getPctCpuUsed ()
	{
		DecimalFormat df = new DecimalFormat("#.##");
		Float f = Precision.round(100 - getPctidle(), 2);

		return f;

	}

	//time property for unity
	@JsonProperty("dg_time")
	public Instant getDgtime ()
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
		TemporalAccessor inst = formatter.withZone(ZoneId.of("UTC")).parse(getTimestamp().replace("UTC", "").trim());
		return Instant.from(inst);

	}


	@JsonProperty("stat_timestamp")
	public String getTimestamp() {return timestamp;}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;}
	private String timestamp;

	@JsonProperty("stat_hostname")
	public String getHostname () {return hostname;}
	public void setHostname (String hostname) {this.hostname = hostname;}
	private String hostname;

//Performance statistics

	@JsonProperty("stat_pctidle")
	public float getPctidle()
	{
		return pctidle;
	}
	public void setPctidle (float pctidle)
	{
		this.pctidle = pctidle;
	}
	private float pctidle;

	@JsonProperty("stat_pctmemused")
	public float getPctmemused() {return pctmemused;}
	public void setPctmemused(float pctmemused) {this.pctmemused = pctmemused;}
	private float pctmemused;

	@JsonProperty("stat_kbmemtotal")
	public long getKbtotalmem() {
		float totalmem =  (kbmemused + kbmemfree);
		return (long) totalmem;
	}

	@JsonProperty("stat_kbmemused")
	public long getKbmemused()
	{
		return kbmemused;
	}
	public void GetKbmemused (long kbmemused)
	{
		this.kbmemused = kbmemused;
	}
	private long kbmemused;


	@JsonProperty("stat_kbmemfree")
	public long getKbmemfree()
	{
		return kbmemfree;
	}
	public void GetKbmemfree (long kbmemfree)
	{
		this.kbmemfree = kbmemfree;
	}
	private long kbmemfree;




	@JsonProperty("stat_pctswpused")
	public float getPctSwpused() {return pctswpused;}
	public void setPctSwpused (float pctswpused) {this.pctswpused = pctswpused;}
	private float pctswpused;

//**********IO
	@JsonProperty("stat_pctiowait")
	public float getPctiowait()
	{
		return pctiowait;
	}
	public void setPctiowait (float pctiowait)
	{
		this.pctiowait = pctiowait;
	}
	private float pctiowait;

/*	//@JsonProperty("io_tps")
	public float getTps() {return tps;}
	public void setTps (float tps) {this.tps = tps;}
	private float tps;

	//@JsonProperty("io_read_tps")
	public float getRtps()
	{
		return rtps;
	}
	public void setRtps (float rtps)
	{
		this.rtps = rtps;
	}
	private float rtps;

	//@JsonProperty("io_write_tps")
	public float getWtps() {return wtps;}
	public void setWtps (float wtps)
	{
		this.wtps = wtps;
	}
	private float wtps;

	//bytes read per sec
	//@JsonProperty("io_byte_read_ps")
	public float getBread()
	{
		return bread;
	}
	public void setBread (float bread)
	{
		this.bread = bread;
	}
	private float bread;

	//bytes written per sec
	//@JsonProperty("io_byte_write_ps")
	public float getBwrtn()
	{
		return bwrtn;
	}
	public void setBwrtn (float bwrtn)
	{
		this.bwrtn = bwrtn;
	}
	private float bwrtn;*/

//*************	Network
	@JsonProperty("stat_nettxkb")
	public float getNettxkb()
	{
		return nettxkb;
	}
	public void setNettxkb (float nettxkb)
	{
		this.nettxkb = nettxkb;
	}
	private float nettxkb;

	@JsonProperty("stat_netrxkb")
	public float getNetrxkb()
	{
		return netrxkb;
	}
	public void setNetrxkb (float netrxkb)
	{
		this.netrxkb = netrxkb;
	}
	private float netrxkb;




//Disk Space (from java.io for DG Data dir

	@JsonProperty("stat_diskfreepct")
	public float getDiskfreepct() {
		return diskfreepct;
	}
	public void setDiskfreepct(float diskfreepct) {
		this.diskfreepct = diskfreepct;
	}
	private float  diskfreepct;

	@JsonProperty("stat_diskfreemb")
	public long getDiskfreemb() {
		return diskfreemb;
	}
	public void setDiskfreemb(long diskfreemb) {
		this.diskfreemb = diskfreemb;
	}
	private long  diskfreemb;

	@JsonProperty("stat_numprocs")
	public long getNumprocs() {
		return numprocs;
	}
	public void setNumprocs(long numprocs) {
		this.numprocs = numprocs;
	}
	private long  numprocs;

	@JsonProperty("stat_node_roles")
	public List<NodeRole> getNodeRoleList() {return nodeRoleList;}
	public void setNodeRoleList(List<NodeRole> nodeRoleList) {this.nodeRoleList = nodeRoleList;}
	private List<NodeRole> nodeRoleList;



	@JsonProperty("stat_net_usage")
	private  List<Map<String, Object>> netUsageDetails = new ArrayList<>();
	public List<Map<String, Object>>  getNetUsageDetails() {return netUsageDetails;}
	public void setNetUsageDetails(List<Map<String, Object> > netUsageDetails) {this.netUsageDetails = netUsageDetails;}

	@JsonProperty("stat_net_errors")
	private  List<Map<String, Object>> netErrorDetails = new ArrayList<>();
	public List<Map<String, Object>>  getNetErrorDetails() {return netErrorDetails;}
	public void setNetErrorDetails(List<Map<String, Object> > netErrorDetails) {this.netErrorDetails = netErrorDetails;}




//UNUSED stats are commented out for clarity -- to be added when needed
/*

//CPU detail stats
	private int CPU;

	private float pctnice;
	public float getPctnice() {return pctnice;}
	public void setPctnice (float pctnice) {this.pctnice = pctnice;}

	public float getPctsteal()
	{
		return pctsteal;
	}
	public void setPctsteal (float pctsteal)
	{
		this.pctsteal = pctsteal;
	}
	private float pctsteal;



	public float getPctuser()
	{
		return pctuser;
	}
	public void setPctuser (float pctuser)
	{
		this.pctuser = pctuser;
	}
	private float pctuser;

	public float getPctproc() {return pctproc;}
	public void setPctProc (float pctproc) {this.pctproc = pctproc;}
	private float pctproc;

	private float pctsystem;
	public float getPctsystem() {return pctsystem;}
	public void setPctsystem (float pctsystem) {this.pctsystem = pctsystem;}

	public float getPctiowait()
	{
		return pctiowait;
	}
	public void setPctiowait (float pctiowait)
	{
		this.pctiowait = pctiowait;
	}
	private float pctiowait;


	//VM-other CPU stats
	private float pctguest;
	public float getPctguest() {return pctguest;}
	public void setPctguest(float pctguest) {this.pctguest = pctguest;}
	public float getPctgnice() {return pctgnice;}
	public void setPctgnice(float pctgnice) {this.pctgnice = pctgnice;}
	public float getPctirq() {return pctirq;}
	public void setPctirq(float pctirq) {this.pctirq = pctirq;}
	public float getPctsoft() {return pctsoft;}
	public void setPctsoft(float pctsoft) {this.pctsoft = pctsoft;}

	private float pctgnice;
	private float pctirq;
	private float pctsoft;


	//still unclasssified TODO: classify and organize

	private long kbswpcad;
	private float majflt;
	private long filenr;
	private long blocked;
	private long kbswpfree;
	private long dentunusd;
	private float pswpout;
	private long ptynr;
	private float fault;
	private float ldavg1;
	private long kbdirty;
	private String inodenr;
	private float ldavg5;
	private long kbinact;
	private String plistsz;
	private String pgsteal;
	private long interval;
	private float pgscank;

	private float pgscand;
	private float frmpg;
	private long kbbuffers;
	private float campg;
	private float pgpgin;
	private float vmeff;
	private float pctcommit;
	private float pctswpcad;
	private float pgfree;
	private float pgpgout;
	private float cswch;
	private float bufpg;
	private float pswpin;
	private long runqsz;

	public float getPgscank()
	{
		return pgscank;
	}
	public void setPgscank (float pgscank)
	{
		this.pgscank = pgscank;
	}

	public long getKbmemused()
	{
		return kbmemused;
	}
	public void setKbmemused (long kbmemused)
	{
		this.kbmemused = kbmemused;
	}

	public float getPgscand()
	{
		return pgscand;
	}
	public void setPgscand (float pgscand)
	{
		this.pgscand = pgscand;
	}

	public float getFrmpg()
	{
		return frmpg;
	}
	public void setFrmpg (float frmpg)
	{
		this.frmpg = frmpg;
	}



	public long getKbbuffers()
	{
		return kbbuffers;
	}
	public void setKbbuffers (long kbbuffers)
	{
		this.kbbuffers = kbbuffers;
	}

	public float getCampg()
	{
		return campg;
	}
	public void setCampg (float campg)
	{
		this.campg = campg;
	}

	public float getPgpgin()
	{
		return pgpgin;
	}
	public void setPgpgin (float pgpgin) {this.pgpgin = pgpgin;}

	public float getVmeff()
	{
		return vmeff;
	}
	public void setVmeff (float vmeff)
	{
		this.vmeff = vmeff;
	}

	public float getPctcommit()
	{
		return pctcommit;
	}
	public void setPctcommit (float pct)
	{
		this.pctcommit = pctcommit;
	}


	public float getPctswpcad()
	{
		return pctswpcad;
	}
	public void setPctswpcad (float pctswpcad)
	{
		this.pctswpcad = pctswpcad;
	}

	public float getPgfree()
	{
		return pgfree;
	}
	public void setPgfree (float pgfree)
	{
		this.pgfree = pgfree;
	}

	public float getPgpgout()
	{
		return pgpgout;
	}
	public void setPgpgout (float pgpgout)
	{
		this.pgpgout = pgpgout;
	}

	public float getCswch()
	{
		return cswch;
	}
	public void setCswch (float cswch)
	{
		this.cswch = cswch;
	}

	public float getBufpg()
	{
		return bufpg;
	}

	public void setBufpg (float bufpg)
	{
		this.bufpg = bufpg;
	}
	public float getPswpin()
	{
		return pswpin;
	}
	public void setPswpin (float pswpin)
	{
		this.pswpin = pswpin;
	}

	public long getRunqsz()
	{
		return runqsz;
	}

	public void setRunqsz (long runqsz)
	{
		this.runqsz = runqsz;
	}



	public long getKbswpcad()
	{
		return kbswpcad;
	}
	public void setKbswpcad (long kbswpcad)
	{
		this.kbswpcad = kbswpcad;
	}

	public float getMajflt()
	{
		return majflt;
	}
	public void setMajflt (float majflt)
	{
		this.majflt = majflt;
	}


	public long getKbinact()
	{
		return kbinact;
	}

	public void setKbinact (long kbinact)
	{
		this.kbinact = kbinact;
	}

	public String getPlistsz ()
	{
		return plistsz;
	}
	public void setPlistsz (String plistsz)
	{
		this.plistsz = plistsz;
	}

	public String getPgsteal ()
	{
		return pgsteal;
	}
	public void setPgsteal (String pgsteal)
	{
		this.pgsteal = pgsteal;
	}

	public long getInterval()
	{
		return interval;
	}

	public void setInterval (long interval)
	{
		this.interval = interval;
	}



	public int getcpu()
	{
		return CPU;
	}
	public void setcpu(int CPU)
	{
		this.CPU = CPU;
	}

	public long getFilenr()
	{
		return filenr;
	}

	public void setFilenr (long filenr)
	{
		this.filenr = filenr;
	}

	public float getLdavg15()
	{
		return ldavg15;
	}
	public void setLdavg15 (float ldavg15)
	{
		this.ldavg15 = ldavg15;
	}
	private float ldavg15;

	public float getLdavg5()
	{
		return ldavg5;
	}
	public void setLdavg5 (float ldavg5)
	{
		this.ldavg5 = ldavg5;
	}

	public float getLdavg1()
	{
		return ldavg1;
	}
	public void setLdavg1 (float ldavg1)
	{
		this.ldavg1 = ldavg1;
	}



	//Memory detail stats
	private long kbactive;
	private long kbswpused;
	private long kbmemfree;
	private long kbcommit;
	private long kbcached;
	public long getKbactive()
	{
		return kbactive;
	}
	public void setKbactive (long kbactive)
	{
		this.kbactive = kbactive;
	}
	public long getKbswpused()
	{
		return kbswpused;
	}
	public void setKbswpused (long kbswpused)
	{
		this.kbswpused = kbswpused;
	}
	public long getKbmemfree()
	{
		return kbmemfree;
	}
	public void setKbmemfree (long kbmemfree)
	{
		this.kbmemfree = kbmemfree;
	}
	public long getKbcommit()
	{
		return kbcommit;
	}
	public void setKbcommit (long kbcommit)
	{
		this.kbcommit = kbcommit;
	}
	public long getKbcached()
	{
		return kbcached;
	}
	public void setKbcached (long kbcached)
	{
		this.kbcached = kbcached;
	}
	public long getBlocked()
	{
		return blocked;
	}
	public void setBlocked (long blocked)
	{
		this.blocked = blocked;
	}
	public long getKbswpfree()
	{
		return kbswpfree;
	}
	public void setKbswpfree (long kbswpfree)
	{
		this.kbswpfree = kbswpfree;
	}
	public long getDentunusd()
	{
		return dentunusd;
	}
	public void setDentunusd (long dentunusd)
	{
		this.dentunusd = dentunusd;
	}



	public float getPswpout()
	{
		return pswpout;
	}
	public void setPswpout (float pswpout)
	{
		this.pswpout = pswpout;
	}

	public long getPtynr() {return ptynr;}
	public void setPtynr (long ptynr) {this.ptynr = ptynr;}


	public float getFault()
	{
		return fault;
	}
	public void setFault (float fault)
	{
		this.fault = fault;
	}




	public long getKbdirty()
	{
		return kbdirty;
	}
	public void setKbdirty (long kbdirty)
	{
		this.kbdirty = kbdirty;
	}
	public String getInodenr ()
	{return inodenr;
	}
	public void setInodenr (String inodenr)
	{this.inodenr = inodenr;}

*/





}
