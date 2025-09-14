// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract BlockchainClosure {
    // 定义事件（修改message为string[]）
    event ClosureStored(
        uint256 indexed id,
        string[] blockHashes,
        string closureHash,
        uint256 nonceValue,
        string timestamp,
        address indexed owner,
        string[] messages // 存储多条消息
    );
    
    // 定义闭环结构（修改message为string[]）
    struct Closure {
        string[] blockHashes;
        string closureHash;
        uint256 nonceValue;
        string timestamp;
        address owner;
        string[] messages; // 存储多条消息
        uint256 createdAt;
    }
    
    // 存储所有闭环
    mapping(uint256 => Closure) public closures;
    uint256 public closureCount;
    
    // 存储用户创建的闭环ID列表
    mapping(address => uint256[]) public userClosures;
    
    // 存储方法：创建并存储闭环（修改message为string[]）
    function storeClosure(
        string[] memory blockHashes,
        string memory closureHash,
        uint256 nonceValue,
        string memory timestamp,
        string[] memory messages // 接收多条消息
    ) external {
        // 检查参数有效性
        require(blockHashes.length > 0, unicode"至少需要一个区块哈希");
        require(bytes(closureHash).length > 0, unicode"需要有效的closure哈希");
        require(messages.length > 0, unicode"至少需要一条消息"); // 新增消息数量检查
        
        uint256 id = closureCount;
        closures[id] = Closure({
            blockHashes: blockHashes,
            closureHash: closureHash,
            nonceValue: nonceValue,
            timestamp: timestamp,
            owner: msg.sender,
            messages: messages, // 存储多条消息
            createdAt: block.timestamp
        });
        
        // 记录用户的闭环
        userClosures[msg.sender].push(id);
        
        // 触发事件
        emit ClosureStored(id, blockHashes, closureHash, nonceValue, timestamp, msg.sender, messages);
        
        // 增加计数器
        closureCount++;
    }
    
    // 查询方法：通过ID获取闭环（修改message为string[]）
    function getClosure(uint256 index) external view returns (
        string[] memory blockHashes,
        string memory closureHash,
        uint256 nonceValue,
        string memory timestamp,
        address owner,
        string[] memory messages, // 返回多条消息
        uint256 createdAt
    ) {
        require(index < closureCount, unicode"闭环不存在");
        Closure memory closure = closures[index];
        return (
            closure.blockHashes,
            closure.closureHash,
            closure.nonceValue,
            closure.timestamp,
            closure.owner,
            closure.messages, // 返回多条消息
            closure.createdAt
        );
    }

}