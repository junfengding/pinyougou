package cn.itcast.bos.service.take_delivery.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.itcast.bos.constant.Constants;
import cn.itcast.bos.dao.base.AreaRepository;
import cn.itcast.bos.dao.base.FixedAreaRepository;
import cn.itcast.bos.dao.take_delivery.OrderRepository;
import cn.itcast.bos.dao.take_delivery.WorkBillRepository;
import cn.itcast.bos.domain.base.Area;
import cn.itcast.bos.domain.base.Courier;
import cn.itcast.bos.domain.base.FixedArea;
import cn.itcast.bos.domain.base.SubArea;
import cn.itcast.bos.domain.take_delivery.Order;
import cn.itcast.bos.domain.take_delivery.WorkBill;
import cn.itcast.bos.service.take_delivery.OrderService;
import cn.itcast.crm.domain.Customer;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private FixedAreaRepository fixedAreaRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private AreaRepository areaRepository;
	@Autowired
	private WorkBillRepository workBillRepository;
	
	@Autowired
	@Qualifier("jmsQueueTemplate")
	private JmsTemplate jmsTemplate;

	// 保存订单操作
	@Override
	public void saveOrder(Order order) {
		// 设置不是从页面上传的属性
		order.setOrderNum(UUID.randomUUID().toString()); // 设置订单号
		order.setOrderTime(new Date());// 设置下单时间
		order.setStatus("1"); // 待取件

		Area area = order.getSendArea();
		// 查询分区
		Area persistArea = areaRepository.findByProvinceAndCityAndDistrict(area.getProvince(), area.getCity(),
				area.getDistrict());
		Area recArea = order.getRecArea();
		Area persistAreaRec = areaRepository.findByProvinceAndCityAndDistrict(area.getProvince(), area.getCity(),
				area.getDistrict());
		// 设置分区
		order.setRecArea(persistAreaRec);
		order.setSendArea(persistArea);

		// 自动分单逻辑，基于CRM地址库完全匹配，获取定区，匹配快递员
		String fixedAreaId = WebClient
				.create(Constants.CRM_MANAGEMENT_URL
						+ "/crm_management/services/customerService/customer/findFixedAreaIdByAddress?address="
						+ order.getSendAddress())
				.accept(MediaType.APPLICATION_JSON).get(String.class);
		
		if (null != fixedAreaId) {
			// 1.下单地址和客户地址一致情况
			FixedArea fixedArea = fixedAreaRepository.findOne(fixedAreaId); // 根据id查询定区
			// 根据定区查询快递员
			Iterator<Courier> iterator = fixedArea.getCouriers().iterator();
			if (iterator.hasNext()) {
				// 自动分单成功
				System.out.println("自动分单成功....");
				Courier courier = iterator.next();
				saveOrder(order, courier);
				// 生成工单发送短信
				generateWorkBill(order);
				return;
			}
		}

		// 2.根据分区关键字分配快递员

		// 自动分单 逻辑， 通过省市区 ，查询分区关键字，匹配地址，基于分区实现自动分单
		for (SubArea subArea : persistArea.getSubareas()) {
			// 当前客户的下单地址 是否包含分区 关键字
			boolean b1 = order.getSendAddress().contains(subArea.getKeyWords());
			// 当前客户的下单地址 是否包含分区 辅助关键字
			boolean b2 = order.getSendAddress().contains(subArea.getAssistKeyWords());
			if (b1 || b2) {
				// 找打分区 ,找到定区、找到快递员
				Iterator<Courier> iterator = subArea.getFixedArea().getCouriers().iterator();
				if (iterator.hasNext()) {
					Courier courier = iterator.next();
					if (courier != null) {
						// 自动分单成功
						System.out.println("自动分单成功....");
						saveOrder(order, courier);
						// 生成工单 发送短信
						generateWorkBill(order);
						return;
					}
				}
			}
		}
		// 3.进入人工分单
		order.setOrderType("2");
		orderRepository.save(order);

	}

	private void generateWorkBill(Order order) {
		// 生成工单
		WorkBill workBill = new WorkBill();
		workBill.setType("新");
		workBill.setPickstate("新单");
		workBill.setBuildtime(new Date());
		workBill.setRemark(order.getRemark());
		final String smsNumber = RandomStringUtils.randomNumeric(4);
		workBill.setSmsNumber(smsNumber); // 短信序号
		workBill.setOrder(order);
		workBill.setCourier(order.getCourier());
		workBillRepository.save(workBill);
		// 发送短信
		// 调用MQ服务，发送一条消息
		jmsTemplate.send("bos_sms", new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage mapMessage = session.createMapMessage();
				mapMessage.setString("telephone", order.getCourier().getTelephone());
				mapMessage.setString("msg", "短信序号：" + smsNumber + ",取件地址：" + order.getSendAddress() + ",联系人:"
						+ order.getSendName() + ",手机:" + order.getSendMobile() + ",快递员捎话：" + order.getSendMobileMsg()+";如非本人操作，请忽略本短信。");
				return mapMessage;
			}
		});

		// 修改工单状态
		workBill.setPickstate("已通知");

	}

	// 自动分单保存
	private void saveOrder(Order order, Courier courier) {
		// 将快递员关联订单上
		order.setCourier(courier);
		// 设置自动分单
		order.setOrderType("1");
		// 保存订单
		orderRepository.save(order);
	}
}
