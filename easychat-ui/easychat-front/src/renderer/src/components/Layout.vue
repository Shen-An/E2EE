<template>
    <div class="layout-container">
        <div class="left-side-inner" :style="{ width: leftWidth + 'px' }">
            <slot name="left-content"></slot>
        </div>
        <div class="drag-handle" @mousedown="startResize"></div>
        <div class="right-content">
            <slot name="right-content"></slot>
        </div>

    </div>
</template>
<script setup>
import { ref } from 'vue';
// 左侧面板宽度
const leftWidth = ref(210);
// 最小宽度限制
const minWidth = ref(100);
// 最大宽度限制
const maxWidth = ref(500);

// 记录拖拽状态
let isResizing = false;
let startX = 0;
let startWidth = 0;

// 开始拖拽
const startResize = (e) => {
    isResizing = true;
    startX = e.clientX;
    startWidth = leftWidth.value;
    
    // 添加临时样式，提高拖拽时的鼠标捕获范围
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
    
    // 监听鼠标移动和释放事件
    document.addEventListener('mousemove', resize);
    document.addEventListener('mouseup', stopResize);
};

// 拖拽过程中调整宽度
const resize = (e) => {
    if (!isResizing) return;
    
    const delta = e.clientX - startX;
    let newWidth = startWidth + delta;
    
    // 应用宽度限制
    newWidth = Math.max(minWidth.value, Math.min(newWidth, maxWidth.value));
    
    leftWidth.value = newWidth;
};

// 结束拖拽
const stopResize = () => {
    isResizing = false;
    
    // 移除临时样式
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
    
    // 移除事件监听
    document.removeEventListener('mousemove', resize);
    document.removeEventListener('mouseup', stopResize);
};
</script>


<style lang="scss" scoped>
.layout-container{
    display: flex;
    .left-side-inner{
        width: 200px;
        background: #e6e5e5;
        border-color:#ddd;
        border-style: solid;
        border-width: 0px 1px 0px 0px;
    }
    .drag-handle {
        width: 2px;
        background: #ddd;
        cursor: col-resize;
        transition: background-color 0.2s;
        
        &:hover {
            background: #aaa;
        }
    }
    .right-content{
        flex: 1;
        height: calc(100vh - 2px);
        background: #f5f5f5;
        width: 100%;
    }
}
</style>