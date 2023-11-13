import {createRouter, createWebHistory} from 'vue-router'
import {getToken, removeToken, tokenExists} from "../utils/token";
import {ElMessage} from 'element-plus'
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import useUserStore from "../stores/userStore";

const global_title = import.meta.env.VITE_GLOBAL_TITLE;

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '',
            components: {default: () => import('../views/main/Main.vue'),},
            meta: {title: global_title, mainTransition: '', bodyTransition: '',},
            children: [
                {
                    path: '/', name: 'index',
                    alias: '',
                    components: {trendBody: () => import('../views/tabs/main/AllPosts.vue')},
                },
                {
                    path: '/n/latest', name: 'latestPosts',
                    components: {trendBody: () => import('../views/tabs/main/LatestPosts.vue')},
                },
                {
                    path: '/n/hot', name: 'hotPosts',
                    components: {trendBody: () => import('../views/tabs/main/HotPosts.vue')},
                },
            ],
        },
        {
            path: '/about',
            name: 'about',
            components: {
                default: () => import('../views/about/About.vue')
            },
            meta: {title: "关于 - " + global_title, requireAuth: true}
        },
        {
            path: '/tags',
            children: [
                {
                    path: '', name: 'tags',
                    components: {default: () => import('../views/tag/Tags.vue')},
                    meta: {title: "标签 - " + global_title, mainTransition: '', bodyTransition: ''},
                },
            ]
        },
        {
            path: '/t/:tagLabel',
            components: {default: () => import('../views/tag/Tag.vue')},
            props: {default: true},
        },
        {
            path: '/login',
            name: 'login',
            components: {
                default: () => import('../views/account/Login.vue'),
            },
            meta: {title: '登录 - ' + global_title}
        },
        {
            path: '/register',
            name: 'register',
            components: {
                default: () => import('../views/account/Register.vue'),
            },
            meta: {title: '注册 - ' + global_title}
        },
        {
            path: '/activate',
            name: 'activate',
            components: {
                default: () => import('../views/account/Activate.vue'),
            },
            meta: {title: "激活账号 - " + global_title, requireAuth: true}
        },
        {
            path: '/u/:username',
            name: 'profile',
            components: {
                default: () => import('../views/user/Profile.vue'),
            },
            props: {default: true},
        },
        {
            path: '/sys-ctrl', name: 'admin',
            components: {
                default: () => import('../views/admin/Admin.vue')
            },
            children: [
                {
                    path: '', name: 'adminControl',
                    alias: '/sys-ctrl',
                    components: {sysMain: () => import('../views/tabs/admin/AdminOverview.vue')},
                    meta: {title: '管理中心 - ' + global_title}
                },
                {
                    path: 'mail', name: 'adminMail',
                    components: {sysMain: () => import('../views/tabs/admin/AdminMail.vue')},
                    meta: {title: '管理中心 - 邮件 - ' + global_title}
                },
                {
                    path: 'tag', name: 'adminTag',
                    components: {sysMain: () => import('../views/tabs/admin/AdminTag.vue')},
                    meta: {title: '管理中心 - 标签 - ' + global_title}
                },
                {
                    path: 'authority', name: 'adminAuthority',
                    components: {sysMain: () => import('../views/tabs/admin/AdminAuthority.vue')},
                    meta: {title: '管理中心 - 权限 - ' + global_title}
                },
                {
                    path: 'sensitive', name: 'adminSensitive',
                    components: {sysMain: () => import('../views/tabs/admin/AdminSensitive.vue')},
                    meta: {title: '管理中心 - 敏感词 - ' + global_title}
                },
                {
                    path: 'user', name: 'adminUser',
                    components: {sysMain: () => import('../views/tabs/admin/AdminUser.vue')},
                    meta: {title: '管理中心 - 用户管理 - ' + global_title}
                }
            ],
            meta: {requireAuth: true, requireAdmin: true, requireEmailVerified: true},
        },
        {
            path: '/setting',
            name: 'setting',
            components: {
                default: () => import('../views/user/Setting.vue'),
            },
            meta: {title: '账号设置 - ' + global_title, requireAuth: true}
        },
        {
            path: '/write',
            name: 'writePost',
            components: {
                default: () => import('../views/post/write/WritePost.vue'),
            },
            meta: {title: '发布帖子 - ' + global_title, requireAuth: true, requireEmailVerified: true},
        },
        {
            path: '/p/:pid(\\d+)',
            name: 'viewPost',
            components: {
                default: () => import('../views/post/view/ViewPost.vue'),
            },
            props: {default: true},
            meta: {title: global_title}
        },
        {
            path: '/search',
            name: 'search',
            components: {
                default: () => import('../views/search/Search.vue'),
            },
            props: {default: true},
            meta: {title: '搜索 - ' + global_title}
        },
        {
            path: '/test',
            name: 'test',
            components: {
                default: () => import('../views/user/RoleTest.vue'),
            },
            meta: {requireAuth: true}
        },
        {
            path: '/:pathMatch(.*)*',
            name: 'error',
            components: {
                default: () => import('../views/error/404.vue'),
            },
            meta: {title: '404 - 页面不存在'}
        }
    ],
    scrollBehavior(to, from, savedPosition) {
        if (to.hash) {
            if (to.path === from.path) {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        if (to.name === 'tags') {
                            resolve({el: to.hash, top: '75', behavior: 'smooth'})
                        } else if (to.name === 'viewPost') {
                            if (to.hash === '#comments') {
                                return new Promise((resolve, reject) => {
                                    setTimeout(() => {
                                        resolve({el: to.hash, top: '80', behavior: 'smooth'})
                                    }, 1500)
                                })
                            } else {
                                resolve({el: to.hash, top: '55', behavior: 'smooth'})
                            }
                        } else {
                            resolve({el: to.hash, behavior: 'smooth'})
                        }
                    }, 0)
                })
            } else {
                if (to.name === 'viewPost') {
                    if (to.hash === '#comments') {
                        return new Promise((resolve, reject) => {
                            setTimeout(() => {
                                resolve({el: to.hash, top: '80', behavior: 'smooth'})
                            }, 1500)
                        })
                    } else {
                        return new Promise((resolve, reject) => {
                            setTimeout(() => {
                                resolve({el: to.hash, behavior: 'smooth'})
                            }, 2200)
                        })
                    }
                } else {
                    return new Promise((resolve, reject) => {
                        setTimeout(() => {
                            if (to.name === 'tags') {
                                resolve({el: to.hash, top: '75', behavior: 'smooth'})
                            } else {
                                resolve({el: to.hash, behavior: 'smooth'})
                            }
                        }, 800)
                    })
                }
            }
        }

        if (savedPosition) {
            return savedPosition
        } else return {top: 0}
    },
})

/* 全局守卫 */
router.beforeEach((to, from, next) => {

    /* 修改标题 */
    if (to.meta.title) {
        document.title = to.meta.title
    }

    /*首页tab栏切换时无需transition过渡动画的route name*/
    let noTransitionRouteArray = ['index', 'hotPosts', 'latestPosts'];
    if (noTransitionRouteArray.indexOf(to.name) !== -1) { /*如果元素在数组里面能匹配到，则会返回在数组内的序号，否则返回负一*/
        if (noTransitionRouteArray.indexOf(from.name) === -1) {
            to.meta.mainTansition = 'fade-transform';
            to.meta.bodyTransition = 'fade';
        } else { /*非首页进入首页时需要过渡动画*/
            to.meta.mainTransition = 'noTransition';
            to.meta.bodyTransition = 'noTransition';
        }
    } else { /*非首页tab切换则需要过渡动画*/
        to.meta.mainTransition = 'fade-transform';
        to.meta.bodyTransition = 'fade';
    }

    /*如果localStorage存有token，则代表已登录，会去判断已登录用户信息是否完整，不完整的话会拉取登录用户的信息存到pinia，一次存取，除非刷新或重开页面，否则不会二次获取用户信息*/
    if (tokenExists()) {
        NProgress.start();
        if (to.path === '/login' || to.path === '/register') {
            document.title = global_title;
            next({path: '/'});
        } else {
            if (useUserStore().role.length === 0) {
                setTimeout(() => {
                    useUserStore().getProfile().then(response => {
                        useUserStore().isLogin = true;
                        useUserStore().token = getToken();
                        if (to.meta.requireAdmin) {
                            const isADMIN = useUserStore().role.some(role => {
                                return role.includes('ADMIN')
                            });
                            if (!isADMIN) {
                                next({name: 'error'})
                            } else next()
                        } else {
                            if (to.meta.requireEmailVerified && !useUserStore().emailVerified) {
                                next({path: '/'})
                                ElMessage({
                                    message: '请激活邮箱后访问',
                                    type: 'error'
                                })
                            } else next();
                        }
                    }).catch(error => {
                        console.log(error)
                        if (to.meta.requireAdmin) {
                            let isADMIN = useUserStore().role.some(role => {
                                return role.includes('ADMIN')
                            });
                            if (!isADMIN) {
                                next({name: 'error'})
                            }
                        } else {
                            if (error.code === 412 || error.code === 419) {
                                next({path: '/login'})
                            }
                        }
                    })
                }, 500)
            } else {
                NProgress.start();
                if (to.meta.requireEmailVerified && !useUserStore().emailVerified) {
                    next({path: '/'})
                    ElMessage({
                        message: '请激活邮箱后访问',
                        type: 'error'
                    })
                } else next();
            }
        }
    } else {
        NProgress.start();
        /* 如果访问的路径需要登录 */
        if (to.meta.requireAdmin) {
            let isADMIN = useUserStore().role.some(role => {
                return role.includes('ADMIN')
            });
            if (!isADMIN) {
                next({name: 'error'})
                return;
            }
        }
        if (to.meta.requireAuth) {
            ElMessage({
                showClose: true,
                message: '请登录后访问',
                type: 'error',
                duration: 3000
            })
            next({path: '/login'})
        } else next()
    }
})

router.afterEach(() => {
    NProgress.done();
})

export default router;

