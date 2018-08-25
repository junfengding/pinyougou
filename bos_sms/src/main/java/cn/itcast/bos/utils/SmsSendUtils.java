package cn.itcast.bos.utils;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;

public class SmsSendUtils {
	public static SmsSingleSenderResult sendMsg(String telephone,String msg) {
		int appid = 1400126304;
		String appkey = "07e1b43d139eef8881a78849e80d3654";
		SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
		SmsSingleSenderResult result = null;
		try {
			//作假
			msg="【华男文化】5467为您的登录验证码，请于1分钟内填写。如非本人操作，请忽略本短信。";
			telephone="18782449706";
			result = ssender.send(0, "86", telephone, msg, "", "");
			System.out.print(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		SmsSendUtils.sendMsg("18782449706","【华男文化】5467为您的登录验证码，请于1分钟内填写。如非本人操作，请忽略本短信。");
	}
}
