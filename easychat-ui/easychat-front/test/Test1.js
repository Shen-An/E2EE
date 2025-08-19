import store from '../store';
const { spawn } = require('child_process');

const executePythonScript = (pythonScriptPath, scriptArgs) => {
    const pythonPath = 'D:\\Anaconda\\python.exe';
    const args = [pythonScriptPath,...scriptArgs.map(arg => JSON.stringify(arg))];

    return new Promise((resolve, reject) => {
        const pythonProcess = spawn(pythonPath, args);

        let stdoutData = '';
        let stderrData = '';

        pythonProcess.stdout.on('data', (data) => {
            stdoutData += data.toString();
        });

        pythonProcess.stderr.on('data', (data) => {
            stderrData += data.toString();
        });

        pythonProcess.on('close', (code) => {
            if (code!== 0) {
                reject(`执行错误: 进程以退出码 ${code} 结束。\n${stderrData}`);
                return;
            }
            if (stderrData) {
                reject(`脚本错误: ${stderrData}`);
                return;
            }
            resolve(stdoutData);
        });
    });
};

// 示例调用 1，对应之前第一个 executeScript 的逻辑
const executeScript1 = (data) => {
    const { A, T, words, boolArr } = data;
    const scriptArgs = [A, T, words, store.getUserId(), boolArr];
    const pythonScriptPath = 'D:/java code/Chat/easychat-ui/easychat-front/src/main/SPCGSwTT_py/SPCEEnc.py';
    return executePythonScript(pythonScriptPath, scriptArgs);
};

// 示例调用 2，对应之前第二个 executeScript 的逻辑
const executeScript2 = (data) => {
    const scriptArgs = [data];
    const pythonScriptPath = 'D:/java code/Chat/easychat-ui/easychat-front/com.easyChat.test/2.py';
    return executePythonScript(pythonScriptPath, scriptArgs);
};

// 测试示例 2
const dataFromVue = [4.8202020398290094e+75, 6.6314599382010465e+75];
(async () => {
    try {
        const result = await executeScript2(dataFromVue);
        console.log(`脚本输出: ${result}`);
    } catch (error) {
        console.error(error);
    }
})();

export {
    executeScript1,
    executeScript2
};
