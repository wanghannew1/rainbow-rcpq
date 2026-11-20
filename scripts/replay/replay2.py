#!/usr/bin/env python3
"""replay2.py — 东软 Lemis(现网 .70 / 本地 Tomcat) 报文重放(2026-11-21 全通道打通版)

与 rpc.sh 的区别(全部为实测勘定的必要形状, 缺一即 500/-30025/-30010):
  1. Content-Type 必须 text/plain(≠multipart/form-data — 那会让 Tomcat 消费流→空 DC→-30025)
  2. header ajaxRequest:true → PojoAction 走 getInputStream() raw JSON 分支
  3. DC 级 parameters 必须带全套: pojo.pagename + pojo.modelname + pojo.dstype=POJODS
     + pojo.interaction + synCount —— 'name' must not be null = 缺 pagename(按页面找 JspBean)
  4. store 级 parameters 同上 + pojo.usetablealias=false; rowSet 必须带齐 primary+filter+delete
     (缺 filter → -30010 记录日志错误: JSONObject["filter"] is not a JSONArray)
  5. 实体 FQN 包段按表分组: TC9W/T.AA97 在 baseDB.{T,A,...}(看页面 .ds.js 的 rowSetName)
  6. 交互 id = 页面 JS setInteraction 原文(语义名即真 id; 同页不同 store 不同 id)
  7. proc 通道: pojo.interaction=REQ-UTIL-procedureService-Q + proName/paraIn/paraOutType
  8. 已知怪癖: 过程报错时东软响应写出器崩(JSONException null object→500) — 过程实已执行
     (SYS_ERR_LOG+回滚), 用户可见=系统错误弹窗; 成功路径 200 正常

会话: POST /rcpq/j_unieap_security_check.do (j_username/j_password, 凭据不入库—
  调 login 子命令时传参, cookie 文件被 .gitignore 的 work/ 承载)。Set-Cookie 不落地
  (URL 重写 jsessionid), replay2.py login 已自动抓 SID 写 cookie。

用法:
  python3 replay2.py login [user pass]                    — 登录并回写 cookie 文件
  python3 replay2.py query <页面> <store> <交互id> <实体FQN> <condition> [pageSize]
  python3 replay2.py proc <proName> '<paraIn JSON数组>'
COOKIE 环境变量覆盖默认; PAGE_ROOT 环境变量覆盖 APP 根(http://localhost:8090/rcpq)。
"""
import hashlib
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request

APP = os.environ.get("PAGE_ROOT", "http://localhost:8090/rcpq")
COOKIE = os.environ.get("COOKIE", "/home/ubuntu/github/rainbow-rcpq/work/cj_legacy_local.txt")


def js_escape(s: str) -> str:
    out = []
    for ch in s:
        o = ord(ch)
        if ch.isalnum() or ch in "@*_+-./":
            out.append(ch)
        elif o < 256:
            out.append("%%%02X" % o)
        else:
            out.append("%%u%04X" % o)
    return "".join(out)


def sid() -> str:
    for line in open(COOKIE).read().splitlines():
        if line.strip() and not line.startswith("#"):
            f = line.split("\t")
            if len(f) >= 7:
                return f[6]
    raise SystemExit("cookie 文件无 JSESSIONID: " + COOKIE)


def post(url: str, body: str, ajax: bool = True) -> tuple:
    headers = {"Content-Type": "text/plain", "Cookie": "JSESSIONID=" + sid()}
    if ajax:
        headers["ajaxRequest"] = "true"
        digest = hashlib.sha1(js_escape(body).lower().encode()).hexdigest()  # isEncrypt=true 时备而不用
        headers["digest"] = digest
    req = urllib.request.Request(url, data=body.encode("utf-8"), headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=120) as r:
            return r.status, r.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", "replace")


def login(user: str, pwd: str) -> None:
    # 登录 → 抓最终 URL 的 jsessionid(URL 重写) → 写 Netscape cookie 文件
    import http.cookiejar
    cj = http.cookiejar.CookieJar()
    op = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    op.open(urllib.request.Request(APP + "/j_unieap_security_check.do",
        data=urllib.parse.urlencode({"j_username": user, "j_password": pwd}).encode()), timeout=30)
    sid_val = None
    for c in cj:
        if c.name == "JSESSIONID":
            sid_val = c.value
    if not sid_val:  # URL 重写兜底: 从任意后续页面的重定向里抓
        r = op.open(APP + "/login.do?method=begin", timeout=30)
        m = re.search(r"jsessionid=([0-9A-Fa-f]+)", r.geturl() + (r.read().decode("utf-8", "replace")))
        sid_val = m.group(1) if m else None
    if not sid_val:
        raise SystemExit("登录失败: 未获得 JSESSIONID")
    with open(COOKIE, "w") as f:
        f.write("# Netscape HTTP Cookie File\n")
        host = urllib.parse.urlparse(APP).hostname
        f.write(f"{host}\tFALSE\t/\tFALSE\t0\tJSESSIONID\t{sid_val}\n")
    print("登录成功, cookie →", COOKIE)


def query(modelpath: str, store: str, interaction: str, entity: str, cond: str,
          page_size: int = 10) -> tuple:
    """modelpath 如 /lemis/pages/td/c/a/tdca_cont.jsp; pagename=tdca_cont 自动推导"""
    page = os.path.basename(modelpath).replace("_cont.jsp", "")
    sp = {"pojo.modelname": modelpath, "pojo.dstype": "POJODS",
          "pojo.usetablealias": "false", "pojo.interaction": interaction,
          "pojo.pagename": page}
    dc = {"header": {"ajaxRequest": True}, "body": {"dataStores": {store: {
        "rowSet": {"primary": [], "filter": [], "delete": []}, "name": store,
        "pageNumber": 1, "pageSize": page_size, "recordCount": 0,
        "rowSetName": entity, "parameters": sp, "condition": cond}},
        "parameters": {"pojo.pagename": page, "pojo.modelname": modelpath,
                       "pojo.dstype": "POJODS", "pojo.interaction": interaction,
                       "synCount": "false"}}}
    return post(APP + "/ria_pojo.do?method=action",
                json.dumps(dc, ensure_ascii=False, separators=(",", ":")))


def proc(pro_name: str, para_in: list, para_out: list = None) -> tuple:
    dc = {"header": {"ajaxRequest": True}, "body": {"dataStores": {}, "parameters": {
        "pojo.interaction": "REQ-UTIL-procedureService-Q", "synCount": "false",
        "proName": pro_name, "paraIn": para_in, "paraOutType": para_out or []}}}
    return post(APP + "/ria_pojo.do?method=action",
                json.dumps(dc, ensure_ascii=False, separators=(",", ":")))


if __name__ == "__main__":
    cmd = sys.argv[1]
    if cmd == "login":
        if len(sys.argv) < 4:
            raise SystemExit("用法: replay2.py login <用户> <口令> (凭据不入库)")
        login(sys.argv[2], sys.argv[3])
    elif cmd == "query":
        # query <modelpath> <store> <交互id> <实体FQN> <condition> [pageSize]
        st, txt = query(sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[6],
                        int(sys.argv[7]) if len(sys.argv) > 7 else 10)
        print(st)
        print(txt)
    elif cmd == "proc":
        st, txt = proc(sys.argv[2], json.loads(sys.argv[3]))
        print(st)
        print(txt)
