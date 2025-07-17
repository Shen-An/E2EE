import { defineStore } from 'pinia'
export const useAESKeyStore = defineStore('AESKeyStore', {
    state: () => {
        return {
            AESKey: {},
           
        }
    },
    actions: {
        setAESKey(email, AESKey) {
            this.AESKey[email] = AESKey;
        },
        getAESKey(email) {
            return this.AESKey[email];
        },
        
    }
})