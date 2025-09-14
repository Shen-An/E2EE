import CryptoJS from 'crypto-js';

class Closure {
    constructor(blockchain = null, startIndex = 0, endIndex = 0, blocks = null, renonceValue = 0, nonceValue = 0) {
        // 严格校验blockchain参数
        if (blockchain && (!blockchain.chain || !Array.isArray(blockchain.chain))) {
            throw new Error("Invalid blockchain instance, chain must be an array");
        }
        
        this.blockchain = blockchain;
        // 确保blocks始终为数组，并在blockchain存在时优先从blockchain获取区块
        this.blocks = Array.isArray(blocks) ? blocks : [];
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.renonceValue = renonceValue;
        this.nonceValue = nonceValue;

        if (blockchain) {
            // 安全获取区块范围
            const validStart = Math.max(startIndex, 0);
            const validEnd = Math.min(endIndex, blockchain.chain.length - 1);
            
            if (validStart <= validEnd) {
                // 清空现有blocks并重新填充（避免多次调用时重复添加）
                this.blocks = [];
                for (let i = validStart; i <= validEnd; i++) {
                    this.blocks.push(blockchain.chain[i]);
                }
            } else {
                console.warn("Invalid block range, blocks remain empty");
            }
        }
        
        // 额外校验blocks类型
        if (!Array.isArray(this.blocks)) {
            this.blocks = [];
            throw new Error("blocks must be an array");
        }
    }
    

    // 输出存储的区块数据
    printBlocks() {
        this.blocks.forEach(block => {
            console.log(JSON.stringify(block, null, 4));
        });
    }

    // 计算并返回所有存储区块的工作量证明
    nonce() {
        return this.blocks.map(block => block.proof);
    }

    // 计算初始区块的哈希值
    calculateInitialBlockHash() {
        if (this.blocks.length > 0) {
            const initialBlock = this.blocks[0];
            const encodedBlock = JSON.stringify(initialBlock, Object.keys(initialBlock).sort());
            return CryptoJS.SHA256(encodedBlock).toString();
        }
        return null;
    }

    // 查找匹配的哈希目标
    findMatchingHashTarget(targetPrefixLength = 2) {
        const initialBlockHash = this.calculateInitialBlockHash();
        if (!initialBlockHash) {
            return null;
        }

        const initialHashPrefix = initialBlockHash.substring(0, targetPrefixLength);

        while (true) {
            // 对整个链 + renonceValue 进行哈希计算
            const chainData = JSON.stringify(this.blockchain.chain, Object.keys(this.blockchain.chain).sort());
            const combinedData = chainData + this.renonceValue.toString();
            const currentHash = CryptoJS.SHA256(combinedData).toString();

            if (currentHash.substring(0, targetPrefixLength) === initialHashPrefix) {
                this.nonceValue = this.renonceValue;
                return [this.renonceValue, currentHash];
            }

            this.renonceValue++;
        }
    }

    // 在限制次数内查找匹配的哈希
    findMatchingHashWithLimit(maxIterations = 10000) {
        const targetPrefixLength = this.compareHashPrefixLength() + 1;
        const initialBlockHash = this.calculateInitialBlockHash();
        if (!initialBlockHash) {
            return false;
        }

        const initialHashPrefix = initialBlockHash.substring(0, targetPrefixLength);

        let iterations = 0;
        while (iterations < maxIterations) {
            // 对整个链 + renonceValue 进行哈希计算
            const chainData = JSON.stringify(this.blockchain.chain, Object.keys(this.blockchain.chain).sort());
            const combinedData = chainData + this.renonceValue.toString();
            const currentHash = CryptoJS.SHA256(combinedData).toString();
            console.log(currentHash);

            if (currentHash.substring(0, targetPrefixLength) === initialHashPrefix) {
                this.nonceValue = this.renonceValue;
                return true;
            }

            this.renonceValue++;
            iterations++;
        }

        return false;
    }

    // 比较哈希前缀长度
    compareHashPrefixLength() {
        const initialBlockHash = this.calculateInitialBlockHash();
        if (!initialBlockHash) {
            return 0;
        }

        // 对整个链 + nonceValue 进行哈希计算
        const chainData = JSON.stringify(this.blockchain.chain, Object.keys(this.blockchain.chain).sort());
        const combinedData = chainData + this.nonceValue.toString();
        const currentHash = CryptoJS.SHA256(combinedData).toString();
        console.log(currentHash);

        // 比较哈希前缀，返回相同的位数
        let matchingLength = 0;
        const minLength = Math.min(initialBlockHash.length, currentHash.length);
        for (let i = 0; i < minLength; i++) {
            if (initialBlockHash[i] === currentHash[i]) {
                matchingLength++;
            } else {
                break;
            }
        }

        return matchingLength;
    }

    // 保存到.clo文件
    async saveToCloFile(filename) {
        const data = {
            "blocks": this.blocks,
            "start_index": this.startIndex + 1,
            "end_index": this.endIndex + 1,
            "renonce_value": this.renonceValue,
            "nonce_value": this.nonceValue
        };

        try {
            const jsonString = JSON.stringify(data, null, 4);
            const blob = new Blob([jsonString], { type: 'application/json' });
            
            // 浏览器环境使用Blob和URL
            if (typeof window !== 'undefined') {
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = filename;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                URL.revokeObjectURL(url);
            } 
            // Node.js环境使用fs模块
            else {
                const fs = await import('fs').then(m => m.default);
                fs.writeFileSync(filename, jsonString);
            }
        } catch (error) {
            console.error('保存文件失败:', error);
            throw error;
        }
    }

    // 从.clo文件加载
    async loadFromCloFile(filename) {
        try {
            let data;
            
            // 浏览器环境使用fetch
            if (typeof window !== 'undefined') {
                const response = await fetch(filename);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                data = await response.json();
            } 
            // Node.js环境使用fs模块
            else {
                const fs = await import('fs').then(m => m.default);
                const fileData = fs.readFileSync(filename, 'utf8');
                data = JSON.parse(fileData);
            }

            this.blocks = data.blocks || [];
            this.startIndex = (data.start_index || 0) - 1;
            this.endIndex = (data.end_index || 0) - 1;
            this.renonceValue = data.renonce_value || 0;
            this.nonceValue = data.nonce_value || 0;
        } catch (error) {
            console.error('加载文件失败:', error);
            throw error;
        }
    }
}

export default Closure;    