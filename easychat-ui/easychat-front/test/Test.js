const { exec } = require('child_process');

// 替换为你的Python解释器的完整路径
const pythonPath = 'D:\\Anaconda\\python.exe';
const readDataFromFile = () => {
    const userDataPath = path.join(__dirname, store.getUserId()+'SPCE.json');
    try {
        const fileContent = fs.readFileSync(userDataPath, 'utf8');
        return JSON.parse(fileContent);
    } catch (error) {
        console.error('从文件读取数据时出错:', error);
        return null;
    }
};
readDataFromFile