import { defineStore } from 'pinia'
export const useMessageCountStore = defineStore('messageCount', {
    state: () => {
        return {
            messageCount: {
                chatCount: 0,
                contactApplyCount: 0
            }

        }
    },
    actions: {
        setCount(key, count, forceUpdate) {
            if (forceUpdate) {
                this.messageCount[key] = count;
                return;
            }
            let curCount = this.messageCount[key];
            this.messageCount[key] = curCount + count;
            console.log("setCount", this.messageCount[key])
        },
        getCount(key) {
            return this.messageCount[key];
        }
    }
});