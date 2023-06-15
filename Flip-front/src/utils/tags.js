/**
 * 转换标签，用于在tags页面展示
 * @param tags
 * @param tagOptions
 * @returns {{}}
 */
export function classifyTags(tags, tagOptions) {
    let allTag = JSON.parse(JSON.stringify(tags));
    let result = {};

    tagOptions.forEach(tagOption => {
        if (!result.hasOwnProperty(tagOption.label)) {
            result[tagOption.label] = []
        }
    });

    allTag.forEach(tag => {
        result[tag.tagOption.label].push(tag)
    })

    return result;
}

/**
 * 让「其它」类型永远排在最后面，用于在tags页面展示
 * @param tagOptions
 */
export function classifyTagOptions(tagOptions) {
    let result = [];
    let other = {};
    let isOther = false;

    tagOptions.forEach(tagOption => {
        isOther = false;
        if (tagOption.label === 'other') {
            isOther = true;
            other = tagOption;
        }
        if (!isOther) {
            result.push(tagOption);
        }
    })
    result.push(other);
    return result;
}

/**
 * 级联标签，用于el-cascader组件中展示
 * @param tags
 * @param tagOptions
 */
export function cascadeTags(tags, tagOptions) {
    let allTag = JSON.parse(JSON.stringify(tags));
    let result = [];

    tagOptions.forEach(tagOption => {
        if (!result[tagOption.label]) {
            result.push({
                value: tagOption.label,
                label: tagOption.name,
                children: [],
            })
        }
    })

    allTag.forEach(tag => {
        result.forEach(option => {
            if (option.value === tag.tagOption.label) {
                option.children.push({
                    value: tag.label,
                    label: tag.name,
                });
            }
        })
    })

    return result;
}