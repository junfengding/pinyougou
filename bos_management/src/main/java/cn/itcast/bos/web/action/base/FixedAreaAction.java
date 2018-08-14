package cn.itcast.bos.web.action.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionContext;

import cn.itcast.bos.domain.base.FixedArea;
import cn.itcast.bos.service.base.FixedAreaService;
import cn.itcast.bos.web.action.common.BaseAction;
import cn.itcast.crm.domain.Customer;

@ParentPackage("json-default")
@Namespace("/")
@Controller
@Scope("prototype")
public class FixedAreaAction extends BaseAction<FixedArea> {
	// 注入service
	@Autowired
	private FixedAreaService fixedAreaService;

	/**
	 * 保存定区信息
	 * 
	 * @return
	 */
	@Action(value = "fixedArea_save", results = {
			@Result(name = "success", type = "redirect", location = "./pages/base/fixed_area.html") })
	public String save() {
		// 调用业务层保存定区
		fixedAreaService.save(model);
		return SUCCESS;
	}

	/**
	 * 分页查询
	 */
	@Action(value = "fixedArea_pageQuery", results = { @Result(name = "success", type = "json") })
	public String pageQuery() {
		// 构造分页查询对象
		Pageable pageable = new PageRequest(page - 1, rows);
		// 构造查询条件
		Specification<FixedArea> specification = new Specification<FixedArea>() {
			@Override
			public Predicate toPredicate(Root<FixedArea> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				if (StringUtils.isNotBlank(model.getId())) {
					Predicate p1 = cb.equal(root.get("id").as(String.class), model.getId());
					list.add(p1);
				}
				if (StringUtils.isNotBlank(model.getFixedAreaName())) {
					Predicate p2 = cb.equal(root.get("fixedAreaName").as(String.class),
							"%" + model.getFixedAreaName() + "%");
					list.add(p2);
				}
				return cb.and(list.toArray(new Predicate[0]));
			}
		};
		// 调用业务层,得到页面信息
		Page<FixedArea> pageData = fixedAreaService.findPageData(specification, pageable);
		this.pushPageDataToValueStack(pageData);

		return SUCCESS;
	}

	// 查询未关联定区客户
	// fixedArea_findNoAssociationCustomers
	@Action(value = "fixedArea_findNoAssociationCustomers", results = { @Result(name = "success", type = "json") })
	public String findNoAssociationCustomers() {
		// 使用webclient调用webservice接口
		Collection<? extends Customer> collection = WebClient
				.create("http://localhost:9002/crm_management/services/customerService/noassociationcustomers")
				.accept(MediaType.APPLICATION_JSON).getCollection(Customer.class);
		ActionContext.getContext().getValueStack().push(collection);
		return SUCCESS;
	}

	// 查询已经关联定区客户
	// fixedArea_findHasAssociationFixedAreaCustomers
	@Action(value = "fixedArea_findHasAssociationFixedAreaCustomers", results = {
			@Result(name = "success", type = "json") })
	public String findHasAssociationFixedAreaCustomers() {

		Collection<? extends Customer> collection = WebClient
				.create("http://localhost:9002/crm_management/services/customerService/hasassociationcustomers/"
						+ model.getId())
				.accept(MediaType.APPLICATION_JSON).getCollection(Customer.class);
		ActionContext.getContext().getValueStack().push(collection);
		return SUCCESS;
	}

	// 属性驱动注入页面传递的idtstr
	private String[] customerIds;

	public void setCustomerIds(String[] customerIds) {
		this.customerIds = customerIds;
	}

	// 关联客户到定区 fixedArea_associationCustomersToFixedArea
	@Action(value = "fixedArea_associationCustomersToFixedArea", results = {
			@Result(name = "success", type = "redirect", location = "./pages/base/fixed_area.html") })
	public String associationCustomersToFixedArea() {
		String customerIdStr = StringUtils.join(customerIds, ",");
		WebClient
				.create("http://localhost:9002/crm_management/services/customerService/associationcustomerstofixedarea?customerIdStr="
						+ customerIdStr + "&fixedAreaId=" + model.getId())
				.put(null);
		return SUCCESS;
	}

	// 属性驱动接收参数
	private Integer courierId;
	private Integer takeTimeId;

	public void setCourierId(Integer courierId) {
		this.courierId = courierId;
	}

	public void setTakeTimeId(Integer takeTimeId) {
		this.takeTimeId = takeTimeId;
	}

	// 定区关联快递员fixedArea_associationCourierToFixedArea
	@Action(value = "fixedArea_associationCourierToFixedArea", results = {
			@Result(name = "success", type = "redirect", location = "./pages/base/fixed_area.html") })
	public String associationCourierToFixedArea() {
		//调用业务层,定区关联快递员
		fixedAreaService.associationCourierToFixedArea(model,courierId,takeTimeId);
		return SUCCESS;
	}

}
