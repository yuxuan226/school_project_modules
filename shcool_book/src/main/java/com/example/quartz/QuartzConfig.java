package com.example.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 定时任务配置
 * 只注册 JobDetail 和 Trigger，Spring Boot 的 Quartz 自动配置会自动创建 SchedulerFactoryBean，
 * 并通过 AutoWiringJobsSpringBeanJobFactory 让 Job 实例走 Spring 容器，@Autowired / @Value 正常注入
 */
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail imgQuartzJobDetail() {
        return JobBuilder.newJob(ImgQuartzJob.class)
                .withIdentity("imgQuartzJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger imgQuartzTrigger(JobDetail imgQuartzJobDetail) {
        // 每天凌晨1点执行
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("0 0 1 * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(imgQuartzJobDetail)
                .withIdentity("imgQuartzTrigger")
                .withSchedule(cron)
                .build();
    }
}
