package com.easyChat.SPCE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SPCEGEN {
    public static String executePythonScript(String pythonPath, String scriptPath) {
        StringBuilder output = new StringBuilder();
        Process process = null;

        try {
            // 构建命令（处理空格和路径中的反斜杠）
            String[] command = {
                    pythonPath,
                    "\"" + scriptPath.replace("\\", "\\\\") + "\""  // 转义路径中的反斜杠
            };

            process = Runtime.getRuntime().exec(command);

            // 读取标准输出（UTF-8 编码）
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");  // 保留换行符
                }
            }

            // 读取错误输出（可选）
            readErrorStream(process);

            // 等待进程完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Python脚本执行失败，退出码：" + exitCode);
            }

            return output.toString().trim();  // 去除首尾空白
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("执行Python脚本时出错：" + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static void readErrorStream(Process process) {
        new Thread(() -> {
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("Python错误输出：" + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
