package inter;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;

public class SendMail {
	// 成员变量中设置发件默认设置
	// 发件人的 邮箱 和 密码（记得替换为自己的邮箱和密码和收件人列表，调试时不要一直发给roy哦~）
	// PS: 某些邮箱服务器为了增加邮箱本身密码的安全性，给 SMTP 客户端设置了独立密码（有的邮箱称为“授权码”）,
	// 对于开启了独立密码的邮箱, 这里的邮箱密码必需使用这个独立密码（授权码）。
	public String myEmailAccount = "2956197249@qq.com";
	public String myEmailPassword = "qqhdckmfolsgdfhi";

	// 发件人邮箱的 SMTP 服务器地址, 必须准确, 不同邮件服务器地址不同, 一般(只是一般, 绝非绝对)格式为: smtp.xxx.com
	// 网易163邮箱的 SMTP 服务器地址为: smtp.163.com QQ邮箱为smtp.qq.com 在各大邮箱的设置中能够找到。
	public String myEmailSMTPHost = "smtp.qq.com";
	// 收件人邮箱（替换为自己知道的有效邮箱）
	public String receiveMailAccount = "zhangmuzhuo969@163.com";
	// 昵称,尽量设定，避免被识别为垃圾邮件  发件人信息
	public String nickname="张慕卓";
	// 抄送名单
	public String copyMailAccount = "";
	// 标题
	public String title = "我的第一次邮箱测试报告！";
	// 属性
	public Properties props;

	/**
	 * 基于成员变量,完成属性设置，从而完成邮件初始化设置
	 * @param
	 * @return
	 */
	public void initMail() {
		// 1. 创建参数配置, 用于连接邮件服务器的参数配置
		props = new Properties(); // 参数配置
		props.setProperty("mail.transport.protocol", "smtp"); // 使用的协议（JavaMail规范要求）
		props.setProperty("mail.smtp.host", myEmailSMTPHost); // 发件人的邮箱的 SMTP 服务器地址
		props.setProperty("mail.smtp.auth", "true"); // 需要请求认证

		// PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接, 也可以自己开启),
		// SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,
		// 需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助,
		// QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看)
		final String smtpPort = "465";
		props.setProperty("mail.smtp.port", smtpPort);
		props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.socketFactory.port", smtpPort);
	}

	public void send(String content,String... attachments) throws Exception {
		//通过session对象创建连接
		Session session = Session.getInstance(props);
		session.setDebug(false); // 设置为debug模式, 可以查看详细的发送 log

		// 3. 通过message对象创建邮件内容
		MimeMessage message = createMimeMessage(session, content, attachments);

		// 4. 根据 Session 获取邮件传输对象
		Transport transport = session.getTransport();

		// 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
		//
		// PS_01: 成败的判断关键在此一句, 如果连接服务器失败, 都会在控制台输出相应失败原因的 log,
		// 仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接, 根据给出的错误
		// 类型到对应邮件服务器的帮助网站上查看具体失败原因。
		//
		// PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
		// (1) 邮箱没有开启 SMTP 服务;
		// (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
		// (3) 邮箱服务器要求必须要使用 SSL 安全连接;
		// (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
		// (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
		//
		// PS_03: 仔细看log, 认真看log, 看懂log, 错误原因都在log已说明。
		transport.connect(myEmailAccount, myEmailPassword);

		// 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人,
		// 密送人
		transport.sendMessage(message, message.getAllRecipients());

		// 7. 关闭连接
		transport.close();

	}

	/**
	 * 创建一封只包含文本的简单邮件
	 *
	 * @param session
	 *            和服务器交互的会话
	 * @param
	 *
	 * @return
	 * @throws Exception
	 */
	public MimeMessage createMimeMessage(Session session, String content,String... attachments)
			throws Exception {
		// 1. 创建一封邮件
		MimeMessage message = new MimeMessage(session);

		// 2. From: 发件人（昵称有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改昵称）
		message.setFrom(new InternetAddress(myEmailAccount, nickname, "UTF-8"));

		// 3. To: 收件人（可以增加多个收件人、抄送、密送）
		// 设置多个发送地址
		try {
			if (null != receiveMailAccount && !receiveMailAccount.isEmpty()) {
				new InternetAddress();
				InternetAddress[] internetAddressTo = InternetAddress.parse(receiveMailAccount);
				message.setRecipients(MimeMessage.RecipientType.TO, internetAddressTo);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 设置多个抄送地址
		System.out.println(copyMailAccount);
		try {
			if (null != copyMailAccount && !copyMailAccount.isEmpty()) {
				new InternetAddress();
				InternetAddress[] internetAddressCC = InternetAddress.parse(copyMailAccount);
				message.setRecipients(MimeMessage.RecipientType.CC, internetAddressCC);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 4. Subject: 邮件主题（标题有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改标题）
		message.setSubject(title, "UTF-8");
		
		/**
		 * 邮件内容设置，包括正文和附件
		 */
		//Mulitpart设置多种格式节点的邮件内容，形成一个混合节点
		MimeMultipart allmm=new MimeMultipart();
		
		// 5. text: 邮件正文（可以使用html标签）（内容有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改发送内容）
		//创建正文节点
		MimeBodyPart text=new MimeBodyPart();
		//设置节点的内容为文本
		text.setContent(content, "text/html;charset=UTF-8");
		//将正文节点添加到multipart中
		allmm.addBodyPart(text);
		
		// 6. attachments：附件列表发送
		for(String attachment:attachments) {
			//创建一个附件节点
			MimeBodyPart attach=new MimeBodyPart();
			//读取本地文件
			DataHandler attachContent=new DataHandler(new FileDataSource(attachment));
			//将文件添加到附件节点
			attach.setDataHandler(attachContent);
			//设置附件的文件名
			attach.setFileName(MimeUtility.encodeText(attachContent.getName()));
			//将附件节点添加到混合节点multipart中
			allmm.addBodyPart(attach);
		}
		
		//设置邮件内容为编好的混合节点
		message.setContent(allmm);

		// 6. 设置发件时间
		message.setSentDate(new Date());

		// 7. 保存设置
		message.saveChanges();

		return message;
	}

}