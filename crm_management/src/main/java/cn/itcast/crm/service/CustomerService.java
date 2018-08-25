package cn.itcast.crm.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

	@Path("/customer")
	@POST
	@Consumes({ "application/xml", "application/json" })
	//客户注册
	public void regist(Customer customer);

	@Path("/customer/telephone/{telephone}")
	@GET
	@Consumes({ "application/xml", "application/json" })
	//根据电话查询客户
	public Customer findByTelephone(@PathParam("telephone") String telephone);

	@Path("/customer/updateType/{telephone}")
	@GET
	@Consumes({ "application/xml", "application/json" })
	//更改客户类型
	public void updateType(@PathParam("telephone") String telephone);

	@Path("/customer/login")
	@GET
	@Consumes({ "application/xml", "application/json" })
	//客户登录
	public Customer login(@QueryParam("telephone") String telephone, @QueryParam("password") String password);
	@Path("/customer/findFixedAreaIdByAddress")
	@GET
	@Consumes({ "application/xml", "application/json" })
	public String findFixedAreaIdByAddress(@QueryParam("address") String address);
}
