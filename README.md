### 1.vue使用，不用node构建

###### import element css
```html
 <link rel="stylesheet" href="/css/index.css">
```
---
###### import vue js
```html
<script src="/js/vue-min-2.5.16.js"></script>
<script src="/js/jquery.min.js"></script>
<script src="/js/vue-router.js"></script>
<script src="/js/axios.min.js"></script>
```
---
###### import element js
```html
<script src="/js/element.js"></script>
<script src="/js/moment.min.js"></script>
```
---


### 1.vue使用-小坑
>  vue的生命周期，分为8个阶段  
> 1.beforeCreate （创建前）  
> 2.created （创建后）  
> 3.beforeMount （载入前）  
> 4.mounted （载入后）  
> 5.beforeUpdate （更新前）  
> 6.updated （更新后）  
> 7.beforeDestory（销毁前）  
> 8.destoryed（销毁后）  

```html
mounted: function () {
    var _this = this   //很重要！！!
    axios.get('/api')
        .then(function (res) {
            console.log(res);
            _this.tableData = res.data
        })
        .catch(function (error) {
            console.log(error);
        });
}

不要用ajax,以下无效，返回结果res不同
mounted:function () {
 var _this = this
 $.ajax({
   url: '/findall',
   type: 'get',
     dataType: 'json',
    success: function (res) {
        _this.tableData=res.data
        console.log(res.data)
    }
})
}

```


---
### axios发送get,post请求
```html
//get请求
axios.get('/poi/data')
.then(function (res) {
    _this.user = res.data
})
.catch(function (error) {
    console.log(error);
});


//post请求
axios.post("/poi/import", formData, config)
.then(function (res) {
    var html = '';
    html+="总共导入"+res.data.totalNum+"条，成功条数:"+res.data.successNum+"，失败条数:"+ res.data.failureNum;
    if (res.data.msg != ''){
        html+="<hr>失败原因:"+ res.data.msg;
    }
    layer.open({
        type: 1,
        title: false,
        closeBtn: 0,
        skin: 'layui-layer-nobg',
        shadeClose: true,
        content: html
    });
}).cache(function (error) {
    layer.msg(error.response.data);
});
                
       
//场景：发送post请求时，有loadding,以下代码可以对所有axios方法进行设置

var index = null;
//添加请求拦截器
axios.interceptors.request.use(function (config) {
    index = layer.load();
    return config;
}, function (error) {
    return Promise.reject(error);
});

//添加响应拦截器
axios.interceptors.response.use(function (res) {
    layer.close(index);
    return res;
},function (error) {
    return Promise.reject(error);
});         
```


---
### poi两个关键类Export, Import
可以快速运行main方法，看效果，然后再结合web

```java
package com.example.poi;

import com.example.vo.UserVo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出Excel文件（导出“XLSX”格式，支持大数据量导出）
 * @author lanxinghua
 * @version 2018-10-03
 */
public class ExportExcel {
	
	private static Logger log = LoggerFactory.getLogger(ExportExcel.class);
			
	/**
	 * 工作薄对象
	 */
	private SXSSFWorkbook wb;
	
	/**
	 * 工作表对象
	 */
	private Sheet sheet;
	
	/**
	 * 样式列表
	 */
	private Map<String, CellStyle> styles;
	
	/**
	 * 当前行号
	 */
	private int rownum;

	/**
	 * 构造函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param headerList 表头列表
	 */
	public ExportExcel(String title, List<String> headerList) {
		initialize(title, headerList);
	}


	/**
	 * 初始化函数
	 * @param title 表格标题，传“空值”，表示无标题
	 * @param headerList 表头列表
	 */
	private void initialize(String title, List<String> headerList) {
		this.wb = new SXSSFWorkbook(500);
		this.sheet = wb.createSheet("Export");
		this.styles = createStyles(wb);
		// Create title
		if (StringUtils.isNotBlank(title)){
			Row titleRow = sheet.createRow(rownum++);
			titleRow.setHeightInPoints(30);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellStyle(styles.get("title"));
			titleCell.setCellValue(title);
			sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(),
					titleRow.getRowNum(), titleRow.getRowNum(), headerList.size()-1));
		}
		// Create header
		if (headerList == null){
			throw new RuntimeException("headerList not null!");
		}
		Row headerRow = sheet.createRow(rownum++);
		headerRow.setHeightInPoints(16);
		for (int i = 0; i < headerList.size(); i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellStyle(styles.get("header"));
			String[] ss = StringUtils.split(headerList.get(i), "**", 2);
			if (ss.length==2){
				cell.setCellValue(ss[0]);
				Comment comment = this.sheet.createDrawingPatriarch().createCellComment(
						new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
				comment.setString(new XSSFRichTextString(ss[1]));
				cell.setCellComment(comment);
			}else{
				cell.setCellValue(headerList.get(i));
			}
		}
		for (int i = 0; i < headerList.size(); i++) {
			int colWidth = sheet.getColumnWidth(i)*2;
	        sheet.setColumnWidth(i, colWidth < 3000 ? 3000 : colWidth);
		}
		//sheet.setColumnWidth(3,10000);
		log.debug("Initialize success.");
	}
	
	/**
	 * 创建表格样式
	 * @param wb 工作薄对象
	 * @return 样式列表
	 */
	private Map<String, CellStyle> createStyles(Workbook wb) {
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
		
		CellStyle style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		Font titleFont = wb.createFont();
		titleFont.setFontName("Arial");
		titleFont.setFontHeightInPoints((short) 16);
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(titleFont);
		styles.put("title", style);

		style = wb.createCellStyle();
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		Font dataFont = wb.createFont();
		dataFont.setFontName("Arial");
		dataFont.setFontHeightInPoints((short) 10);
		style.setFont(dataFont);
		styles.put("data", style);
		
		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(CellStyle.ALIGN_LEFT);
		styles.put("data1", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(CellStyle.ALIGN_CENTER);
		styles.put("data2", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(CellStyle.ALIGN_RIGHT);
		styles.put("data3", style);
		
		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
//		style.setWrapText(true);
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font headerFont = wb.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		style.setFont(headerFont);
		styles.put("header", style);
		
		return styles;
	}

	/**
	 * 添加一行
	 * @return 行对象
	 */
	public Row addRow(){
		return sheet.createRow(rownum++);
	}
	

	/**
	 * 添加一个单元格
	 * @param row 添加的行
	 * @param column 添加列号
	 * @param val 添加值
	 * @return 单元格对象
	 */
	public Cell addCell(Row row, int column, Object val){
		return this.addCell(row, column, val, 0);
	}


	/**
	 * 添加一个单元格
	 * @param row 添加的行
	 * @param column 添加列号
	 * @param val 添加值
	 * @param align 对齐方式（1：靠左；2：居中；3：靠右）
	 * @return 单元格对象
	 */
	public Cell addCell(Row row, int column, Object val, int align){
		Cell cell = row.createCell(column);
		String cellFormatString = "@";
		try {
			if(val == null){
				cell.setCellValue("");
			}{
				if(val instanceof String) {
					cell.setCellValue((String) val);
				}else if(val instanceof Integer) {
					cell.setCellValue((Integer) val);
					cellFormatString = "0";
				}else if(val instanceof Long) {
					cell.setCellValue((Long) val);
					cellFormatString = "0";
				}else if(val instanceof Double) {
					cell.setCellValue((Double) val);
					cellFormatString = "0.00";
				}else if(val instanceof Float) {
					cell.setCellValue((Float) val);
					cellFormatString = "0.00";
				}else if(val instanceof Date) {
					cell.setCellValue((Date) val);
					cellFormatString = "yyyy-MM-dd HH:mm";
				}
			}
			if (val != null){
				CellStyle style = styles.get("data_column_"+column);
				if (style == null){
					style = wb.createCellStyle();
					style.cloneStyleFrom(styles.get("data"+(align>=1&&align<=3?align:2)));
			        style.setDataFormat(wb.createDataFormat().getFormat(cellFormatString));
					styles.put("data_column_" + column, style);
				}
				cell.setCellStyle(style);
			}
		} catch (Exception ex) {
			log.info("Set cell value ["+row.getRowNum()+","+column+"] error: " + ex.toString());
			cell.setCellValue(val.toString());
		}
		return cell;
	}

	/**
     * 添加数据
	 *
	 * @param dataList
     * @return
     */
	public ExportExcel setDataList(List<UserVo> dataList){
		for (int i = 0; i < dataList.size(); i++) {
			Row row = this.addRow();
			UserVo user = dataList.get(i);
			this.addCell(row, 0, user.getId());
			this.addCell(row, 1, user.getUserName());
			this.addCell(row, 2, user.getAge());
		}
		return this;
	}

	
	/**
	 * 输出数据流
	 * @param os 输出数据流
	 */
	public ExportExcel write(OutputStream os) throws IOException{
		wb.write(os);
		return this;
	}
	
	/**
	 * 输出到客户端
	 * @param fileName 输出文件名
	 */
	public ExportExcel write(HttpServletResponse response, String fileName) throws IOException{
		response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename="+fileName);
		write(response.getOutputStream());
		return this;
	}
	
	/**
	 * 输出到文件
	 */
	public ExportExcel writeFile(String name) throws Exception{
		FileOutputStream os = new FileOutputStream(name);
		this.write(os);
		return this;
	}
	
	/**
	 * 清理临时文件
	 */
	public ExportExcel dispose(){
		wb.dispose();
		return this;
	}
	
	/**
	 * 导出测试
	 */
	public static void main(String[] args) throws Exception {
		List<String> headerList = Lists.newArrayList();
		headerList.add("编号");
		headerList.add("姓名");
		headerList.add("年龄");
		List<UserVo> dataList = Lists.newArrayList();
		for (int i = 1; i <= headerList.size(); i++) {
			UserVo userVo = new UserVo();
			userVo.setId(String.valueOf(i));
			userVo.setAge("年龄"+i);
			userVo.setUserName("用户"+i);
			dataList.add(userVo);

		}
		ExportExcel ee = new ExportExcel("表格标题", headerList).setDataList(dataList);
		ee.writeFile("target/export.xlsx");
		ee.dispose();
		log.debug("Export success.");
	}
}

```


```java
package com.example.poi;

import com.alibaba.fastjson.JSON;
import com.example.vo.UserVo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 导入Excel文件（支持“XLS”和“XLSX”格式）
 * @author lanxinghua
 * @version 2018-10-03
 */

public class ImportExcel {
	
	private static Logger log = LoggerFactory.getLogger(ImportExcel.class);
			
	/**
	 * 工作薄对象
	 */
	private Workbook wb;
	
	/**
	 * 工作表对象
	 */
	private Sheet sheet;
	
	/**
	 * 标题行号
	 */
	private int headerNum;
	
	/**
	 * 构造函数
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @throws InvalidFormatException
	 * @throws IOException 
	 */
	public ImportExcel(String fileName, int headerNum) throws Exception {
		this(new File(fileName), headerNum);
	}
	
	/**
	 * 构造函数
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @throws InvalidFormatException
	 * @throws IOException 
	 */
	public ImportExcel(File file, int headerNum) throws Exception {
		this(file, headerNum, 0);
	}

	/**
	 * 构造函数
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @param sheetIndex 工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException 
	 */
	public ImportExcel(String fileName, int headerNum, int sheetIndex) throws Exception {
		this(new File(fileName), headerNum, sheetIndex);
	}
	
	/**
	 * 构造函数
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @param sheetIndex 工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException 
	 */
	public ImportExcel(File file, int headerNum, int sheetIndex) throws Exception {
		this(file.getName(), new FileInputStream(file), headerNum, sheetIndex);
	}
	
	/**
	 * 构造函数
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @param sheetIndex 工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException 
	 */
	public ImportExcel(MultipartFile multipartFile, int headerNum, int sheetIndex) throws Exception {
		this(multipartFile.getOriginalFilename(), multipartFile.getInputStream(), headerNum, sheetIndex);
	}

	/**
	 * 构造函数
	 * @param headerNum 标题行号，数据行号=标题行号+1
	 * @param sheetIndex 工作表编号
	 * @throws InvalidFormatException
	 * @throws IOException 
	 */
	public ImportExcel(String fileName, InputStream is, int headerNum, int sheetIndex) throws Exception {
		if (StringUtils.isBlank(fileName)){
			throw new Exception("导入文档为空!");
		}else if(fileName.toLowerCase().endsWith("xls")){    
			this.wb = new HSSFWorkbook(is);
        }else if(fileName.toLowerCase().endsWith("xlsx")){  
        	this.wb = new XSSFWorkbook(is);
        }else{  
        	throw new Exception("文档格式不正确!");
        }  
		if (this.wb.getNumberOfSheets()<sheetIndex){
			throw new Exception("文档中没有工作表!");
		}
		this.sheet = this.wb.getSheetAt(sheetIndex);
		this.headerNum = headerNum;
		log.debug("Initialize success.");
	}
	
	/**
	 * 获取行对象
	 * @param rownum
	 * @return
	 */
	public Row getRow(int rownum){
		return this.sheet.getRow(rownum);
	}

	/**
	 * 获取数据行号
	 * @return
	 */
	public int getDataRowNum(){
		return headerNum+1;
	}
	
	/**
	 * 获取最后一个数据行号
	 * @return
	 */
	public int getLastDataRowNum(){
		return this.sheet.getLastRowNum()+headerNum;
	}
	
	/**
	 * 获取最后一个列号
	 * @return
	 */
	public int getLastCellNum(){
		return this.getRow(headerNum).getLastCellNum();
	}
	
	/**
	 * 获取单元格值
	 * @param row 获取的行
	 * @param column 获取单元格列号
	 * @return 单元格值
	 */
	public Object getCellValue(Row row, int column){
		Object val = "";
		try{
			Cell cell = row.getCell(column);
			if (cell != null){
				if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
					val = cell.getNumericCellValue();
				}else if (cell.getCellType() == Cell.CELL_TYPE_STRING){
					val = cell.getStringCellValue();
				}else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA){
					val = cell.getCellFormula();
				}else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN){
					val = cell.getBooleanCellValue();
				}else if (cell.getCellType() == Cell.CELL_TYPE_ERROR){
					val = cell.getErrorCellValue();
				}
			}
		}catch (Exception e) {
			return val;
		}
		return val;
	}
	
	/**
	 * 获取导入数据列表
	 */
	public List<UserVo> getDataList(){
		List<UserVo> dataList = Lists.newArrayList();
		for (int i = this.getDataRowNum(); i < this.getLastDataRowNum(); i++) {
			Row row = this.getRow(i);
			UserVo user = new UserVo();
			user.setId(String.valueOf(this.getCellValue(row, 0)));
			user.setUserName(String.valueOf(this.getCellValue(row, 1)));
			user.setAge(String.valueOf(this.getCellValue(row, 2)));
			dataList.add(user);
		}
		return dataList;
	}

	/**
	 * 导入测试
	 */
	public static void main(String[] args) throws Throwable {
		String filePath = "target/export.xlsx";
		FileInputStream in = new FileInputStream(filePath);
		XSSFWorkbook wb = new XSSFWorkbook(in);
		System.out.println("sheet个数："+wb.getNumberOfSheets());
		System.out.println("sheet名字："+wb.getSheetName(0));
		ImportExcel importExcel = new ImportExcel(filePath, 1);
		List<UserVo> dataList = importExcel.getDataList();
		dataList.forEach(user -> {
			System.out.println(JSON.toJSONString(user));
		});
	}
}

```


