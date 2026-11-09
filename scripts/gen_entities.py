#!/usr/bin/env python3
"""hbm.xml → Kotlin JPA 实体生成器（rainbow-rcpq）

用法: python3 scripts/gen_entities.py [--table NAME]...
表清单在 TABLES 字典维护; 输出到 rcpq-entity/src/main/kotlin/com/jlrc/rcpq/entity/

修复记录(相对首版):
- 主键类型按 hbm 实际 type(不再一律 Long)——AA12.id VARCHAR→String
- 序列名读 hbm <param name="sequence"> 真实值——SEQ_AA01_AAA010 而非占位 AA01_SEQ
- 仅 generator class="sequence" 时生成 @GeneratedValue; assigned/composite 不生成
"""
import os
import re

RES = "/home/ubuntu/github/rainbowpq/src/main/resources"
OUT = os.path.join(os.path.dirname(__file__), "..", "rcpq-entity/src/main/kotlin/com/jlrc/rcpq/entity")

HBM = {
    "AA01": f"{RES}/com/neusoft/lemis/apps/entities/baseDB/A/AA01.hbm.xml",
    "AA10": f"{RES}/com/neusoft/lemis/apps/entities/baseDB/A/AA10.hbm.xml",
    "AA12": f"{RES}/com/neusoft/lemis/apps/entities/baseDB/A/AA12.hbm.xml",
    "SYS_QUESTION": f"{RES}/com/neusoft/lemis/apps/entities/baseDB/S/SYS_QUESTION.hbm.xml",
}

TYPE_MAP = {
    "java.lang.Long": "Long", "long": "Long",
    "java.lang.Integer": "Int", "integer": "Int", "int": "Int",
    "java.lang.String": "String", "string": "String",
    "java.math.BigDecimal": "BigDecimal", "big_decimal": "BigDecimal",
    "java.lang.Double": "BigDecimal", "double": "BigDecimal",
    "java.lang.Float": "BigDecimal", "float": "BigDecimal",
    "java.sql.Timestamp": "LocalDateTime", "timestamp": "LocalDateTime",
    "java.util.Date": "LocalDateTime", "date": "LocalDate",
    "java.sql.Date": "LocalDate",
    "java.lang.Boolean": "Boolean", "boolean": "Boolean", "true_false": "Boolean",
}
NUMERIC_ID = {"java.lang.Long", "long", "java.lang.Integer", "integer", "int"}


def gen(table, path):
    t = open(path, encoding="utf-8", errors="replace").read()
    props, seen = [], set()

    def add(nm, col, ty, orig):
        if nm not in seen:
            seen.add(nm)
            props.append((nm, col, TYPE_MAP.get(orig, "String"), orig))

    for m in re.finditer(r'<property\s+name="([^"]+)"\s+type="([^"]+)"[^>]*?(?:/>|>\s*<column\s+name="([^"]+)")', t, re.S):
        add(m.group(1), (m.group(3) or m.group(1)).upper(), TYPE_MAP.get(m.group(2), "String"), m.group(2))

    pk, seq, id_ty = [], None, None
    cid = re.search(r"<composite-id>(.*?)</composite-id>", t, re.S)
    if cid:
        for km in re.finditer(r'<key-property\s+name="([^"]+)"[^>]*?type="([^"]+)"', cid.group(1)):
            pk.append(km.group(1))
            add(km.group(1), km.group(1).upper(), TYPE_MAP.get(km.group(2), "String"), km.group(2))
    else:
        m = re.search(r'<id\s+name="([^"]+)"\s+type="([^"]+)"(.*?)</id>', t, re.S)
        if m:
            nm, id_ty = m.group(1), m.group(2)
            pk.append(nm)
            cm = re.search(r'<column\s+name="([^"]+)"', m.group(3))
            add(nm, (cm.group(1) if cm else nm).upper(), TYPE_MAP.get(id_ty, "String"), id_ty)
            gm = re.search(r'<generator\s+class="sequence">\s*<param\s+name="sequence">([^<]+)</param>', m.group(3))
            if gm:
                seq = gm.group(1)
    props.sort(key=lambda p: (p[0] not in pk,))  # 主键在前
    return props, pk, seq, id_ty, bool(cid)


def render(table, props, pk, seq, id_ty, composite):
    head = ('package com.jlrc.rcpq.entity\n\nimport jakarta.persistence.*\nimport java.io.Serializable\n'
            'import java.math.BigDecimal\nimport java.time.LocalDate\nimport java.time.LocalDateTime\n\n')
    lines = [head]
    if composite:
        lines.append(f'@Entity\n@Table(name = "{table}")\n@IdClass({table}Id::class)\nopen class {table} {{\n')
    else:
        lines.append(f'@Entity\n@Table(name = "{table}")\nopen class {table} {{\n')
    idprops = [p for p in props if p[0] in pk]
    others = [p for p in props if p[0] not in pk]
    if composite:
        for nm, col, ty, _ in idprops:
            lines.append(f'    @Id\n    @Column(name = "{col}")\n    var {nm}: {ty}? = null\n\n')
    else:
        nm, col, ty, orig = idprops[0]
        lines.append(f'    @Id\n')
        if seq:  # 仅真序列主键
            lines.append(f'    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "{seq}")\n'
                         f'    @SequenceGenerator(name = "{seq}", sequenceName = "{seq}", allocationSize = 1)\n')
        lines.append(f'    @Column(name = "{col}")\n    var {nm}: {ty}? = null\n\n')
    for nm, col, ty, _ in others:
        lines.append(f'    @Column(name = "{col}")\n    var {nm}: {ty}? = null\n\n')
    lines.append('}\n')
    if composite:
        lines.append(f'data class {table}Id(\n' +
                     ''.join(f'    val {nm}: {ty}? = null,\n' for nm, _, ty, _ in idprops) + ') : Serializable\n')
    return ''.join(lines)


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for table, path in HBM.items():
        props, pk, seq, id_ty, composite = gen(table, path)
        code = render(table, props, pk, seq, id_ty, composite)
        open(os.path.join(OUT, f"{table}.kt"), "w", encoding="utf-8").write(code)
        print(f"{table}: {len(props)}列 pk={pk} 序列={seq} id类型={id_ty} 复合={composite}")
