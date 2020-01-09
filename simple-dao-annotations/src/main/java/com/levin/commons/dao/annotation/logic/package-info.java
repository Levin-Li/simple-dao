/**
 * 逻辑分组
 * <p/>
 * class DTO {
 *
 * @AND int a;
 * @OR String b;
 * <p/>
 * int c;
 * @END int d
 * <p/>
 * }
 * @END 以上将生成 (a =? and ( b =? or c =?) and d =? )
 */
package com.levin.commons.dao.annotation.logic;