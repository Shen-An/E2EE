import store from '../store';

const { spawn } = require('child_process');

const pythonPath = 'D:\\Anaconda\\python.exe';
const pythonScriptPath = 'D:/java code/Chat/easychat-ui/easychat-front/src/main/SPCGSwTT_py/SPCEEnc.py';

const executeScript = (data) => {
    const { A, T, messageContent } = data;

    // 准备传递给 Python 脚本的参数
    const args = [
        pythonScriptPath,
        JSON.stringify(A),
        JSON.stringify(T),
        JSON.stringify(messageContent),
        JSON.stringify(store.getUserId())
    ];

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


export{
    executeScript
}
