package cn.itcast.bos.web.action;

import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionContext;

import cn.itcast.bos.constant.Constants;
import cn.itcast.bos.utils.MailUtils;
import cn.itcast.crm.domain.Customer;

@ParentPackage("json-default")
@Namespace("/")
@Controller
@Scope("prototype")
public class CustomerAction extends BaseAction<Customer> {

	// 登录方法
	@Action(value = "customer_login", results = {
			@Result(name = "success", type = "redirect", location = "index.html#/myhome"),
			@Result(name = "login", type = "redirect", location = "login.html") })
	public String login() {
		String baseAddress = Constants.CRM_MANAGEMENT_URL
				+ "/crm_management/services/customerService/customer/login?telephone=" + model.getTelephone()
				+ "&password=" + model.getPassword();

		System.out.println(baseAddress);
		Customer customer = WebClient.create(baseAddress).accept(MediaType.APPLICATION_JSON).get(Customer.class);
		if (customer == null) {
			return LOGIN;
		} else {
			// 登录成功将客户保存到session中
			ServletActionContext.getRequest().getSession().setAttribute("customer", customer);
			return SUCCESS;
		}
	}

	@Autowired
	@Qualifier("jmsQueueTemplate")
	private JmsTemplate jmsQueueTemplate;

	@Action(value = "customer_sendSms")
	public String sendSms() {
		// 生成短信验证码
		String randomCode = RandomStringUtils.randomNumeric(4);
		// 将短信验证码保存到session
		ServletActionContext.getRequest().getSession().setAttribute(model.getTelephone(), randomCode);
		System.out.println("手机生成的验证码为:" + randomCode);
		// 编辑短信内容
		final String msg = "【华男文化】" + randomCode + "为您的登录验证码，请于1分钟内填写。如非本人操作，请忽略本短信。";
		// 生产消息
		jmsQueueTemplate.send("bos_sms", new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage mapMessage = session.createMapMessage();
				mapMessage.setString("telephone", model.getTelephone());
				mapMessage.setString("msg", msg);
				return mapMessage;
			}
		});
		return NONE;
	}

	private String checkcode;
	private String checkPassword;

	public void setCheckcode(String checkcode) {
		this.checkcode = checkcode;
	}

	public String getCheckPassword() {
		return checkPassword;
	}

	public void setCheckPassword(String checkPassword) {
		this.checkPassword = checkPassword;
	}

	// 注入redis编辑邮件
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Action(value = "customer_regist", results = {
			@Result(name = "success", type = "redirect", location = "signup-success.html"),
			@Result(name = "input", type = "redirect", location = "signup.html") })
	public String regist() throws Exception {
		ServletActionContext.getResponse().setContentType("text/html;charset=UTF-8");
		String checkcode1 = (String) ServletActionContext.getRequest().getSession().getAttribute(model.getTelephone());
		if (checkcode1 == null || !checkcode1.equals(checkcode)) {
			ServletActionContext.getResponse().getWriter().println("验证码错误,请<a href='signup.html'>重新注册</a>");
			return NONE;
		}
		if (!checkPassword.equals(model.getPassword())) {
			ServletActionContext.getResponse().getWriter().println("两次密码输入不一致,请<a href='signup.html'>重新注册</a>");
			return NONE;
		}
		// 调用webService 连接CRM 保存客户信息
		WebClient.create("http://localhost:9002/crm_management/services/customerService/customer")
				.type(MediaType.APPLICATION_JSON).post(model);
		System.out.println("注册成功!");

		// 发送一封激活邮件
		// 生成激活码
		String activecode = RandomStringUtils.randomNumeric(32);

		redisTemplate.opsForValue().set(model.getTelephone(), activecode, 24, TimeUnit.HOURS);
		// 调用MailUtils发送激活邮件
		String content = "尊敬的客户您好，请于24小时内，进行邮箱账户的绑定，点击下面地址完成绑定:<br/><a href='" + MailUtils.activeUrl + "?telephone="
				+ model.getTelephone() + "&activecode=" + activecode + "'>速运快递邮箱绑定地址</a>";
		MailUtils.sendMail("速运快递激活邮件", content, model.getEmail(), activecode);
		System.out.println(content);
		return SUCCESS;
	}

	// 属性驱动,接收激活邮件传递的参数
	private String activecode;

	public void setActivecode(String activecode) {
		this.activecode = activecode;
	}

	// 邮件绑定
	@Action(value = "customer_activeMail") // customer_activeMail
	public String activeMail() throws Exception {
		ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");
		// 判断激活码是否有效
		String acticecodeRedis = redisTemplate.opsForValue().get(model.getTelephone());
		if (acticecodeRedis == null || !acticecodeRedis.equals(activecode)) {
			// 激活码无效
			ServletActionContext.getResponse().getWriter().println("激活码无效，请登录系统，重新绑定邮箱！");
		} else {
			// 激活码有效
			// 根据电话号码查询客户
			Customer customer = WebClient.create("http://localhost:9002/crm_management/services"
					+ "/customerService/customer/telephone/" + model.getTelephone()).accept(MediaType.APPLICATION_JSON)
					.get(Customer.class);
			// 判断客户的type
			if (customer.getType() == null || customer.getType() != 1) {
				WebClient.create("http://localhost:9002/crm_management/services/customerService/customer/updateType/"
						+ model.getTelephone()).get();
				ServletActionContext.getResponse().getWriter().println("邮箱绑定成功！");
			} else {
				// 已经绑定过
				ServletActionContext.getResponse().getWriter().println("邮箱已经绑定过，无需重复绑定！");
			}
			// 删除redis的激活码
			redisTemplate.delete(model.getTelephone());
		}
		return NONE;
	}

}
