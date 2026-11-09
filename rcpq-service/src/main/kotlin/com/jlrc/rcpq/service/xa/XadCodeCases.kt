package com.jlrc.rcpq.service.xa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component

/** xa 人员代码表用例（旧 XaadService.doQuery 的规格重写） */
@Component
class XadCodeCases(
    @PersistenceContext private val em: EntityManager,
) {
    /**
     * 查询指定类别的 AA10 代码项。
     * 旧实现硬编码 aaa101='健康状况' 且存在 list.get(1) 索引 bug（循环取固定第2条）；
     * 此处按参数化类别查询全部项并修正索引——语义按意图保真，缺陷修复记录在案。
     */
    fun queryCodes(category: String): List<Map<String, String?>> =
        em.createQuery(
            "select t.aaa103, t.aaa101 from AA10 t where t.aaa101 = :cat", Array<Any>::class.java)
            .setParameter("cat", category)
            .resultList
            .map { pair -> mapOf("aaa103" to pair[0]?.toString(), "aaa101" to pair[1]?.toString()) }
}
