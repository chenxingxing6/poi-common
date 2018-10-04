package com.example.controller;

import com.alibaba.fastjson.JSON;
import com.example.poi.ExportExcel;
import com.example.poi.ImportExcel;
import com.example.vo.UserVo;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User: lanxinghua
 * Date: 2018/10/3 17:45
 * Desc:
 */
@Controller
@RequestMapping("/poi")
public class PoiController {
    private static final Logger logger = LoggerFactory.getLogger(PoiController.class);

    /**
     * 跳转到页面
     *
     * @return
     */
    @RequestMapping("/view")
    public String view() {
        return "/poi";
    }

    /**
     * 导出数据
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportCard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String fileName = URLEncoder.encode("商家画像.xlsx", "utf-8");
            List<String> headerList = Lists.newArrayList();
            headerList.add("编号");
            headerList.add("姓名");
            headerList.add("年龄");
            List<UserVo> data = getData(headerList);
            new ExportExcel("表格标题", headerList).setDataList(data).write(response, fileName).dispose();
            return;
        } catch (Exception e) {
            logger.error("导出失败" + e.getMessage());
        }
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public String importFile(MultipartFile file) throws Exception {
        Map<String,Object> map = new HashMap<>();
        int successNum = 0;
        int failureNum = 0;
        int totalNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        ImportExcel importExcel = new ImportExcel(file, 1, 0);
        List<UserVo> dataList = importExcel.getDataList();
        totalNum = dataList.size();
        for (UserVo userVo : dataList) {
            logger.info("[数据：]"+JSON.toJSONString(userVo));
            //对数据进行校验
            if (!"用户1".equals(userVo.getUserName())) {
                //保存到数据库
                //对user进行校验，以后这部分我们可以用BeanValidarors进行校验，然后将异常的捕获，返回给前台
                successNum++;
            }else {
                failureNum++;
                failureMsg.append("<br/>第"+failureNum+"条，用户："+userVo.getUserName()+"已经存在;");

            }
        }
        map.put("successNum", successNum);
        map.put("failureNum", failureNum);
        map.put("totalNum", totalNum);
        map.put("msg", failureMsg);
        return JSON.toJSONString(map);
    }

    /**
     * 获取模拟数据
     *
     * @return
     */
    @RequestMapping(value = "/data")
    @ResponseBody
    public String getDatas() {
        List<String> headerList = Lists.newArrayList();
        headerList.add("编号");
        headerList.add("姓名");
        headerList.add("年龄");
        List<UserVo> data = getData(headerList);
        return JSON.toJSONString(data);
    }

    /**
     * 假造数据
     *
     * @param headerList
     * @return
     */
    private List<UserVo> getData(List<String> headerList) {
        List<UserVo> dataList = Lists.newArrayList();
        for (int i = 1; i <= headerList.size(); i++) {
            UserVo userVo = new UserVo();
            userVo.setId(String.valueOf(i));
            userVo.setAge("年龄" + i);
            userVo.setUserName("用户" + i);
            dataList.add(userVo);

        }
        return dataList;
    }
}