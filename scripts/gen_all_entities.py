#!/usr/bin/env python3
"""全量实体生成: 扫描全部 hbm.xml → Kotlin JPA 实体(已生成的 16 表跳过覆盖)
处理: $_ 反编译产物名($→_)、复合主键@IdClass、序列生成器、无 hbm 属性类型兜底"""
import os
import re

RES = "/home/ubuntu/github/rainbowpq/src/main/resources"
OUT = "/home/ubuntu/github/rainbow-rcpq/rcpq-entity/src/main/kotlin/com/jlrc/rcpq/entity"
DONE = {"AA01", "AA10", "AA12", "SYS_QUESTION", "AB01", "AB06", "AB07", "SYS_FILE",
        "AC01", "AC01_LB", "AC06", "AC0B", "AC0D", "CC0P", "TC90", "TC90_LIST"}

TYPE_MAP = {
    "java.lang.Long": "Long", "long": "Long",
    "java.lang.Integer": "Int", "integer": "Int", "int": "Int",
    "java.lang.String": "String", "string": "String",
    "java.math.BigDecimal": "BigDecimal", "big_decimal": "BigDecimal",
    "java.lang.Double": "BigDecimal", "double": "BigDecimal",
    "java.lang.Float": "BigDecimal", "float": "BigDecimal",
    "java.sql.Timestamp": "LocalDateTime", "timestamp": "LocalDateTime",
    "java.util.Date": "LocalDateTime", "date": "LocalDate", "java.sql.Date": "LocalDate",
    "java.lang.Boolean": "Boolean", "boolean": "Boolean", "true_false": "Boolean", "yes_no": "Boolean",
    "java.lang.Byte": "Int", "byte": "Int",
}
NUMERIC_ID = {"java.lang.Long", "long", "java.lang.Integer", "integer", "int"}

def cls_name(hbm_class):
    """表名/类名 → 合法 Kotlin 标识符($_ → _)"""
    return hbm_class.replace("$", "_")

def parse(path):
    t = open(path, encoding="utf-8", errors="replace").read()
    m = re.search(r'<class\s+name="([^"]+)"[^>]*?table="([^"]*)"', t)
    if not m:
        return None
    fq, table = m.group(1), m.group(2)
    props, seen = [], set()

    def add(nm, col, ty, orig):
        if nm and nm not in seen:
            seen.add(nm)
            props.append((nm, (col or nm).upper(), TYPE_MAP.get(orig, "String"), orig))

    for pm in re.finditer(r'<property\s+name="([^"]+)"\s+type="([^"]+)"[^>]*?(?:/>|>\s*<column\s+name="([^"]+)")', t, re.S):
        add(pm.group(1), pm.group(3), TYPE_MAP.get(pm.group(2), "String"), pm.group(2))
    pk, seq, composite = [], None, False
    cid = re.search(r"<composite-id>(.*?)</composite-id>", t, re.S)
    if cid:
        composite = True
        for km in re.finditer(r'<key-property\s+name="([^"]+)"[^>]*?type="([^"]+)"', cid.group(1)):
            add(km.group(1), km.group(1).upper(), TYPE_MAP.get(km.group(2), "String"), km.group(2))
            pk.append(km.group(1))
    else:
        m2 = re.search(r'<id\s+name="([^"]+)"\s+type="([^"]+)"(.*?)</id>', t, re.S)
        if m2:
            nm, ty = m2.group(1), m2.group(2)
            cm = re.search(r'<column\s+name="([^"]+)"', m2.group(3))
            add(nm, (cm.group(1) if cm else nm).upper(), TYPE_MAP.get(ty, "String"), ty)
            pk.append(nm)
            gm = re.search(r'<generator\s+class="sequence">\s*<param\s+name="sequence">([^<]+)</param>', m2.group(3))
            if gm:
                seq = gm.group(1)
    props.sort(key=lambda p: (p[0] not in pk,))
    if not pk:
        return None  # 无主键的 hbm(视图型映射)不可作 JPA 实体
    return fq, table, props, pk, seq, composite

def render(cls, table, props, pk, seq, composite):
    out = ['package com.jlrc.rcpq.entity\n\nimport jakarta.persistence.*\nimport java.io.Serializable\n'
           'import java.math.BigDecimal\nimport java.time.LocalDate\nimport java.time.LocalDateTime\n\n']
    if composite:
        out.append(f'@Entity\n@Table(name = "{table}")\n@IdClass({cls}Id::class)\nopen class {cls} {{\n')
    else:
        out.append(f'@Entity\n@Table(name = "{table}")\nopen class {cls} {{\n')
    idp = [p for p in props if p[0] in pk]
    rest = [p for p in props if p[0] not in pk]
    if composite:
        for nm, col, ty, _ in idp:
            out.append(f'    @Id\n    @Column(name = "{col}")\n    var {nm}: {ty}? = null\n\n')
    elif idp:
        nm, col, ty, orig = idp[0]
        out.append('    @Id\n')
        if seq:
            out.append(f'    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "{seq}")\n'
                       f'    @SequenceGenerator(name = "{seq}", sequenceName = "{seq}", allocationSize = 1)\n')
        jty = ty if orig in NUMERIC_ID or ty in ("Long", "Int", "BigDecimal") else ty
        out.append(f'    @Column(name = "{col}")\n    var {nm}: {jty}? = null\n\n')
    for nm, col, ty, _ in rest:
        out.append(f'    @Column(name = "{col}")\n    var {nm}: {ty}? = null\n\n')
    out.append('}\n')
    if composite:
        out.append(f'data class {cls}Id(\n' + ''.join(
            f'    val {nm}: {ty}? = null,\n' for nm, _, ty, _ in idp) + ') : Serializable\n')
    return ''.join(out)

def main():
    os.makedirs(OUT, exist_ok=True)
    n_ok = n_skip = n_nopk = 0
    for dp, _, fs in os.walk(RES):
        for fn in fs:
            if not fn.endswith(".hbm.xml"):
                continue
            r = parse(os.path.join(dp, fn))
            if not r:
                n_nopk += 1
                continue
            fq, table, props, pk, seq, composite = r
            simple = fq.split(".")[-1]
            if simple in DONE:
                n_skip += 1
                continue
            cls = cls_name(simple)
            code = render(cls, table, props, pk, seq, composite)
            with open(os.path.join(OUT, f"{cls}.kt"), "w", encoding="utf-8") as f:
                f.write(code)
            n_ok += 1
    print(f"生成 {n_ok} 个实体, 跳过已存在 {n_skip}, 无主键跳过 {n_nopk}")

if __name__ == "__main__":
    main()
