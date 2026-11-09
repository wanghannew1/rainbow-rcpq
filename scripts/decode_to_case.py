#!/usr/bin/env python3
"""Oracle DECODE(a, k1, v1, k2, v2, ..., default?) → PG CASE 表达式转换器(嵌套安全)。

语义: 值比较用 IS NOT DISTINCT FROM(NULL=NULL 命中, 同 Oracle DECODE); 无 default 且未命中 → NULL。
用法: from decode_to_case import decode_to_case
"""
import re

def decode_to_case(sql: str) -> str:
    def find_call(s, start):
        """返回与 start 处 'DECODE(' 配对的右括号索引"""
        depth = 0
        i = start
        while i < len(s):
            if s[i] == '(':
                depth += 1
            elif s[i] == ')':
                depth -= 1
                if depth == 0:
                    return i
            i += 1
        raise ValueError("unbalanced")

    def split_args(s):
        args, depth, cur = [], 0, []
        in_str = False
        for ch in s:
            if ch == "'" and not in_str:
                in_str = True; cur.append(ch)
            elif ch == "'" and in_str:
                in_str = False; cur.append(ch)
            elif not in_str and ch == '(':
                depth += 1; cur.append(ch)
            elif not in_str and ch == ')':
                depth -= 1; cur.append(ch)
            elif not in_str and ch == ',' and depth == 0:
                args.append(''.join(cur)); cur = []
            else:
                cur.append(ch)
        args.append(''.join(cur))
        return [a.strip() for a in args]

    def replace_one(s):
        idx = s.upper().find('DECODE(')
        if idx < 0:
            return None
        close = find_call(s, idx + len('DECODE') )
        inner = s[idx + len('DECODE('):close]
        args = split_args(inner)
        target, pairs = args[0], args[1:]
        whens = []
        default = None
        if len(pairs) % 2 == 1:
            default = pairs[-1]
            pairs = pairs[:-1]
        for i in range(0, len(pairs), 2):
            whens.append(f"WHEN ({target}) IS NOT DISTINCT FROM ({pairs[i]}) THEN ({pairs[i+1]})")
        case = "CASE " + " ".join(whens)
        if default is not None:
            case += f" ELSE ({default})"
        case += " END"
        return s[:idx] + case + s[close+1:]

    while True:
        replaced = replace_one(sql)
        if replaced is None:
            return sql
        sql = replaced

if __name__ == '__main__':
    import sys
    print(decode_to_case(sys.stdin.read()))
