package com.flip.validation;

import jakarta.validation.GroupSequence;

/**
 * VG：ValidationGroup
 * 分组校验接口，用于定于校验顺序，只要有其中一个顺序的校验不通过，后续顺序的校验将会停止。
 * 此接口用在 @Validated 或 @Valid 中。（不配置的话，默认是随机顺序校验，并且会把全部都校验一遍，返回的结果可能是一大串）
 */
@GroupSequence({VG.First.class, VG.Second.class, VG.Third.class, VG.Fourth.class, VG.Fifth.class,
        VG.Sixth.class, VG.Seventh.class, VG.Eighth.class, VG.Ninth.class, VG.Tenth.class})
public interface VG {
    interface First {}

    interface Second {}

    interface Third {}

    interface Fourth {}

    interface Fifth {}

    interface Sixth {}

    interface Seventh {}

    interface Eighth {}

    interface Ninth {}

    interface Tenth {}
}
