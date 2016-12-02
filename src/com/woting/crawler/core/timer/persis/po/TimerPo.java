package com.woting.crawler.core.timer.persis.po;

public class TimerPo {
	private String CronExpression;
	private String PlayCountCronExpression;
	private String CategoryCronExpression;
	public String getCronExpression() {
		return CronExpression;
	}
	public void setCronExpression(String cronExpression) {
		CronExpression = cronExpression;
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
}
