package com.woting.crawler.core.timer.model;

import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woting.crawler.core.timer.BCPlayIsValidateTimerJob;
import com.woting.crawler.core.timer.CrawlerCategoryJob;
import com.woting.crawler.core.timer.CrawlerSrcTimerJob;
import com.woting.crawler.core.timer.PlayNumTimerJob;
import com.woting.crawler.core.timer.ShareTimerJob;
import com.woting.crawler.core.timer.persis.po.TimerPo;
import com.woting.crawler.ext.SpringShell;

public class Timer {
	private Logger logge = LoggerFactory.getLogger(Timer.class);
	private String SrcCronExpression;
	private String PlayCountCronExpression;
	private String CategoryCronExpression;
	private String BCPlayIsValidateCronExpression;
	private String ShareCronExpression;
	private Scheduler scheduler;
	private JobDetailImpl jobdetail1; //抓取专辑声音进程执行的任务
	private CronTriggerImpl cronTrigger1; //抓取专辑声音进程触发器
	private JobDetailImpl jobdetail2; //抓取点击量进程执行的任务
	private CronTriggerImpl cronTrigger2; //抓取点击量进程触发器
	private JobDetailImpl jobdetail3; //抓取分类进程执行的任务
	private CronTriggerImpl cronTrigger3; //抓取分类进程触发器
	private JobDetailImpl jobdetail4; //电台播放地址是否有效检测任务
	private CronTriggerImpl cronTrigger4; //电台播放地址是否有效检测进程触发器
	private JobDetailImpl jobdetail5; //更新分享临时票据触发器
	private CronTriggerImpl cronTrigger5; //更新分享临时票据进程触发器
	
	public Timer(String str) {
		TimerPo timerPo = (TimerPo) SpringShell.getBean("timer");
		this.SrcCronExpression = timerPo.getCronExpression();
		this.PlayCountCronExpression = timerPo.getPlayCountCronExpression();
		this.CategoryCronExpression = timerPo.getCategoryCronExpression();
		this.BCPlayIsValidateCronExpression = timerPo.getBCPlayIsValidateCronExpression();
		this.ShareCronExpression = timerPo.getShareCronExpression();
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
	public String getBCPlayIsValidateCronExpression() {
		return BCPlayIsValidateCronExpression;
	}
	public void setBCPlayIsValidateCronExpression(String bCPlayIsValidateCronExpression) {
		BCPlayIsValidateCronExpression = bCPlayIsValidateCronExpression;
	}
	public String getShareCronExpression() {
		return ShareCronExpression;
	}
	public void setShareCronExpression(String shareCronExpression) {
		ShareCronExpression = shareCronExpression;
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
	public JobDetailImpl getJobdetail1() {
		return jobdetail1;
	}
	public void setJobdetail1(JobDetailImpl jobdetail1) {
		this.jobdetail1 = jobdetail1;
	}
	public CronTriggerImpl getCronTrigger1() {
		return cronTrigger1;
	}
	public void setCronTrigger1(CronTriggerImpl cronTrigger1) {
		this.cronTrigger1 = cronTrigger1;
	}
	public JobDetailImpl getJobdetail4() {
		return jobdetail4;
	}
	public void setJobdetail4(JobDetailImpl jobdetail4) {
		this.jobdetail4 = jobdetail4;
	}
	public CronTriggerImpl getCronTrigger4() {
		return cronTrigger4;
	}
	public void setCronTrigger4(CronTriggerImpl cronTrigger4) {
		this.cronTrigger4 = cronTrigger4;
	}
	public JobDetailImpl getJobdetail5() {
		return jobdetail5;
	}
	public void setJobdetail5(JobDetailImpl jobdetail5) {
		this.jobdetail5 = jobdetail5;
	}
	public CronTriggerImpl getCronTrigger5() {
		return cronTrigger5;
	}
	public void setCronTrigger5(CronTriggerImpl cronTrigger5) {
		this.cronTrigger5 = cronTrigger5;
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
			this.jobdetail4 = new JobDetailImpl("BCPlayIsValidate", "JobGroup4", BCPlayIsValidateTimerJob.class);
			this.cronTrigger4 = new CronTriggerImpl("CronTrigger4", "TriggerGroup4");
			cronTrigger4.setCronExpression(BCPlayIsValidateCronExpression);
			this.jobdetail5 = new JobDetailImpl("Share", "JobGroup5", ShareTimerJob.class);
			this.cronTrigger5 = new CronTriggerImpl("CronTrigger5", "TriggerGroup5");
			cronTrigger5.setCronExpression(ShareCronExpression);
			scheduler.scheduleJob(jobdetail1, cronTrigger1);
			scheduler.scheduleJob(jobdetail2, cronTrigger2);
			scheduler.scheduleJob(jobdetail3, cronTrigger3);
			scheduler.scheduleJob(jobdetail4, cronTrigger4);
			scheduler.scheduleJob(jobdetail5, cronTrigger5);
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
