package com.woting.crawler.core.timer.model;

import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.crawler.core.timer.CrawlerCategoryJob;
import com.woting.crawler.core.timer.CrawlerSrcTimerJob;
import com.woting.crawler.core.timer.PlayNumTimerJob;
import com.woting.crawler.core.timer.persis.po.TimerPo;
import com.woting.crawler.ext.SpringShell;

public class Timer {
	private Logger logge = LoggerFactory.getLogger(Timer.class);
	private String SrcCronExpression;
	private String PlayCountCronExpression;
	private String CategoryCronExpression;
	private Scheduler scheduler;
	private JobDetailImpl jobdetail1; //抓取专辑声音进程执行的任务
	private CronTriggerImpl cronTrigger1; //抓取专辑声音进程触发器
	private JobDetailImpl jobdetail2; //抓取点击量进程执行的任务
	private CronTriggerImpl cronTrigger2; //抓取点击量进程触发器
	private JobDetailImpl jobdetail3; //抓取分类进程执行的任务
	private CronTriggerImpl cronTrigger3; //抓取分类进程触发器
	
	public Timer(String str) {
//		str = FileUtils.readFile(str);
//		Map<String, Object> m = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
		TimerPo timerPo = (TimerPo) SpringShell.getBean("timer");
		System.out.println(JsonUtils.objToJson(timerPo));
		this.SrcCronExpression = timerPo.getCronExpression();// (String) m.get("CronExpression");
		this.PlayCountCronExpression =timerPo.getPlayCountCronExpression();// (String) m.get("PlayCountCronExpression");
		this.CategoryCronExpression =timerPo.getCategoryCronExpression();// (String) m.get("CategoryCronExpression");
	}
	
	public String getPlayCountCronExpression() {
		return PlayCountCronExpression;
	}

	public void setPlayCountCronExpression(String playCountCronExpression) {
		PlayCountCronExpression = playCountCronExpression;
	}
	
	public String getCategoryCronExpression() {
		return CategoryCronExpression;
	}

	public void setCategoryCronExpression(String categoryCronExpression) {
		CategoryCronExpression = categoryCronExpression;
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

	public JobDetailImpl getJobdetail3() {
		return jobdetail3;
	}

	public void setJobdetail3(JobDetailImpl jobdetail3) {
		this.jobdetail3 = jobdetail3;
	}

	public CronTriggerImpl getCronTrigger3() {
		return cronTrigger3;
	}

	public void setCronTrigger3(CronTriggerImpl cronTrigger3) {
		this.cronTrigger3 = cronTrigger3;
	}

	@SuppressWarnings("deprecation")
	public Scheduler getScheduler() {
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			this.scheduler = sf.getScheduler();
			this.jobdetail1 = new JobDetailImpl("CrawlerSrc", "JobGroup1", CrawlerSrcTimerJob.class);
			this.cronTrigger1 = new CronTriggerImpl("CronTrigger1", "TriggerGroup1");
			cronTrigger1.setCronExpression(SrcCronExpression);
			this.jobdetail2 = new JobDetailImpl("CrawlerPlayNum", "JobGroup2", PlayNumTimerJob.class);
			this.cronTrigger2 = new CronTriggerImpl("CronTrigger2", "TriggerGroup2");
			cronTrigger2.setCronExpression(PlayCountCronExpression);
			this.jobdetail3 = new JobDetailImpl("CrawlerCategory", "JobGroup3", CrawlerCategoryJob.class);
			this.cronTrigger3 = new CronTriggerImpl("CronTrigger3", "TriggerGroup3");
			cronTrigger3.setCronExpression(CategoryCronExpression);
			scheduler.scheduleJob(jobdetail1, cronTrigger1);
			scheduler.scheduleJob(jobdetail2, cronTrigger2);
			scheduler.scheduleJob(jobdetail3, cronTrigger3);
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
