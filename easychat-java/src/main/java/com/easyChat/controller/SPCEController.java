package com.easyChat.controller;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.SpceIllegalTrace;
import com.easyChat.entity.query.SpceIllegalTraceQuery;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.enums.ResponseCodeEnum;
import com.easyChat.service.SpceIllegalTraceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/SPCE")
public class SPCEController extends ABaseController {
    @Resource
    private SpceIllegalTraceService spceIllegalTraceService;

    private static final String FILE_PATH = Constants.FILEPATH;
    private String pythonPath = "D:\\Anaconda\\python.exe";
    String PYTHON_SCRIPT_PATH = "D:\\java code\\Chat\\easychat-java\\src\\main\\java\\com\\easyChat\\SPCE\\";

    @GlobalInterceptor
    @RequestMapping("/getPk")
    public ResponseVo downloadPythonOutput() {
        try {
            // 读取文件内容
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return getSuccessResponseVo(ResponseEntity.ok()
                    .headers(headers)
                    .body(content));
        } catch (IOException e) {
            e.printStackTrace();
            return getBusinessErrorResponseVo(null, ResponseCodeEnum.CODE_602.getMsg());
        }
    }

    @GlobalInterceptor
    @RequestMapping("/sendCt")
    public ResponseVo receiveJsonData(@RequestBody Map<String, Object> data) {
        try {
            // 打印接收到的数据
            System.out.println("接收到的 JSON 数据: " + data);

            // 提取 tag 和 ct 的值
            String tag = null;
            String ct = null;
            String boolArr = null;
            if (data.containsKey("tag")) {
                tag = data.get("tag").toString();
                System.out.println("tag: " + tag);
            }
            if (data.containsKey("ct")) {
                ct = data.get("ct").toString();
                // 对 ct 进行处理，确保其为字符串格式
                ct = "\"" + ct.replace("\"", "\\\"") + "\"";
                System.out.println("提取到的 ct 值: " + ct);
            }
            if (data.containsKey("boolArr")) {
                boolArr = data.get("boolArr").toString();
                System.out.println("boolArr: " + boolArr);
            }
            String PYTHON_SCRIPT_PATH_CommitPy = PYTHON_SCRIPT_PATH + "SPCECommit.py";
            // 调用 Python 脚本并传递参数
            if (tag != null && ct != null) {
                ProcessBuilder pb = new ProcessBuilder(pythonPath, PYTHON_SCRIPT_PATH_CommitPy, tag, ct, boolArr);
                Process process = pb.start();

                // 获取脚本的输出
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                // 等待脚本执行完成
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Python 脚本执行成功，输出如下:");
                    System.out.println(output.toString());

                    List<String> resultList = parsePythonOutput1(output.toString());
                    return getSuccessResponseVo(resultList);

                } else {
                    System.err.println("Python 脚本执行失败，退出码: " + exitCode);
                }
            }

            return getSuccessResponseVo(null);
        } catch (Exception e) {
            e.printStackTrace();
            return getBusinessErrorResponseVo(null, "数据处理失败");
        }
    }

    //处理精度丢失，转化为字符串列表
    private static List<String> parsePythonOutput1(String output) {
        List<String> resultList = new ArrayList<>();
        // 去除方括号
        String trimmedOutput = output.trim().replace("[", "").replace("]", "");
        String[] values = trimmedOutput.split(",");
        for (String value : values) {
            resultList.add(value.trim());
        }
        return resultList;
    }

    @GlobalInterceptor
    @RequestMapping("/sendCommit")
    public ResponseVo receiveData(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        Integer illegalCount = spceIllegalTraceService.getSpceIllegalTraceByUserId(tokenUserInfoDto.getUserId()).getIllegalCount();
        Long currentTime = System.currentTimeMillis();
        //当前时间-违规时间>7天，则恢复次数
        SpceIllegalTrace spceIllegalTrace = spceIllegalTraceService.getSpceIllegalTraceByUserId(tokenUserInfoDto.getUserId());
        if(spceIllegalTrace.getIllegalTime() != null){
            if(currentTime-spceIllegalTrace.getIllegalTime() >Constants.MILLS_SECONDS_3DAYS_AGO/3*7){
                spceIllegalTrace.setIllegalCount(3);
                spceIllegalTraceService.updateSpceIllegalTraceByUserId(spceIllegalTrace, tokenUserInfoDto.getUserId());
            }
        }

        try {

            System.out.println("接收到的数据: " + data);

            // 提取 dataArr、tag 、 ct 、boolArr的值
            String dataArr = null;
            String tag = null;
            String ct = null;
            String boolArr = null;
            String userPk = null;
            if (data.containsKey("data")) {
                dataArr = data.get("data").toString();

                dataArr = dataArr.replace("\r\n", "");
                dataArr = dataArr.replace("[", "");
                dataArr = dataArr.replace("]", "");
                System.out.println("dataArr: " + dataArr);
                if (dataArr.length() == 0) {
                    //如果dataArr不存在，即vi不存在，那么没有非法的，退出即可
                    return getSuccessResponseVo(illegalCount);
                }
            }
            if (data.containsKey("tag")) {
                tag = data.get("tag").toString();
                System.out.println("tag: " + tag);
            }
            if (data.containsKey("ct")) {
                ct = data.get("ct").toString();
                // 对 ct 进行处理，确保其为字符串格式
                ct = "\"" + ct.replace("\"", "\\\"") + "\"";
                System.out.println("提取到的 ct 值: " + ct);
            }
            if (data.containsKey("boolArr")) {
                boolArr = data.get("boolArr").toString();
                System.out.println("boolArr: " + boolArr);
            }
            if (data.containsKey("userPk")) {
                userPk = data.get("userPk").toString();
            }
            String PYTHON_SCRIPT_PATH_DECPy = PYTHON_SCRIPT_PATH + "SPCEDEC.py";
            // 调用 Python 脚本并传递参数
            if (data != null) {
                ProcessBuilder pb = new ProcessBuilder(pythonPath, PYTHON_SCRIPT_PATH_DECPy, tag, ct, boolArr, dataArr, userPk);
                Process process = pb.start();

                // 获取脚本的输出
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                // 等待脚本执行完成
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Python 脚本执行成功，输出如下:");
                    System.out.println(output.toString());


                    Integer temp = illegalCount;

                    if (illegalCount > 0) {
                        if (output.toString().contains("追踪成功！用户公钥哈希：")) {
                            illegalCount = 0;
                        } else if (output.toString().contains("当前非法次数：2")) {
                            illegalCount = illegalCount - 2;
                        } else if (output.toString().contains("当前非法次数：1")) {
                            illegalCount = illegalCount - 1;
                        }
                    }
                    //一句话不包含三个非法字符，则恢复
                    if (illegalCount < 3 && illegalCount > 0) {
                        illegalCount = 3;
                    }
                    if (illegalCount < 0) {
                        illegalCount = 0;
                    }

                    //只有非法次数改变，或者用户违规才会更新
                    if (temp != illegalCount ||output.toString().contains("追踪成功！用户公钥哈希：")) {
                        spceIllegalTrace = new SpceIllegalTrace();
                        spceIllegalTrace.setUserId(tokenUserInfoDto.getUserId());
                        spceIllegalTrace.setIllegalCount(illegalCount);
                        spceIllegalTrace.setIllegalTime(currentTime);
                        spceIllegalTraceService.updateSpceIllegalTraceByUserId(spceIllegalTrace, tokenUserInfoDto.getUserId());
                    }


//                    System.out.println(result);
                    return getSuccessResponseVo(illegalCount);
                } else {
                    System.err.println("Python 脚本执行失败，退出码: " + exitCode);
                }
            }

            return getSuccessResponseVo(null);
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常情况
            return getServerErrorResponseVo(null);
        }
    }

//    @GlobalInterceptor
//    @RequestMapping("loadDataList")
//    public ResponseVo loadDataList(SpceIllegalTraceQuery query) {
//        return getSuccessResponseVo(spceIllegalTraceService.findListByPage(query));
//    }

    /**
     * 新增
     */
    @GlobalInterceptor
    @RequestMapping("addIllegalTrace")
    public ResponseVo add(HttpServletRequest request, SpceIllegalTrace bean) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        SpceIllegalTraceQuery spceIllegalTraceQuery = new SpceIllegalTraceQuery();
        spceIllegalTraceQuery.setUserId(tokenUserInfoDto.getUserId());
        if (spceIllegalTraceService.findCountByParam(spceIllegalTraceQuery) != 1) {
            this.spceIllegalTraceService.add(bean);
        }
        return getSuccessResponseVo(null);
    }

    private List<BigInteger> parsePythonOutput(String output) {
        List<BigInteger> resultList = new ArrayList<>();
        // 去除首尾的方括号
        String trimmedOutput = output.trim().replace("[", "").replace("]", "");
        // 按逗号分割字符串
        String[] parts = trimmedOutput.split(",");
        for (String part : parts) {
            try {
                // 去除可能存在的空格并转换为 BigInteger 类型
                BigInteger number = new BigInteger(part.trim());
                resultList.add(number);
            } catch (NumberFormatException e) {
                System.err.println("解析数字时出错: " + part);
            }
        }
        return resultList;
    }
//    /**
//     * 批量新增
//     */
//
//    @RequestMapping("addBatch")
//    public ResponseVo addBatch(@RequestBody List<SpceIllegalTrace> listBean) {
//        this.spceIllegalTraceService.addBatch(listBean);
//        return getSuccessResponseVo(null);
//    }
//
//    /**
//     * 批量新增或修改
//     */
//
//    @RequestMapping("addOrUpdateBatch")
//    public ResponseVo addOrUpdateBatch(@RequestBody List<SpceIllegalTrace> listBean) {
//        this.spceIllegalTraceService.addOrUpdateBatch(listBean);
//        return getSuccessResponseVo(null);
//    }
//
//    /**
//     * 根据UserId查询
//     */
//
//    @RequestMapping("getSpceIllegalTraceByUserId")
//    public ResponseVo getSpceIllegalTraceByUserId(String userId) {
//        return getSuccessResponseVo(this.spceIllegalTraceService.getSpceIllegalTraceByUserId(userId));
//    }
//
//    /**
//     * 根据UserId更新
//     */
//
//    @RequestMapping("updateSpceIllegalTraceByUserId")
//    public ResponseVo updateSpceIllegalTraceByUserId(SpceIllegalTrace bean, String userId) {
//        this.spceIllegalTraceService.updateSpceIllegalTraceByUserId(bean,userId);
//        return getSuccessResponseVo(null);
//    }
//
//    /**
//     * 根据UserId删除
//     */
//
//    @RequestMapping("deleteSpceIllegalTraceByUserId")
//    public ResponseVo deleteSpceIllegalTraceByUserId(String userId) {
//        this.spceIllegalTraceService.deleteSpceIllegalTraceByUserId(userId);
//        return getSuccessResponseVo(null);
//    }

}