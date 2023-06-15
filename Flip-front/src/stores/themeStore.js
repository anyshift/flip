import {defineStore} from "pinia/dist/pinia";

const useThemeStore = defineStore('theme', {
    state: () => ({
        currentTheme: '',
        hljsTheme: '',
    }),
})

export default useThemeStore