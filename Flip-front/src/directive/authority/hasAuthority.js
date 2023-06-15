import useUserStore from "../../stores/userStore";

/**
 * https://cn.vuejs.org/guide/reusability/custom-directives.html
 */
export default {
    mounted(el, binding, vnode, prevVnode) {
        const {value} = binding; //解构出来的 value 是传递给指令的值。例如 v-hasAuthority="activity:post:write" 中，value 值是 activity:post:write。
        const authorities = useUserStore().authorities;

        if (value && value instanceof Array && value.length > 0) {
            const authorityValue = value;

            const hasAuthority = authorities.some(authority => {
                return authorityValue.includes(authority.authority)
            })

            if (!hasAuthority) {
                el.parentNode && el.parentNode.removeChild(el); //el是指令绑定的元素
            }
        } else throw new Error('需要权限信息')
    }
}