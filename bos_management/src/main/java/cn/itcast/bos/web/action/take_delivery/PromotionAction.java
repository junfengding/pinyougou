package cn.itcast.bos.web.action.take_delivery;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

import cn.itcast.bos.domain.take_delivery.Promotion;
import cn.itcast.bos.service.take_delivery.PromotionService;
import cn.itcast.bos.web.action.common.BaseAction;

@ParentPackage("json-default")
@Namespace("/")
@Controller
@Scope("prototype")
public class PromotionAction extends BaseAction<Promotion> {
	private File titleImgFile;
	private String titleImgFileFileName;

	public void setTitleImgFile(File titleImgFile) {
		this.titleImgFile = titleImgFile;
	}

	public void setTitleImgFileFileName(String titleImgFileFileName) {
		this.titleImgFileFileName = titleImgFileFileName;
	}

	// 注入service
	@Autowired
	private PromotionService promotionService;

	/**
	 * 保存数据库操作
	 */
	@Action(value = "promotion_save", results = {
			@Result(name = "success", type = "redirect", location = "./pages/take_delivery/promotion.html") })
	public String save() throws IOException {
		// 文件保存目录路径
		String savePath = ServletActionContext.getServletContext().getRealPath("/upload/");
		// 文件保存目录URL
		String saveUrl = ServletActionContext.getRequest().getContextPath() + "/upload/";
		// 定义允许上传的文件扩展名
		HashMap<String, String> extMap = new HashMap<String, String>();
		extMap.put("image", "gif,jpg,jpeg,png,bmp");
		// 重新生成文件上传的文件名
		UUID uuid = UUID.randomUUID();
		String ext = titleImgFileFileName.substring(titleImgFileFileName.lastIndexOf("."));
		String randomFileName = uuid + ext;
		// 保存图片 (绝对路径)
		File destFile = new File(savePath + "/" + randomFileName);
		System.out.println(destFile.getAbsolutePath());
		FileUtils.copyFile(titleImgFile, destFile);
		// 将保存路径 相对工程web访问路径，保存model中
		model.setTitleImg(ServletActionContext.getRequest().getContextPath() + "/upload/" + randomFileName);

		// 调用业务层，完成活动任务数据保存
		promotionService.save(model);
		return SUCCESS;
	}

	/**
	 * 分页查询显示
	 * 
	 * @return
	 */
	@Action(value = "promotion_pageQuery", results = { @Result(name = "success", type = "json") })
	public String pageQuery() {
		// 构造分页查询参数
		Pageable pageable = new PageRequest(page - 1, rows);
		Page<Promotion> pageData = promotionService.findPageData(pageable);
		// 压入值栈
		pushPageDataToValueStack(pageData);
		return SUCCESS;
	}

}
