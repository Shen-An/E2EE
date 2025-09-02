import { ethers } from "ethers";

// Whitechain Testnet 网络配置
const whitechainConfig = {
  rpcUrl: "https://rpc-testnet.whitechain.io", // 正确的绝对 URL
  chainId: 2625, // 链 ID
};

// 合约相关配置（根据实际情况替换）
const contractConfig = {
  address: "0xd00b8577cd693f45310d5d5c33a818dc45142c03", // 合约地址
  abi: [
    {
      "inputs": [
        {
          "internalType": "string",
          "name": "newMessage",
          "type": "string"
        }
      ],
      "name": "setMessage",
      "outputs": [],
      "stateMutability": "nonpayable",
      "type": "function"
    },
    {
      "inputs": [
        {
          "internalType": "string",
          "name": "initialMessage",
          "type": "string"
        }
      ],
      "stateMutability": "nonpayable",
      "type": "constructor"
    },
    {
      "inputs": [],
      "name": "getMessage",
      "outputs": [
        {
          "internalType": "string",
          "name": "",
          "type": "string"
        }
      ],
      "stateMutability": "view",
      "type": "function"
    }
  ], // 合约 ABI
};

// 封装连接与交互类
class WhitechainConnector {
  constructor() {
    // 初始化 provider（连接区块链网络的基础对象）
    this.provider = new ethers.providers.JsonRpcProvider(whitechainConfig.rpcUrl);
    // 初始化合约实例（先基于 provider 做只读交互，如需写操作再结合 signer）
    this.contract = new ethers.Contract(
      contractConfig.address,
      contractConfig.abi,
      this.provider
    );
    this.signer = null; // 用于签名交易（写操作时需设置）
  }

  // 连接钱包（获取 signer，用于写操作，这里以 MetaMask 为例）
  async connectWallet() {
    if (typeof window.ethereum === "undefined") {
      throw new Error("未检测到 MetaMask 等钱包，请安装并启用");
    }
    // 请求钱包授权
    await window.ethereum.request({ method: "eth_requestAccounts" });
    // 基于钱包创建 signer
    const web3Provider = new ethers.providers.Web3Provider(window.ethereum);
    this.signer = web3Provider.getSigner();
    // 更新合约实例为可写（结合 signer）
    this.contract = this.contract.connect(this.signer);
    return this.signer.getAddress(); // 返回当前钱包地址
  }

  // 调用合约只读方法（示例：获取消息）
  async getMessage() {
    return this.contract.getMessage();
  }

  // 调用合约写方法（示例：设置消息）
  async setMessage(newMessage) {
    if (!this.signer) {
      throw new Error("请先调用 connectWallet 连接钱包");
    }
    const tx = await this.contract.setMessage(newMessage);
    return tx.wait(); // 等待交易确认，返回交易收据
  }


}

// 导出单例实例，方便全局调用
export default new WhitechainConnector();