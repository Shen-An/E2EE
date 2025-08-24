package com.easyChat.AI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AIExec {
    private static String pythonPath = "D:\\Anaconda\\python.exe";
    static String PYTHON_SCRIPT_PATH = "D:\\java code\\Chat\\easychat-java\\src\\main\\java\\com\\easyChat\\AI\\AI.py";

    public static String execute(String msg) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(pythonPath, PYTHON_SCRIPT_PATH,msg);
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
            return  output.toString().split("</think>")[1].replace("\n","");
        }
        return "服务器繁忙，请稍后再试。";
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println(execute("你好"));
    }
}
