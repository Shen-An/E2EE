import { defineStore } from 'pinia'
export const useUserInfoStore = defineStore('userInfo', {
    state: () => {
        return {
            userInfo: {}
        }
    },
    actions: {
        setInfo(info) {
            this.userInfo = info;
            localStorage.setItem('userInfo', JSON.stringify(info));
        },
        getInfo() {
            return this.userInfo;
        }
    }
});