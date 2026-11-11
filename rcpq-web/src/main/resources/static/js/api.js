/* 工作台共享基座: fetch 封装 / 全局状态 / 小工具。页面文件通过 window.WB 使用。 */
window.WB = window.WB || {};
WB.pages = WB.pages || {};
WB.ui = WB.ui || {};
(() => {
  const { reactive } = Vue;

  const DEFAULT_OPER = { account: "jiaq", aae011: "jiaq", aae017: "4101", aae019: "贾悫",
    aae020: "吉林省彩虹人才开发咨询服务有限公司", aae022: "410100000000" };
  const store = reactive({ oper: DEFAULT_OPER, opers: [DEFAULT_OPER] });

  async function post(path, body) {
    const r = await fetch(path, { method: "POST",
      headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
    if (!r.ok) throw new Error("HTTP " + r.status);
    return r.json();
  }
  async function get(path) {
    const r = await fetch(path);
    if (!r.ok) throw new Error("HTTP " + r.status);
    return r.json();
  }
  /** /proc 过程网关: appCode!=1 视为失败抛错(errMsg 透出), 成功返回完整结果 */
  async function proc(name, body) {
    const r = await post("/proc/" + encodeURIComponent(name), body);
    if (r.appCode !== 1) {
      const e = new Error(r.errMsg || ("appCode=" + r.appCode));
      e.result = r;
      throw e;
    }
    return r;
  }
  /** /wb 工作台端点: 不带 body 走 GET, 带 body 走 POST; path 以 / 开头(自动加 /wb 前缀) */
  function wb(path, body) { return body === undefined ? get("/wb" + path) : post("/wb" + path, body); }
  /** 交互网关行编辑保存(整行提交) */
  function interaction(code, body) { return post("/interaction/" + encodeURIComponent(code), body); }

  /** 码表缓存: codes('AAE00P') → Promise["1-派遣", ...] */
  const codeCache = {};
  function codes(category) {
    if (!codeCache[category]) {
      codeCache[category] = wb("/codes?category=" + encodeURIComponent(category))
        .then(rows => (rows || []).map(r => r.item))
        .catch(() => []);
    }
    return codeCache[category];
  }

  async function loadOpers() {
    try {
      const rows = await wb("/opers");
      if (rows && rows.length) store.opers = rows;
      if (!store.opers.some(o => o.aae011 === store.oper.aae011)) store.oper = store.opers[0];
    } catch (e) { /* 保底默认经办人 */ }
  }

  /** 审核人五要素(bae011/17/19/20/22), 行编辑保存时随行提交 */
  function operBae() {
    const o = store.oper;
    return { bae011: o.aae011, bae017: o.aae017, bae019: o.aae019, bae020: o.aae020, bae022: o.aae022 };
  }
  /** 经办人五要素(aae011/17/19/20/22), /proc 过程网关用 */
  function operAae() {
    const o = store.oper;
    return { aae011: o.aae011, aae017: o.aae017, aae019: o.aae019, aae020: o.aae020, aae022: o.aae022 };
  }

  /** 批次行 ace10a 状态: 3 过 / 2 拒(带原因 ace10b) / 1 完成(已提交) */
  const BATCH_LABEL = { "3": "成功", "2": "拒绝", "1": "完成" };
  const BATCH_TYPE = { "3": "success", "2": "danger", "1": "info" };
  function batchLabel(s) { return BATCH_LABEL[s] || s || "未校验"; }
  function batchType(s) { return BATCH_TYPE[s] || "info"; }
  const CONTRACT_STATE = { "2": "待审", "3": "生效" };
  function contractState(r) { return CONTRACT_STATE[r] || (r || "-"); }
  function contractStateType(r) { return r === "3" ? "success" : (r === "2" ? "warning" : "info"); }
  /** 本地时间 yyyy-MM-ddTHH:mm:ss(交互网关 bae036 审核时间用) */
  function nowLocal() {
    return new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 19);
  }

  WB.api = { store, post, get, proc, wb, interaction, codes, loadOpers, operBae, operAae,
    batchLabel, batchType, contractState, contractStateType, nowLocal,
    YMD: new Date().toISOString().slice(0, 10) };
})();
