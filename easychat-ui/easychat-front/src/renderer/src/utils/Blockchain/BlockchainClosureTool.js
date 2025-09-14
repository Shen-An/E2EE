import { ethers } from "ethers";
import Blockchain from './BlockChain.js';
import Closure from './Closure.js';

// 区块链闭环工具配置
const closureConfig = {
    rpcUrl: "https://rpc-testnet.whitechain.io",
    chainId: 2625,
    contractAddress: "0xAb460ac4b917dABb8b1FE1410449e761B6B5a160",
    contractAbi: [
        {
            "anonymous": false,
            "inputs": [
                {
                    "indexed": true,
                    "internalType": "uint256",
                    "name": "id",
                    "type": "uint256"
                },
                {
                    "indexed": false,
                    "internalType": "string[]",
                    "name": "blockHashes",
                    "type": "string[]"
                },
                {
                    "indexed": false,
                    "internalType": "string",
                    "name": "closureHash",
                    "type": "string"
                },
                {
                    "indexed": false,
                    "internalType": "uint256",
                    "name": "nonceValue",
                    "type": "uint256"
                },
                {
                    "indexed": false,
                    "internalType": "string",
                    "name": "timestamp",
                    "type": "string"
                },
                {
                    "indexed": true,
                    "internalType": "address",
                    "name": "owner",
                    "type": "address"
                },
                {
                    "indexed": false,
                    "internalType": "string[]",
                    "name": "messages",
                    "type": "string[]"
                }
            ],
            "name": "ClosureStored",
            "type": "event"
        },
        {
            "inputs": [
                {
                    "internalType": "string[]",
                    "name": "blockHashes",
                    "type": "string[]"
                },
                {
                    "internalType": "string",
                    "name": "closureHash",
                    "type": "string"
                },
                {
                    "internalType": "uint256",
                    "name": "nonceValue",
                    "type": "uint256"
                },
                {
                    "internalType": "string",
                    "name": "timestamp",
                    "type": "string"
                },
                {
                    "internalType": "string[]",
                    "name": "messages",
                    "type": "string[]"
                }
            ],
            "name": "storeClosure",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "closureCount",
            "outputs": [
                {
                    "internalType": "uint256",
                    "name": "",
                    "type": "uint256"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "uint256",
                    "name": "",
                    "type": "uint256"
                }
            ],
            "name": "closures",
            "outputs": [
                {
                    "internalType": "string",
                    "name": "closureHash",
                    "type": "string"
                },
                {
                    "internalType": "uint256",
                    "name": "nonceValue",
                    "type": "uint256"
                },
                {
                    "internalType": "string",
                    "name": "timestamp",
                    "type": "string"
                },
                {
                    "internalType": "address",
                    "name": "owner",
                    "type": "address"
                },
                {
                    "internalType": "uint256",
                    "name": "createdAt",
                    "type": "uint256"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "uint256",
                    "name": "index",
                    "type": "uint256"
                }
            ],
            "name": "getClosure",
            "outputs": [
                {
                    "internalType": "string[]",
                    "name": "blockHashes",
                    "type": "string[]"
                },
                {
                    "internalType": "string",
                    "name": "closureHash",
                    "type": "string"
                },
                {
                    "internalType": "uint256",
                    "name": "nonceValue",
                    "type": "uint256"
                },
                {
                    "internalType": "string",
                    "name": "timestamp",
                    "type": "string"
                },
                {
                    "internalType": "address",
                    "name": "owner",
                    "type": "address"
                },
                {
                    "internalType": "string[]",
                    "name": "messages",
                    "type": "string[]"
                },
                {
                    "internalType": "uint256",
                    "name": "createdAt",
                    "type": "uint256"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "address",
                    "name": "",
                    "type": "address"
                },
                {
                    "internalType": "uint256",
                    "name": "",
                    "type": "uint256"
                }
            ],
            "name": "userClosures",
            "outputs": [
                {
                    "internalType": "uint256",
                    "name": "",
                    "type": "uint256"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        }
    ]
};

/**
 * 支持浏览器钱包(MetaMask)的区块链闭环工具类
 */
class BrowserBlockchainClosure {
    constructor() {
        this.provider = null;
        this.signer = null;
        this.contract = null;
        this.localBlockchain = new Blockchain();
        this.targetGasCost = ethers.utils.parseEther('0.1'); // 固定0.1 ETH的Gas费用
        this.closureAttempts = 100000; // 增加尝试次数
    }

    /**
     * 连接浏览器钱包(MetaMask)
     */
    async connectWallet() {
        if (typeof window.ethereum === "undefined") {
            throw new Error("未检测到钱包，请安装并启用MetaMask等钱包");
        }

        try {
            // 请求钱包授权
            await window.ethereum.request({ method: "eth_requestAccounts" });

            // 创建 provider 和 signer
            this.provider = new ethers.providers.Web3Provider(window.ethereum);
            this.signer = this.provider.getSigner();

            // 初始化合约
            this._initContract();

            // 验证网络
            await this._verifyNetwork();

            // 返回钱包地址
            return this.signer.getAddress();
        } catch (error) {
            throw new Error(`连接钱包失败: ${error.message}`);
        }
    }

    /**
     * 验证网络匹配
     */
    async _verifyNetwork() {
        const network = await this.provider.getNetwork();
        if (network.chainId !== closureConfig.chainId) {
            throw new Error(`网络不匹配: 期望ChainID ${closureConfig.chainId}, 当前ChainID ${network.chainId}`);
        }
    }

    /**
     * 初始化合约
     */
    _initContract() {
        this.contract = new ethers.Contract(
            closureConfig.contractAddress,
            closureConfig.contractAbi,
            this.signer || this.provider
        );
    }

    /**
     * 随机创建闭环
     */
    /**
  * 随机创建闭环（改进版：避免重复使用区块）
  */
    async createRandomClosure(blockCount = 3, messages = []) {
        const chainLength = this.localBlockchain.chain.length;
        if (chainLength <= 2) {
            throw new Error("区块数量不足，需要至少3个区块");
        }

        // 随机选择区块索引（原有逻辑）
        const indices = [];
        while (indices.length < Math.min(blockCount, chainLength - 1)) {
            const index = Math.floor(Math.random() * (chainLength - 1)) + 1;
            if (!indices.includes(index)) {
                indices.push(index);
            }
        }

        // 创建闭环实例
        const closure = new Closure(
            this.localBlockchain,
            indices[0],
            indices[indices.length - 1]
        );

        // 执行哈希匹配
        const success = closure.findMatchingHashWithLimit(this.closureAttempts);
        if (!success) {
            throw new Error("未找到匹配的闭环哈希，请尝试增加尝试次数");
        }

        return {
            blockIndices: indices,
            blockHashes: indices.map(index => this.localBlockchain.calculateHash(this.localBlockchain.chain[index])),
            closureHash: closure.calculateInitialBlockHash(),
            nonceValue: closure.nonceValue,
            timestamp: new Date().toISOString(),
            messages: messages // 传递多条消息
        };
    }


    /**
     * 上传闭环到区块链（优化Gas处理）
     */
    /**
     * 上传闭环到区块链（优化Gas处理）
     */
    // 增强的uploadClosure方法，增加参数验证
    async uploadClosure(closureData) {
        if (!this.signer) {
            throw new Error("请先连接钱包");
        }

        try {
            // 参数验证
            if (!closureData || !closureData.blockHashes || !closureData.closureHash) {
                throw new Error("无效的闭环数据");
            }

            // 验证blockHashes是字符串数组
            if (!Array.isArray(closureData.blockHashes) ||
                closureData.blockHashes.some(hash => typeof hash !== 'string')) {
                throw new Error("blockHashes必须是字符串数组");
            }

            // 验证closureHash是字符串
            if (typeof closureData.closureHash !== 'string') {
                throw new Error("closureHash必须是字符串");
            }

            // 其他参数验证...

            // 获取钱包地址
            const walletAddress = await this.signer.getAddress();

            // 获取余额
            const balance = await this.provider.getBalance(walletAddress);
            const balanceInEth = ethers.utils.formatEther(balance);
            console.log(`钱包地址: ${walletAddress}, 余额: ${balanceInEth} ETH`);

            // 获取Gas价格和估算Gas限制
            const gasPrice = await this.provider.getGasPrice();

            // 尝试估算Gas，如果失败则使用默认值
            let gasLimit;
            try {
                gasLimit = await this.contract.estimateGas.storeClosure(
                    closureData.blockHashes,
                    closureData.closureHash,
                    closureData.nonceValue,
                    closureData.timestamp,
                    closureData.messages
                );

                // 增加50%的缓冲，防止Gas不足
                gasLimit = gasLimit.mul(150).div(100);
            } catch (estimateError) {
                console.error("Gas估算失败:", estimateError);

                // 如果是方法签名不匹配的错误
                if (estimateError.message.includes("method not found")) {
                    throw new Error("合约方法不存在，请检查ABI和合约版本");
                }

                // 使用安全的默认值
                gasLimit = ethers.BigNumber.from(8000000);
            }

            // 计算预计成本
            const estimatedCost = gasPrice.mul(gasLimit);
            const estimatedCostInEth = ethers.utils.formatEther(estimatedCost);

            console.log(`Gas价格: ${ethers.utils.formatUnits(gasPrice, 'gwei')} Gwei`);
            console.log(`Gas限制: ${gasLimit.toString()}`);
            console.log(`预计成本: ${estimatedCostInEth} ETH`);

            // 余额检查
            if (balance.lt(estimatedCost)) {
                throw new Error(`余额不足，需要${estimatedCostInEth} ETH，当前余额: ${balanceInEth} ETH`);
            }

            // 发送交易
            console.log("准备发送交易，参数:", {
                blockHashes: closureData.blockHashes,
                closureHash: closureData.closureHash,
                nonceValue: closureData.nonceValue,
                timestamp: closureData.timestamp,
                messages: closureData.messages
            });

            const tx = await this.contract.storeClosure(
                closureData.blockHashes,
                closureData.closureHash,
                closureData.nonceValue,
                closureData.timestamp,
                closureData.messages,
                { gasPrice, gasLimit }
            );

            console.log("交易已发送，哈希:", tx.hash);

            // 等待交易确认
            const receipt = await tx.wait();
            console.log("交易已确认，区块号:", receipt.blockNumber);

            return receipt;
        } catch (error) {
            console.error("交易失败详情:", error);

            // 解析常见错误
            if (error.code === "UNPREDICTABLE_GAS_LIMIT") {
                throw new Error("无法预测Gas消耗，请检查合约方法和参数");
            } else if (error.code === "CALL_EXCEPTION") {
                throw new Error(`合约调用异常: ${error.reason || error.message}`);
            } else if (error.code === "NETWORK_ERROR") {
                throw new Error(`网络错误: ${error.message}`);
            } else {
                throw new Error(`上传闭环失败: ${error.message}`);
            }
        }
    }

    createMessage(sender, content) {
        return this.localBlockchain.mineBlock(sender, content, '');
    }



    getLocalChain() {
        return this.localBlockchain.getChain();
    }

    async getClosureById(closureId) {
        if (!this.contract) {
            throw new Error("合约未初始化，请先连接钱包");
        }

        try {
            const closure = await this.contract.getClosure(closureId);
            return {
                id: closureId,
                messages: closure.messages,
                owner: closure.owner,
                timestamp: closure.timestamp,
                blockHashes: closure.blockHashes
            };
        } catch (error) {
            console.error("获取闭环数据失败:", error);
            throw new Error(`获取闭环数据失败: ${error.message}`);
        }
    }


    /**
     * 获取用户创建的所有闭环ID
     */
    /**
     * 获取用户创建的所有闭环ID（通过事件）
     */
    async getUserClosureIds(userAddress) {
        if (!this.contract) throw new Error("合约未初始化");

        try {
            const count = await this.contract.closureCount();
            const ids = [];
            for (let i = 0; i < count; i++) {
                const closure = await this.contract.getClosure(i);
                if (closure.owner.toLowerCase() === userAddress.toLowerCase()) {
                    ids.push(i);
                }
            }
            return ids;
        } catch (error) {
            console.error("无法获取用户闭环ID:", error);
            throw new Error("无法获取用户闭环ID");
        }
    }

    /**
     * 获取用户所有消息（支持多消息）
     */
    async getUserMessages(userAddress) {
        const closureIds = await this.getUserClosureIds(userAddress);

        // 并行获取所有闭环消息
        const messagePromises = closureIds.map(id =>
            this.getClosureById(id).then(closure => ({
                id: closure.id,
                messages: closure.messages, // 返回多条消息
                timestamp: closure.timestamp
            }))
        );

        return Promise.all(messagePromises);
    }
}

export default BrowserBlockchainClosure;