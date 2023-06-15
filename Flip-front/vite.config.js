import path from "path";

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'
import {loadEnv} from "vite"

// https://cn.vitejs.dev/config/
export default ({mode}) => defineConfig({
    envDir: './env',
    plugins: [
        vue(),
        AutoImport({
            resolvers: [ElementPlusResolver()],
        }),
        Components({
            resolvers: [ElementPlusResolver()],
        }),
    ],
    resolve: {
        alias: {
            '~': path.resolve(__dirname, '/'), //设置路径
            '@': path.resolve(__dirname, './src'), //设置别名
        },
        extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json'], /* https://cn.vitejs.dev/config/shared-options.html#resolve-extensions */
    },
    server: {
        port: 80, /* 指定前端端口 */
        host: true, /* 指定服务器应该监听哪个 IP 地址。 如果将此设置为 0.0.0.0 或者 true 将监听所有地址，包括局域网和公网地址。 */
        open: true, /* 启动Vite项目后在浏览器打开项目首页 */
        proxy: { /* https://cn.vitejs.dev/config/server-options.html#server-proxy], https://zxuqian.cn/vite-proxy-config */
            '/api' : {
                target: loadEnv(mode, './env').VITE_PROXY_TARGET, /*后端地址*/
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, ''), /* 重写，后端接收时不会有/api */
            }
        },
    },
})
