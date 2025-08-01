const { spawn } = require('child_process');

const pythonPath = 'D:\\Anaconda\\python.exe';
const pythonScriptPath = "D:/java code/Chat/easychat-ui/easychat-front/test/2.py";

const executeScript = (data) => {
    const { A, T, messageContent } = data;

    // 准备传递给 Python 脚本的参数
    const args = [
        pythonScriptPath,
        JSON.stringify(A),
        JSON.stringify(T),
        JSON.stringify(messageContent),
        JSON.stringify("U84319281252")
    ];

    return new Promise((resolve, reject) => {
        const pythonProcess = spawn(pythonPath, args);

        let stdoutData = '';
        let stderrData = '';

        pythonProcess.stdout.on('data', (data) => {
            stdoutData += data.toString();
        });

        pythonProcess.stderr.on('data', (data) => {
            stdoutData += data.toString();
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

const dataFromVue = {
    "A": {
        "x": "14515985575884475909231271891664171646570883468344520271556318498526076891378",
        "y": "12503469309074248954559846764035963791230742539768463996200210225584010180460"
    },
    "T": {
        "(9350148218719773837433878108876435170265905176595042576792048479637261743307, 14097617999796013250106978805333303898889151953362208294148435565816204990621)": {
            "x": "18015517622100979347447590964580384066818005695854461044913810563626108339323",
            "y": "10518408297607731221607379374996298683620050290108210786065728493794063053372"
        }
    },
    "messageContent": "你;llo",
 
};

(async () => {
    try {
        const result = await executeScript(dataFromVue);
        console.log(`脚本输出: ${result}`);
    } catch (error) {
        console.error(error);
    }
})();