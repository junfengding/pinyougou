package cn.itcast.crm.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import cn.itcast.crm.domain.Customer;

/**
 * 客户操作
 * 
 * @author Administrator
 *
 */
public interface CustomerService {
	// 查询所有未关联的客户
	@Path("/noassociationcustomers")
	@GET
	@Produces({ "application/xml", "application/json" })
	public List<Customer> findNoAssociationCustomers();

	// 已经关联到指定区域的客户
	@Path("/hasassociationcustomers/{fixedareaid}")
	@GET
	@Produces({ "application/xml", "application/json" })
	public List<Customer> findHasAssociationCustomers(@PathParam("fixedareaid") String fixedAreaId);

	@Path("/associationcustomerstofixedarea")
	@PUT
	// 将客户关联到定区上 ， 将所有客户id 拼成字符串 1,2,3
	public void associationCustomersToFixedArea(@QueryParam("customerIdStr") String customerIdStr,
			@QueryParam("fixedAreaId") String fixedAreaId);

}
