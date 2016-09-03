package com.woting.crawler.core.timer.model;

import java.util.Map;
import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.timer.CrawlerSrcTimerJob;
import com.woting.crawler.core.timer.PlayNumTimerJob;
import com.woting.crawler.scheme.utils.FileUtils;

public class Timer {
	private Logger logge = LoggerFactory.getLogger(Timer.class);
	private String SrcCronExpression;
	private String PlayCountCronExpression;
	private Scheduler scheduler;
	private JobDetailImpl jobdetail1; //抓取专辑声音进程执行的任务
	private CronTriggerImpl cronTrigger1; //抓取专辑声音进程触发器
	private JobDetailImpl jobdetail2; //抓取点击量进程执行的任务
	private CronTriggerImpl cronTrigger2; //抓取点击量进程触发器
	
	@SuppressWarnings("unchecked")
	public Timer(String str) {
		str = FileUtils.readFile(str);
		Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
		this.SrcCronExpression = (String) m.get("CronExpression");
		this.PlayCountCronExpression = (String) m.get("PlayCountCronExpression");
	}
	
	public String getPlayCountCronExpression() {
		return PlayCountCronExpression;
	}

	public void setPlayCountCronExpression(String playCountCronExpression) {
		PlayCountCronExpression = playCountCronExpression;
	}

	public JobDetailImpl getJobdetail2() {
		return jobdetail2;
	}

	public void setJobdetail2(JobDetailImpl jobdetail2) {
		this.jobdetail2 = jobdetail2;
	}

	public CronTriggerImpl getCronTrigger2() {
		return cronTrigger2;
	}

	public void setCronTrigger2(CronTriggerImpl cronTrigger2) {
		this.cronTrigger2 = cronTrigger2;
	}

	public String getSrcCronExpression() {
		return SrcCronExpression;
	}
	
	public void setSrcCronExpression(String srcCronExpression) {
		SrcCronExpression = srcCronExpression;
	}

	@SuppressWarnings("deprecation")
	public Scheduler getScheduler() {
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			this.scheduler = sf.getScheduler();
			this.jobdetail1 = new JobDetailImpl("JobDetail1", "JobGroup1", CrawlerSrcTimerJob.class);
			this.cronTrigger1 = new CronTriggerImpl("CronTrigger1", "TriggerGroup1");
			cronTrigger1.setCronExpression(SrcCronExpression);
			this.jobdetail2 = new JobDetailImpl("JobDetail2", "JobGroup2", PlayNumTimerJob.class);
			this.cronTrigger2 = new CronTriggerImpl("CronTrigger2", "TriggerGroup2");
			cronTrigger2.setCronExpression(PlayCountCronExpression);
			scheduler.scheduleJob(jobdetail1, cronTrigger1);
			scheduler.scheduleJob(jobdetail2, cronTrigger2);
		} catch (Exception e) {
			logge.info("启动时间加载时报错");
			return null;
		}
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
}