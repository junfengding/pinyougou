package cn.itcast.bos.mq;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.springframework.stereotype.Service;

import com.github.qcloudsms.SmsSingleSenderResult;

import cn.itcast.bos.utils.SmsSendUtils;

@Service("smsConsumer")
public class SmsConsumer implements MessageListener {

	@Override
	public void onMessage(Message message) {
		MapMessage mapMessage = (MapMessage) message;
		try {
			// String result = "000/xxxx";
			SmsSingleSenderResult result = SmsSendUtils.sendMsg(mapMessage.getString("telephone"),mapMessage.getString("msg"));
			if (result.errMsg.equals("OK") && result.result == 0) {
				// 发送成功
				System.out.println(
						"发送短信成功,手机号：" + mapMessage.getString("telephone") + "，验证码：" + mapMessage.getString("msg"));
			} else {
				// 发送失败
				throw new RuntimeException("短信发送失败, 信息码：" + result);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}