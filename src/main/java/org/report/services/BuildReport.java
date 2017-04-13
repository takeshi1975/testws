package org.report.services;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public class BuildReport {

	private static final Logger log = Logger.getLogger(BuildReport.class);

	@Value("${app.mail.to}")
	private String mailTo;
	
	@Value("${app.db.user}")
	private String dbuser;
	
	@Value("${app.db.password}")
	private String dbpassword;

	@Value("${app.db.url}")
	private String dburl;

	@Value("${app.db.driver}")
	private String driver;

	@Value("${app.mail.account}")
	private String mailAccount;
	
	@Value("${app.mail.password}")
	private String mailPassword;	

	private Connection getConnection() {

		try {
			Class.forName(driver).newInstance();
			return DriverManager.getConnection(dburl, dbuser, dbpassword);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Date getDateLessOne(){
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}
	
	private void generatePDF(File source, String destFolder) {
		Connection connection = getConnection();
		try {
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			String jasperFile = source.getAbsolutePath().replaceAll(".jrxml", ".jasper"); 
			JasperCompileManager.compileReportToFile(source.getAbsolutePath(),jasperFile);
			log.info("JasperFile -->"+jasperFile);
			Map<String, Object> params = new HashMap<String, Object>();			
			params.put("FECHA", df.format(getDateLessOne()));
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperFile, params, connection);
			String destFileName = destFolder+File.separator+source.getName().replace(".jrxml", ".pdf");
			log.info(destFileName);
			JasperExportManager.exportReportToPdfFile(jasperPrint, destFileName);
		} catch (JRException e) { 
			log.error("Error en la compilación del fichero ",e);
		}
	}

	public String createReports(String sourceFolder, String destFolder) {
		log.info("Se va a crear el report");
		try {			
			List<File> sources = this.listFilesForFolder(new File(sourceFolder),".jrxml");
			sources.forEach(source -> generatePDF(source, destFolder));
		} catch (Exception ex) {
			log.error("Error generando los ficheros pdf", ex);
			return ex.toString();
		}
		return "Reports created";
	}

	private Properties init() {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "mail.gmail.com");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.port", 25);
		properties.put("mail.smtp.mail.sender", "europlayas.alarms@gmail.com");
		properties.put("mail.smtp.user", mailAccount);
		properties.put("mail.smtp.auth", "true");
		return properties;
	}

	private List<File> listFilesForFolder(final File folder, String extension) throws IOException {
		List<File> list = new ArrayList<File>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getName().endsWith(extension)) {
				list.add(fileEntry);
				log.debug(fileEntry);
			} else 
				log.warn("El fichero "+fileEntry.getName()+" no coincide con el patron *"+extension);
		}
		if (list.size()==0)
			log.warn("No se han encontrado ficheros de interes en la carpeta "+folder.getAbsolutePath());
		return list;
	}

	public void sendReport(String destFolder) {
		init();
		try {
			File folder = new File(destFolder);
			List<File> listFilesForFolder = this.listFilesForFolder(folder,".pdf");

			Session session = Session.getDefaultInstance(init(), null);
			Message message = new MimeMessage(session);
			String buffer[] = mailTo.split(",");
			for (int i=0; i<buffer.length; i++){
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(buffer[i].trim()));
			}			
			message.setSubject("Greetings from IT..");
			Multipart multipart = new MimeMultipart();

			// creates body part for the message
			MimeBodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setContent("This is a mail from IT", "text/html");

			// creates body part for the attachment

			for (File f : listFilesForFolder) {
				MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.attachFile(f);
				multipart.addBodyPart(attachPart);
			}
			// adds parts to the multipart
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);
			Transport transport = session.getTransport("smtp");
			// Enter your correct gmail UserID and Password
			// if you have 2FA enabled then provide App Specific Password
			transport.connect("smtp.gmail.com", mailAccount, mailPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			for (File f : listFilesForFolder)
				f.delete();
		} catch (MessagingException me) {
			log.error("Error en el envio de mensaje: ", me);
		} catch (IOException ioex) {
			log.error("Error en la lectura de la carpeta de PDF's", ioex);
		}

	}

}
