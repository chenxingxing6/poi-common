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
