/*-
 * #%L
 * magic-obs
 * %%
 * Copyright (C) 2019 - 2021 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.magic.data

sealed class Either<TLeft, TRight> {
    data class Left<TLeft, TRight>(val value: TLeft) : Either<TLeft, TRight>()
    data class Right<TLeft, TRight>(val value: TRight) : Either<TLeft, TRight>()

    val isLeft: Boolean
        get() = this is Left<TLeft, TRight>

    val isRight: Boolean
        get() = this is Right<TLeft, TRight>

    fun leftOr(or: (TRight) -> Nothing): TLeft = when (this) {
        is Left<TLeft, TRight> -> this.value
        is Right<TLeft, TRight> -> or(this.value)
    }

    fun rightOr(or: (TLeft) -> Nothing): TRight = when (this) {
        is Left<TLeft, TRight> -> or(this.value)
        is Right<TLeft, TRight> -> this.value
    }

    fun leftOrNull() = (this as? Left<TLeft, TRight>)?.value
    fun rightOrNull() = (this as? Right<TLeft, TRight>)?.value
}

fun <TLeft, TRight> TLeft.left() = Either.Left<TLeft, TRight>(this)
fun <TLeft, TRight> TRight.right() = Either.Right<TLeft, TRight>(this)
