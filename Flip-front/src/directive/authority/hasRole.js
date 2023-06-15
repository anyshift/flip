import useUserStore from "../../stores/userStore";

/**
 * https://cn.vuejs.org/guide/reusability/custom-directives.html
 *
 */
export default {
    mounted(el, binding, vnode, prevVnode) {
        const {value} = binding; //解构出来的 value 是传递给指令的值。例如 v-hasRole="ROLE_ADMIN" 中，value 值是 ROLE_ADMIN。
        const roles = useUserStore().role;
        console.log(roles)

        if (value && value instanceof Array && value.length > 0) {
            const roleValue = value;

            const hasRole = roles.some(role => {
                return roleValue.includes(role)
            })

            if (!hasRole) {
                el.parentNode && el.parentNode.removeChild(el)
            }
        } else throw new Error('需要角色信息')
    }
}