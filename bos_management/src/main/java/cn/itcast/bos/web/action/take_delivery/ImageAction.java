package cn.itcast.bos.web.action.take_delivery;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;

import com.opensymphony.xwork2.ActionContext;

import cn.itcast.bos.web.action.common.BaseAction;

@ParentPackage("json-default")
@Namespace("/")
@Controller
@Scope("protoType")
public class ImageAction extends BaseAction<Object> {
	// 属性驱动,接收文件参数
	private File imgFile; // 文件
	private String imgFileFileName; // 文件名字
	private String imgFileContentType; // 文件类型

	public void setImgFile(File imgFile) {
		this.imgFile = imgFile;
	}

	public void setImgFileFileName(String imgFileFileName) {
		this.imgFileFileName = imgFileFileName;
	}

	public void setImgFileContentType(String imgFileContentType) {
		this.imgFileContentType = imgFileContentType;
	}

	/**
	 * 接收上传文件
	 */
	@Action(value = "image_upload", results = { @Result(name = "success", type = "json") })
	public String upload() {
		System.out.println("文件" + imgFile);
		System.out.println("文件名" + imgFileFileName);
		System.out.println("文件类型" + imgFileContentType);
		try {
			// 文件保存目录路径
			String savePath = ServletActionContext.getServletContext().getRealPath("/upload/");
			// 文件保存目录URL
			String saveUrl = ServletActionContext.getRequest().getContextPath() + "/upload/";
			// 定义允许上传的文件扩展名
			HashMap<String, String> extMap = new HashMap<String, String>();
			extMap.put("image", "gif,jpg,jpeg,png,bmp");
			// 重新生成文件上传的文件名
			UUID uuid = UUID.randomUUID();
			String ext = imgFileFileName.substring(imgFileFileName.lastIndexOf("."));
			String randomFileName = uuid + ext;
			// 保存图片
			FileCopyUtils.copy(imgFile, new File(savePath + "/" + randomFileName));
			// 封装返回参数
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("error", 0);
			map.put("url", saveUrl + randomFileName);
			// 封装参数压入栈顶
			ActionContext.getContext().getValueStack().push(map);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}

	/**
	 * 文件浏览
	 */
	@Action(value = "image_manage",results = { @Result(name = "success", type = "json") })
	public String manage() {
		//根目录路径，可以指定绝对路径，比如 /var/www/attached/
		String rootPath = ServletActionContext.getServletContext().getRealPath("/") + "upload/";
		//根目录URL，可以指定绝对路径，比如 http://www.yoursite.com/attached/
		String rootUrl  = ServletActionContext.getRequest().getContextPath() + "/upload/";
		//图片扩展名
		String[] fileTypes = new String[]{"gif", "jpg", "jpeg", "png", "bmp"};
		File currentPathFile=new File(rootPath);
		
		List<Map<String, Object>> fileList = new ArrayList<Map<String, Object>>();
		if(currentPathFile.listFiles() != null) {
			for (File file : currentPathFile.listFiles()) {
				HashMap<String, Object> hash = new HashMap<String, Object>();
				String fileName = file.getName();
				if(file.isDirectory()) {
					hash.put("is_dir", true);
					hash.put("has_file", (file.listFiles() != null));
					hash.put("filesize", 0L);
					hash.put("is_photo", false);
					hash.put("filetype", "");
				} else if(file.isFile()){
					String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
					hash.put("is_dir", false);
					hash.put("has_file", false);
					hash.put("filesize", file.length());
					hash.put("is_photo", Arrays.<String>asList(fileTypes).contains(fileExt));
					hash.put("filetype", fileExt);
				}
				hash.put("filename", fileName);
				hash.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.lastModified()));
				fileList.add(hash);
			}
		}
		HashMap<String,Object> result=new HashMap<String,Object>();
		result.put("moveup_dir_path", "");
		result.put("current_dir_path", rootPath);
		result.put("current_url", rootUrl);
		result.put("total_count", fileList.size());
		result.put("file_list", fileList);
		ActionContext.getContext().getValueStack().push(result);

		return SUCCESS;
	}
}
