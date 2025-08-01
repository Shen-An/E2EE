import { defineStore } from 'pinia'
export const useSPCEKeyGenStore = defineStore('SPCEKeyGenStore', {
    state: () => {
        return {
            SPCEKeyGenStore: {},
           
        }
    },
    actions: {
        setSPCEKeyGen(key, keyValue) {
            this.SPCEKeyGenStore[key] = keyValue;
        },
        getSPCEKeyGen(key) {
            return this.SPCEKeyGenStore[key];
        },
        
    }
})