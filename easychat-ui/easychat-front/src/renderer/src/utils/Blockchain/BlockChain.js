import CryptoJS from 'crypto-js';

class Blockchain {
    constructor() {
        this.chain = [];
        this.createGenesisBlock();
    }

    // 创建创世区块
    createGenesisBlock() {
        const timestamp = new Date().toISOString();
        this.chain.push(this.createBlock(1, '0', 'genesis_user', 'genesis_block', 'genesis_sign', timestamp));
    }

    // 创建新区块
    createBlock(proof, previousHash, user, data, sign, timestamp) {
        const block = {
            index: this.chain.length + 1,
            timestamp,
            proof,
            user,
            data,
            sign,
            previousHash
        };
        return block;
    }

    // 检查区块是否存在
    blockExists(blockData) {
        const block = typeof blockData === 'string' ? JSON.parse(blockData) : blockData;
        return this.chain.some(existingBlock => this.isEqualBlock(existingBlock, block));
    }

    // 获取上一个区块
    getPreviousBlock() {
        return this.chain[this.chain.length - 1];
    }

    // 工作量证明算法
    proofOfWork(previousProof) {
        let newProof = 1;
        let checkProof = false;
        
        while (!checkProof) {
            const hashOperation = CryptoJS.SHA256((newProof ** 2 - previousProof ** 2).toString()).toString();
            if (hashOperation.substring(0, 4) === '0000') {
                checkProof = true;
            } else {
                newProof++;
            }
        }
        
        return newProof;
    }

    // 计算区块哈希
    calculateHash(block) {
        const blockString = JSON.stringify({
            ...block,
            timestamp: new Date(block.timestamp).toISOString()
        }, Object.keys(block).sort());
        return CryptoJS.SHA256(blockString).toString();
    }

    // 验证区块链有效性
    isValidChain() {
        for (let i = 1; i < this.chain.length; i++) {
            const currentBlock = this.chain[i];
            const previousBlock = this.chain[i - 1];
            
            // 验证哈希
            if (currentBlock.previousHash !== this.calculateHash(previousBlock)) {
                return false;
            }
            
            // 验证工作量证明
            if (!this.isValidProof(previousBlock.proof, currentBlock.proof)) {
                return false;
            }
        }
        
        return true;
    }

    // 验证工作量证明
    isValidProof(previousProof, currentProof) {
        const hashOperation = CryptoJS.SHA256((currentProof ** 2 - previousProof ** 2).toString()).toString();
        return hashOperation.substring(0, 4) === '0000';
    }

    // 挖矿（添加新区块）
    mineBlock(user, data, sign, timestamp = new Date().toISOString()) {
        const previousBlock = this.getPreviousBlock();
        const previousProof = previousBlock.proof;
        const proof = this.proofOfWork(previousProof);
        const previousHash = this.calculateHash(previousBlock);
        const block = this.createBlock(proof, previousHash, user, data, sign, timestamp);
        
        this.chain.push(block);
        return block;
    }

    // 获取完整区块链
    getChain() {
        return [...this.chain];
    }

    // 添加外部区块
    addBlock(blockData) {
        const block = typeof blockData === 'string' ? JSON.parse(blockData) : blockData;
        const previousBlock = this.getPreviousBlock();
        
        // 验证 previousHash
        if (block.previousHash !== this.calculateHash(previousBlock)) {
            return false;
        }
        
        // 验证工作量证明
        if (!this.isValidProof(previousBlock.proof, block.proof)) {
            return false;
        }
        
        this.chain.push(block);
        return true;
    }

    // 辅助方法：比较两个区块是否相同
    isEqualBlock(block1, block2) {
        return JSON.stringify({...block1, timestamp: new Date(block1.timestamp).toISOString()}) === 
               JSON.stringify({...block2, timestamp: new Date(block2.timestamp).toISOString()});
    }
}

export default Blockchain;