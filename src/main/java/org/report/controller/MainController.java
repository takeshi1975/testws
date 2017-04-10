package org.report.controller;

import org.report.services.BuildReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ComponentScan("org.report.services")
public class MainController {
	
	@Autowired
	private BuildReport buildReport;
	
	@Value("${app.source.folder}")
	private String sourceFolder;
	@Value("${app.dest.folder}")
	private String destFolder;
		
	
	@RequestMapping("/")
	@Scheduled(cron="0 0 9,15 * * ?")
	public String reportController(){
		buildReport.createReports(sourceFolder, destFolder);
		return "The reports are build";
	}
	
	@RequestMapping("/send")
	@Scheduled(cron="0 15 9,15 * * ?")
	public String reportSend(){
		buildReport.sendReport(destFolder);
		return "The reports are sended";
	}

}
