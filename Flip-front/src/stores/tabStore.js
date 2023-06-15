import {defineStore} from "pinia";

const tabPrefix = '/n/';

const useTabStore = defineStore("tab", {
    state: () => ({
        mainTabs: [
            { name: "全部", path: "/", },
            { name: "最新", path: tabPrefix + "latest", },
            { name: "热门", path: tabPrefix + "hot", },
            { name: "标签", path: '/tags', icon: "czs-block-l" },
        ],
        menuTabs: [
            { name: "标签", path: '/tags', icon: "czs-block-l" },
            /*{ name: "广场", path: '/plaza', icon: "czs-shop-l" },*/
        ],
        userTabs: [
            { name: "主题", path: '' },
            { name: "评论", path: 'comments' },
            { name: "关注", path: 'follow' },
            { name: "收藏", path: 'bookmark' },
        ],
        defaultAsideMainMenus: [
            { name: "首页", path: "/", icon: "czs-home-l" },
            { name: "博客", path: "/blog", icon: "czs-book-l" },
            { name: "广场", path: "/plaza", icon: "czs-shop-l" },
            { name: "标签", path: "/tags", icon: "czs-block-l" }
        ],
        defaultAsideMinorMenus: [
            { name: "聊天室", path: "/chat", icon: "czs-talk-l" },
            { name: "朋友圈", path: "/moments", icon: "czs-moments" },
        ],
    })
})

export default useTabStore