/* 社保报增回盘(M2, 仿东软 tdck): 名册由合同推导推送 TC9W → 报增查询 → 成果确认
   (报增成功=写 TC9W_BP aaa90w='1'/报增失败异议='2') → 延续至下月/审核通过(复用
   PKG_保险人员增减 忠实行为) → 成功校验/回盘提交(/proc 已移植过程直调)。
   旧"报增名册导入"为生产 INVALID 死包不移植; 局方回盘文件导入留 M5(TC9W_HZ)。 */
(() => {
  const { reactive, ref, onMounted } = Vue;
  const { api } = WB;
  const { ElMessageBox } = ElementPlus;

  const rp = reactive({ ym: "202611", kind: "social", aab001: null, aaa030: null,
    aaa020: "", bucket: "hx", log: "", busy: false });
  const isMed = () => rp.kind === "medical";
  const isHf = () => rp.kind === "housingfund";
  const isSm = () => rp.kind === "smedical";
  const isGs = () => rp.kind === "sgongshang";
  const isMg = () => rp.kind === "mgongshang";
  const procName = (base) => isMg()
    ? "PKG_保险人员增减.PRC_市工伤异动回盘" + base
    : isGs()
    ? "PKG_保险人员增减.PRC_省工伤异动回盘" + base
    : isSm()
    ? "PKG_保险人员增减.PRC_省医保异动回盘" + base
    : isHf()
    ? "PKG_保险人员增减.PRC_公积金异动回盘" + base
    : isMed()
    ? "PKG_保险人员增减.PRC_市医保" + base
    : "PKG_保险人员增减.PRC_社保异动回盘" + base;
  const kindQS = () => "&kind=" + rp.kind;
  const rows = ref([]); const heads = ref([]); const units = ref([]);
  const bpRows = ref([]); const bpLog = ref("");
  const bjRows = ref([]); const bjLog = ref("");
  const dlg = reactive({ show: false, atc9w0: null, name: "", abp001: "", abp004: "", aaa001: "" });

  async function loadUnits() {
    try { units.value = (await api.wb("/units")) || []; } catch (e) { /* 保底 */ }
  }

  async function push() {
    rp.busy = true; rp.log = "";
    try {
      const r = await api.wb("/hz/push", { ym: rp.ym, kind: rp.kind,
        aab001: rp.aab001 || null, aae011: api.store.oper.aae011 });
      rp.log = r.msg || `已推送 ${r.pushed} 人`;
      if (r.skipped && r.skipped.length) rp.log += " 跳过: " + r.skipped.slice(0, 5).join(", ");
      await loadHeads();
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
    rp.busy = false;
  }

  async function loadHeads() {
    try { heads.value = (await api.wb("/hz/heads?ym=" + rp.ym + kindQS())) || []; }
    catch (e) { heads.value = []; }
  }

  async function loadRoster() {
    if (!rp.ym) { ElementPlus.ElMessage.warning("选年月"); return; }
    rp.busy = true; rp.log = "";
    try {
      const q = `ym=${rp.ym}${kindQS()}` + (rp.aaa030 ? `&aaa030=${rp.aaa030}` : "") +
        (rp.aaa020 ? `&aaa020=${encodeURIComponent(rp.aaa020)}` : "");
      rows.value = (await api.wb("/hz/roster?" + q)) || [];
      rp.log = `报增名册 ${rp.ym}: 共 ${rows.value.length} 人`;
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
    rp.busy = false;
  }

  function openOk(row) {
    dlg.atc9w0 = row.atc9w0; dlg.name = row.aac003; dlg.abp001 = api.YMD; dlg.aaa001 = ""; dlg.abp004 = "";
    dlg.show = true;
  }
  async function saveOk() {
    try {
      const r = await api.wb("/hz/confirm", { atc9w0: dlg.atc9w0, ok: true, kind: rp.kind,
        abp001: dlg.abp001, abp004: dlg.abp004, aaa001: dlg.aaa001, aae011: api.store.oper.aae011 });
      dlg.show = false; rp.log = r.msg; loadRoster();
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
  }
  async function confirmNo(row) {
    try {
      const v = await ElMessageBox.prompt(`报增失败原因(【${row.aac003}】)`, "报增失败(异议)",
        { inputValue: "报增失败" });
      const r = await api.wb("/hz/confirm", { atc9w0: row.atc9w0, ok: false, kind: rp.kind,
        aae013: v.value, aae011: api.store.oper.aae011 });
      rp.log = r.msg; loadRoster();
    } catch (e) { if (e !== "cancel" && e?.message) ElementPlus.ElMessage.error(e.message); }
  }

  const selRows = ref([]);
  function sel() { return selRows.value; }
  const fileInput = ref(null);
  function pickFile() { fileInput.value.click(); }
  /* M5: 局方申报结果(回盘)文件导入 — 社保=社保异动回盘 9 列/市医保=个人缴费信息 17 列,
     按证件或报盘行序对齐名册写 TC9W_BP/BP04 待校验, 之后走成功校验→回盘提交 */
  async function uploadBp(ev) {
    const f = ev.target.files[0]; if (!f) return;
    try {
      const fd = new FormData();
      fd.append("file", f);
      const r = await fetch(`/wb/hz/import-bp?ym=${rp.ym}&kind=${rp.kind}&aae011=${api.store.oper.aae011}`,
        { method: "POST", body: fd });
      const j = await r.json();
      if (j.appCode !== 1) { ElementPlus.ElMessage.error(j.errMsg || "导入失败"); }
      else {
        rp.log = j.msg + (j.rejects && j.rejects.length ? " 例: " + j.rejects.slice(0, 3).join("; ") : "");
        loadBp(); loadRoster();
      }
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
    ev.target.value = "";
  }
  async function nextMonth() {
    const s = sel(); if (!s.length) { ElementPlus.ElMessage.warning("请勾选名册行"); return; }
    try {
      const r = await api.wb("/hz/next", { atc900: s.map(x => x.atc900) });
      rp.log = r.msg || "已延续"; loadRoster();
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
  }
  async function pass() {
    const s = sel(); if (!s.length) { ElementPlus.ElMessage.warning("请勾选名册行"); return; }
    try {
      const r = await api.wb("/hz/pass", { atc900: s.map(x => x.atc900),
        atc9w0: s.map(x => x.atc9w0) });
      rp.log = r.msg || "已审核通过"; loadRoster();
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
  }

  async function loadBp() {
    if (!rp.aaa030 || !rp.aaa020) { ElementPlus.ElMessage.warning("先选户头与参保属地"); return; }
    try {
      const bq = isHf() ? `&bucket=${rp.bucket}` : "";
      bpRows.value = (await api.wb(`/hz/bp?ym=${rp.ym}${kindQS()}${bq}&aaa030=${rp.aaa030}&aaa020=${encodeURIComponent(rp.aaa020)}`)) || [];
      const s = bpRows.value.filter(r => r.aaa90w === "1").length;
      const y = bpRows.value.filter(r => r.aaa90w === "2").length;
      bpLog.value = `回盘批次(${rp.ym} 户头${rp.aaa030}${isHf() ? " 桶" + rp.bucket : ""}): 共 ${bpRows.value.length} 行` +
        (isHf() ? "" : `(成功 ${s}/异议 ${y})`);
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
  }
  async function check() {
    try {
      const r = await api.proc(procName("校验"),
        { aae002: rp.ym, aaa030: rp.aaa030, aaa020: rp.aaa020 });
      bpLog.value = `成功校验: 通过 ${r.pass ?? "-"} / 拒绝 ${r.reject ?? "-"}`;
    } catch (e) { bpLog.value = "校验失败: " + e.message; }
  }
  async function commit() {
    try {
      await ElMessageBox.confirm("回盘提交将按回盘桶成果更新合同状态(参保成功/异议/停保), 确认?", "回盘提交");
      const r = await api.proc(procName("提交"),
        { aae002: rp.ym, aaa030: rp.aaa030, aaa020: rp.aaa020, aae011: api.store.oper.aae011 });
      bpLog.value = (isHf()
        ? `公积金回盘提交完成: 参保成功 ${r.cbApplied ?? 0} / 停保 ${r.tzApplied ?? 0} / 名单终态置位 ${r.terminalApplied ?? 0}(W0#1)`
        : `回盘提交完成: 停保 ${r.stopApplied ?? 0} / 参保成功 ${r.successApplied ?? 0} / 异议 ${r.objectionApplied ?? 0}`);
      loadRoster();
    } catch (e) { if (e !== "cancel") bpLog.value = "提交失败: " + e.message; }
  }
  /* W1 公积金封存/异议回盘(TC9U_BP02/BP03): 校验/提交四键, 东软 tdcae 弹窗 */
  async function procPair(label, name, body, fmt) {
    try {
      const r = await api.proc("PKG_保险人员增减.PRC_公积金" + name, body);
      bpLog.value = label + ": " + fmt(r);
    } catch (e) { bpLog.value = label + "失败: " + e.message; }
  }
  const gjBody = () => ({ aae002: rp.ym, aaa030: rp.aaa030, aaa020: rp.aaa020 });
  const fcCheck = () => procPair("封存校验", "封存回盘校验", gjBody(), r => `清洗 ${r.cleaned ?? "-"} 行(忠实: 逐行校验结果回写旧系统已注释)`);
  const fcCommit = () => procPair("封存提交", "封存回盘提交", gjBody(), r => `开户落账 ${r.applied ?? 0} / 跳过 ${r.skipped ?? 0}`);
  const yyCheck = () => procPair("异议校验", "异议回盘校验", gjBody(), r => `通过 ${r.pass ?? "-"} / 拒绝 ${r.reject ?? "-"}`);
  const yyCommit = () => procPair("异议提交", "异议回盘提交",
    { ...gjBody(), aae011: api.store.oper.aae011 }, r => `启封 ${r.hfApplied ?? 0} / 异议 ${r.yyApplied ?? 0}`);
  /* W2 省医保: 基数补差回盘(按 ace100 批次, tdcbm) + 省医保工资表(TC9W_BP07 重建, 调险金引擎) */
  async function bjCheck() {
    try {
      const v = await ElMessageBox.prompt("基数补差回盘批次号(ace100)", "补差校验", { inputValue: "" });
      const r = await api.proc("PKG_保险人员增减.PRC_省医保基数补差回盘校验", { ace100: v.value });
      bpLog.value = `补差校验(批次 ${v.value}): 通过 ${r.pass ?? "-"} / 拒绝 ${r.reject ?? "-"}`;
    } catch (e) { if (e !== "cancel") bpLog.value = "补差校验失败: " + e.message; }
  }
  async function bjCommit() {
    try {
      const v = await ElMessageBox.prompt("基数补差回盘批次号(ace100)", "补差提交", { inputValue: "" });
      await ElMessageBox.confirm("补差提交将重建 TJ0B2 台账(先物理删旧账, 旧系统同款), 确认?", "补差提交");
      const r = await api.proc("PKG_保险人员增减.PRC_省医保基数补差回盘提交",
        { ace100: v.value, aae002: rp.ym, aaa030: rp.aaa030, aaa020: rp.aaa020, aae011: api.store.oper.aae011 });
      bpLog.value = `补差提交(批次 ${v.value}): 落账 ${r.applied ?? 0} 行`;
    } catch (e) { if (e !== "cancel") bpLog.value = "补差提交失败: " + e.message; }
  }
  /* W2/W6 补差确认(tdcbm:101/134 · tdcbs:101/134): TJ0B2/TJ0B3.AAEZZZ 置 1/0+经办件套 */
  async function buchaQueRen(xz, flag) {
    try {
      await ElMessageBox.confirm(
        `确认${flag === "1" ? "" : "取消"}基数补差台账(${xz === "2" ? "TJ0B2 医保" : "TJ0B3 工伤"}, AAEZZZ=${flag === "1" ? 1 : 0})?`, "补差确认");
      await api.proc("PKG_保险人员增减.PRC_保险补差确认",
        { xz, flag, aae002: rp.ym, aaa030: rp.aaa030, aaa020: rp.aaa020, aae011: api.store.oper.aae011 });
      bpLog.value = `补差确认(${xz === "2" ? "医保" : "工伤"}): AAEZZZ=${flag === "1" ? 1 : 0}`;
    } catch (e) { if (e !== "cancel") bpLog.value = "补差确认失败: " + e.message; }
  }
  /* W3a 省工伤: 减免(TC09_BP→TC09) / 补差(TC9W_BP08→TJ0B3) */
  const gsBatch = () => ElMessageBox.prompt("批次号(ace100)", "批次", { inputValue: "" });
  const gsBody = (ace) => ({ ace100: ace, aae002: rp.ym, aaa030: rp.aaa030, aaa020: rp.aaa020, aae011: api.store.oper.aae011 });
  async function gsJmCheck() {
    try { const v = await gsBatch();
      const r = await api.proc("PKG_保险人员增减.PRC_省工伤减免校验", { ace100: v.value });
      bpLog.value = `减免校验(批次 ${v.value}): 通过 ${r.pass ?? "-"} / 拒绝 ${r.reject ?? "-"}`;
    } catch (e) { if (e !== "cancel") bpLog.value = "减免校验失败: " + e.message; }
  }
  async function gsJmCommit() {
    try { const v = await gsBatch();
      await ElMessageBox.confirm("减免提交将重建 TC09 台账(先物理删旧账, 旧系统同款), 确认?", "减免提交");
      const r = await api.proc("PKG_保险人员增减.PRC_省工伤减免提交", gsBody(v.value));
      bpLog.value = `减免提交(批次 ${v.value}): 落账 ${r.applied ?? 0} 行`;
    } catch (e) { if (e !== "cancel") bpLog.value = "减免提交失败: " + e.message; }
  }
  async function gsBcCheck() {
    try { const v = await gsBatch();
      const r = await api.proc("PKG_保险人员增减.PRC_省工伤基数补差回盘校验", { ace100: v.value });
      bpLog.value = `补差校验(批次 ${v.value}): 通过 ${r.pass ?? "-"}(缺月仍置过=忠实)`;
    } catch (e) { if (e !== "cancel") bpLog.value = "补差校验失败: " + e.message; }
  }
  async function gsBcCommit() {
    try { const v = await gsBatch();
      const r = await api.proc("PKG_保险人员增减.PRC_省工伤基数补差回盘提交", gsBody(v.value));
      bpLog.value = `补差提交(批次 ${v.value}): 落账 ${r.applied ?? 0} 行(TJ0B3 逐月摊分)`;
    } catch (e) { if (e !== "cancel") bpLog.value = "补差提交失败: " + e.message; }
  }
  async function gzb() {
    if (!rp.aaa030 || !rp.aaa020) { ElementPlus.ElMessage.warning("先选户头与参保属地"); return; }
    try {
      const r = await api.proc("PKG_保险人员增减.PRC_省医保工资表",
        { aaa030: rp.aaa030, aae002: rp.ym, aaa110: rp.aaa020, aae011: api.store.oper.aae011 });
      bpLog.value = `省医保工资表: 生成 ${r.rows ?? 0} 行(TC9W_BP07, 险金引擎计算)`;
      rp.bucket = "gz"; loadBp();
    } catch (e) { bpLog.value = "工资表生成失败: " + e.message; }
  }

  const stTag = (s) => ({ "1": "待校验", "4": "已校验" }[s] || (s || "-"));

  /* 补缴明细(M3): 调已移植过程重建 TC9W_BP05 再查询 — 参保成功且缴费截止年月<当月
     且工资截止年月=当月 的合同, 按年分组算单位/个人应补(险金引擎) */
  async function loadBj() {
    if (!rp.aaa030 || !rp.aaa020) { ElementPlus.ElMessage.warning("先选户头与参保属地"); return; }
    try {
      const r = await api.proc("PKG_保险人员增减.PRC_社保补缴明细",
        { aae002: rp.ym, aaa030: rp.aaa030, aaa020: rp.aaa020 });
      bjRows.value = (await api.wb(`/hz/bj?ym=${rp.ym}&aaa030=${rp.aaa030}&aaa020=${encodeURIComponent(rp.aaa020)}`)) || [];
      bjLog.value = `补缴明细重建: 合同 ${r.contracts ?? "-"} 个, 明细 ${r.rows ?? bjRows.value.length} 行`;
    } catch (e) { bjLog.value = "补缴明细失败: " + e.message; }
  }

  WB.pages.hz = {
    template: `
    <div class="page">
      <el-form inline>
        <el-form-item label="险种">
          <el-select v-model="rp.kind" style="width:110px" @change="rows = []; heads = []; bpRows = []; rp.bucket = 'hx'">
            <el-option value="social" label="社保"/>
            <el-option value="medical" label="市医保"/>
            <el-option value="smedical" label="省医保"/>
            <el-option value="sgongshang" label="省工伤"/>
            <el-option value="mgongshang" label="市工伤"/>
            <el-option value="housingfund" label="公积金"/>
          </el-select>
        </el-form-item>
        <el-form-item label="年月">
          <el-date-picker v-model="rp.ym" type="month" value-format="YYYYMM"
                          placeholder="202611" style="width:120px"/>
        </el-form-item>
        <el-form-item label="单位(可空)">
          <el-select v-model="rp.aab001" filterable clearable placeholder="全部单位" style="width:240px">
            <el-option v-for="u in units" :key="u.aab001" :value="u.aab001"
                       :label="u.aab001 + ' ' + u.aab004"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="报增名册导入(CHECK_TC9W_IMP)/tdb/p 合同新增导入"
            area="名册导入" btn="导入" note="旧报增名册导入为生产即 INVALID 死包未移植; 新系统由合同推导推送 TC9W(等效替代)">
            <el-button type="primary" :loading="rp.busy" @click="push">生成名册并推送</el-button>
          </wb-dsf-tip>
          <wb-dsf-tip menu="—" page="—" area="—" btn="—" note="新系统辅助按键, 东软无对应">
            <el-button @click="loadHeads">刷新户头</el-button>
          </wb-dsf-tip>
          <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费(社保异动结果)"
            area="报增查询条件" btn="查询(doQueryBz)">
            <el-button @click="loadRoster">报增查询</el-button>
          </wb-dsf-tip>
        </el-form-item>
        <el-form-item label="户头">
          <el-select v-model="rp.aaa030" clearable placeholder="户头" style="width:200px" @change="bpRows = []">
            <el-option v-for="h in heads" :key="h.aaa030 + '-' + h.aaa100" :value="h.aaa030"
                       :label="h.aaa030 + ' ' + (h.aaa031 || '')"/>
          </el-select>
        </el-form-item>
        <el-form-item label="参保属地">
          <el-select v-model="rp.aaa020" clearable placeholder="属地" style="width:200px">
            <el-option v-for="h in heads.filter(x => !rp.aaa030 || x.aaa030 === rp.aaa030)"
                       :key="h.aaa100" :value="h.aaa100" :label="(h.aaa101 || '') + ' ' + h.aaa100"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isHf()">
          <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
            area="申报结果导入弹窗" btn="导入" note="局方回盘 .xls: 社保=社保异动回盘 9 列/市医保=个人缴费信息 17 列(样本见 baopan/huipan); 按证件或报盘行序对齐名册, 写 TC9W_BP/BP04 待校验">
            <el-button size="small" type="warning" @click="pickFile">导入回盘文件</el-button>
          </wb-dsf-tip>
          <input ref="fileInput" type="file" accept=".xls,.xlsx" style="display:none" @change="uploadBp"/>
        </el-form-item>
        <el-form-item v-if="isHf()" label="回盘桶">
          <el-select v-model="rp.bucket" style="width:130px" @change="bpRows = []">
            <el-option value="hx" label="异动(TC9W_BP02)"/>
            <el-option value="fc" label="封存(TC9U_BP02)"/>
            <el-option value="yy" label="异议(TC9U_BP03)"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="isGs()" label="回盘桶">
          <el-select v-model="rp.bucket" style="width:140px" @change="bpRows = []">
            <el-option value="hx" label="异动(TC9W_BP09)"/>
            <el-option value="bj" label="补差(TC9W_BP08)"/>
            <el-option value="jm" label="减免(TC09_BP)"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="isSm()" label="回盘桶">
          <el-select v-model="rp.bucket" style="width:140px" @change="bpRows = []">
            <el-option value="hx" label="异动(TC9W_BP01)"/>
            <el-option value="bj" label="补差(TC9W_BP06)"/>
            <el-option value="gz" label="工资表(TC9W_BP07)"/>
          </el-select>
        </el-form-item>
      </el-form>
      <div v-if="rp.log" class="kv">{{ rp.log }}</div>

      <h4>名册工作表({{ isHf() ? "TC9U 公积金" : "TC9W 社保/医保" }}, 报增)</h4>
      <div style="margin-bottom:6px" v-if="!isHf()">
        <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
          area="报增结果列表" btn="审核(doPass)" note="tc90.atc90p='0'; TC9W 更新沿用旧系统报错行为">
          <el-button size="small" type="success" @click="pass">审核通过(勾选)</el-button>
        </wb-dsf-tip>
        <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
          area="报增结果列表" btn="延续至下月(doNextBz)" note="tc90.aaa201 社保参保年月+1; TC9W 更新沿用旧系统报错行为">
          <el-button size="small" type="warning" @click="nextMonth">延续至下月(勾选)</el-button>
        </wb-dsf-tip>
        <span class="kv">报增失败=局方反馈未通过, 登记异议后随回盘提交; 延续至下月=社保参保年月+1(与旧系统同: TC90 延续生效, TC9W 更新按旧报错)。</span>
      </div>
      <el-table :data="rows" size="small" border max-height="380"
                @selection-change="s => selRows = s">
        <el-table-column type="selection" width="36"/>
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aac002" label="身份证号码" width="170"/>
        <el-table-column prop="aab004" label="单位" min-width="160" show-overflow-tooltip/>
        <el-table-column label="户头" width="70"><template #default="{row}">{{ row.aaa030 }}</template></el-table-column>
        <el-table-column label="参保属地" width="140"><template #default="{row}">{{ row.aaa101 || row.aaa100 }}</template></el-table-column>
        <el-table-column prop="aac001" label="个人编号" width="90"/>
        <el-table-column label="状态" width="80"><template #default="{row}">
          <el-tag size="small" :type="row.atc9w8 === '4' ? 'success' : 'info'">{{ stTag(row.atc9w8) }}</el-tag>
        </template></el-table-column>
        <el-table-column label="成果" width="90">
          <template #default="{row}">
            <el-tag v-if="row.confirmed_ok" size="small" type="success">报增成功</el-tag>
            <el-tag v-else-if="row.confirmed_no" size="small" type="danger">异议</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成果确认" width="150">
          <template #default="{row}">
            <wb-dsf-tip :menu="isHf() ? '员工异动/补退费管理(公积金)' : '员工异动/补退费管理(社保)'"
              :page="isHf() ? 'td/c/ae 员工公积金报增申请(15)' : 'td/c/k 社保异动回盘/补退费'"
              area="成果确认" :btn="isHf() ? '登记回盘结果(公积金回盘文件未到位 W0#4 前的手动通道)' : '报增成功(申报结果导入的单行等价)'"
              :note="isHf() ? '写 TC9W_BP02 正常行(基数/比例缺省=名册值), 异动回盘校验→提交后更新 TC90.ATC90W/AAA004 + W0#1 终态置位; 公积金异议走异议回盘桶' : '写 TC9W_BP 参保成功(实际参保时间+个人编号), 回盘提交后更新合同'">
              <el-button size="small" type="success" link @click="openOk(row)">{{ isHf() ? "登记回盘结果" : "报增成功" }}</el-button>
            </wb-dsf-tip>
            <wb-dsf-tip v-if="!isHf()" menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
              area="成果确认" btn="报增失败导入" note="登记异议(aaa90w='2'+原因), 回盘提交后转参保异议">
              <el-button size="small" type="danger" link @click="confirmNo(row)">报增失败</el-button>
            </wb-dsf-tip>
          </template>
        </el-table-column>
      </el-table>

      <h4>回盘批次({{ isHf() ? "公积金三桶 TC9W_BP02/TC9U_BP02/TC9U_BP03" : "TC9W_BP" }}) — 校验 → 提交</h4>
      <div style="margin-bottom:6px">
        <wb-dsf-tip :menu="isHf() ? '员工异动/补退费管理(公积金)' : '员工异动/补退费管理(社保)'"
          :page="isHf() ? 'td/c/ae 员工公积金报增申请(15)' : 'td/c/k 社保异动回盘/补退费'"
          area="成果确认(回盘批次)" btn="批次查询" note="新系统按年月/户头/属地(公积金封存桶按 aaa100)查回盘桶">
          <el-button size="small" @click="loadBp">查询批次</el-button>
        </wb-dsf-tip>
        <wb-dsf-tip :menu="isHf() ? '员工异动/补退费管理(公积金)' : '员工异动/补退费管理(社保)'"
          :page="isHf() ? 'td/c/ae 员工公积金报增申请(15)' : 'td/c/k 社保异动回盘/补退费'"
          area="成果确认" :btn="isHf() ? '异动回盘校验' : '报增成功校验'"
          :note="isHf() ? '已移植过程 PRC_公积金异动回盘校验(清洗+匹配+编号/比例校验; W0#1: 旧校验尾置终态被注释→移至提交后)' : '已移植过程 PRC_社保异动回盘校验(清洗+逐行校验, 待审置已校验)'">
          <el-button size="small" type="primary" @click="check">{{ isHf() ? "异动校验" : "成功校验" }}</el-button>
        </wb-dsf-tip>
        <wb-dsf-tip :menu="isHf() ? '员工异动/补退费管理(公积金)' : '员工异动/补退费管理(社保)'"
          :page="isHf() ? 'td/c/ae 员工公积金报增申请(15)' : 'td/c/k 社保异动回盘/补退费'"
          area="回盘" :btn="isHf() ? '异动回盘提交' : '回盘提交'"
          :note="isHf() ? '已移植过程 PRC_公积金异动回盘提交(参保成功回写 TC90.ATC90W/AAA004/比例/基数+停保+TC90_YY; W0#1 裁决: 恢复 TC9U.ATC9U8 整批置4)' : '已移植过程 PRC_社保异动回盘提交(停保/参保成功/异议三路更新 TC90+盖章)'">
          <el-button size="small" type="danger" @click="commit">回盘提交</el-button>
        </wb-dsf-tip>
        <template v-if="isHf()">
          <wb-dsf-tip menu="员工异动/补退费管理(公积金)" page="td/c/ae 员工公积金报增申请(15)"
            area="封存回盘弹窗(tdcae:243/268)" btn="封存回盘校验/提交"
            note="封存校验净效果=文本清洗(旧系统逐行结果回写被注释, 忠实); 封存提交回写 TC90.AAA004(ATC90W='2'时同时清) + 桶行置'新开户'">
            <el-button size="small" type="primary" plain @click="fcCheck">封存校验</el-button>
            <el-button size="small" type="danger" plain @click="fcCommit">封存提交</el-button>
          </wb-dsf-tip>
          <wb-dsf-tip menu="员工异动/补退费管理(公积金)" page="td/c/ae 员工公积金报增申请(15)"
            area="异议回盘弹窗(tdcae:322-391)" btn="异议回盘校验/提交"
            note="异议校验: ABP003 含'100066835'+'状态封存'→启封, 否则异议; 提交: 启封置 TC9U.ATC9U1='2'/ATC9UY='2', 异议走参保异议赋值(SAFESIGN='4')+TC90.ATC90W='2'">
            <el-button size="small" type="primary" plain @click="yyCheck">异议校验</el-button>
            <el-button size="small" type="danger" plain @click="yyCommit">异议提交</el-button>
          </wb-dsf-tip>
        </template>
        <template v-if="isSm()">
          <wb-dsf-tip menu="员工异动/补退费管理(市医保)" page="td/c/bm 医保基数调整补差回盘(10, 省工伤 tdcbs 复用)"
            area="基数补差回盘弹窗" btn="校验/提交"
            note="已移植过程 PRC_省医保基数补差回盘校验/提交(按 AAA005 医保个人编号匹配+当月 TC93 校验; 提交重建 TJ0B2 台账, 先物理删旧账忠实)">
            <el-button size="small" type="primary" plain @click="bjCheck">补差校验</el-button>
            <el-button size="small" type="danger" plain @click="bjCommit">补差提交</el-button>
            <el-button size="small" type="success" plain @click="buchaQueRen('2', '1')">补差确认</el-button>
            <el-button size="small" plain @click="buchaQueRen('2', '0')">取消确认</el-button>
          </wb-dsf-tip>
          <wb-dsf-tip menu="员工异动/补退费管理(省医保)" page="td/c/bn 员工省医保报增申请(11):412/523"
            area="省医保缴费工资表" btn="生成"
            note="已移植过程 PRC_省医保工资表(重建 TC9W_BP07, 险金引擎计算五险扣款; 当月已回盘则拒绝)">
            <el-button size="small" type="warning" plain @click="gzb">工资表生成</el-button>
          </wb-dsf-tip>
        </template>
        <template v-if="isGs()">
          <wb-dsf-tip menu="员工异动/补退费管理(省工伤)" page="td/c/bt 省工伤减免回盘(27)"
            area="减免回盘弹窗" btn="校验/提交"
            note="已移植过程 PRC_省工伤减免校验/提交(按 AAA009 编号匹配+TC09 重建; 额度/合计零值拒)">
            <el-button size="small" type="primary" plain @click="gsJmCheck">减免校验</el-button>
            <el-button size="small" type="danger" plain @click="gsJmCommit">减免提交</el-button>
          </wb-dsf-tip>
          <wb-dsf-tip menu="员工异动/补退费管理(省工伤)" page="td/c/bs 省工伤基数调整补差回盘(26)"
            area="基数补差回盘弹窗" btn="校验/提交"
            note="已移植过程 PRC_省工伤基数补差回盘校验/提交(跨年所属期逐月摊分 TJ0B3; 缺月校验仍置'3'忠实; 旧 DELETE TJ0B3 被注释=累积模式)">
            <el-button size="small" type="primary" plain @click="gsBcCheck">补差校验</el-button>
            <el-button size="small" type="danger" plain @click="gsBcCommit">补差提交</el-button>
            <el-button size="small" type="success" plain @click="buchaQueRen('3', '1')">补差确认</el-button>
            <el-button size="small" plain @click="buchaQueRen('3', '0')">取消确认</el-button>
          </wb-dsf-tip>
        </template>
        <span class="kv">{{ isHf()
          ? "对应东软 tdcae 封存/异议回盘弹窗; 公积金回盘文件实样未到位(W0#4), 桶行由「登记回盘结果」手动写入。"
          : isSm()
          ? "对应东软 tdcah 省医保异动报盘回盘(13)/tdcbm 补差(10)/tdcbn 工资表(11); 回盘文件=baopan/huipan 省医保 7 列实样已支持导入。"
          : "对应东软 tdck 成果确认: 校验/提交为已移植过程 PRC_社保异动回盘校验/提交。" }}</span>
      </div>
      <div v-if="bpLog" class="kv">{{ bpLog }}</div>
      <el-table v-if="bpRows.length && isSm() && rp.bucket === 'bj'" :data="bpRows" size="small" border max-height="260">
        <el-table-column prop="id" label="行ID" width="90"/>
        <el-table-column prop="ace100" label="批次" width="90"/>
        <el-table-column prop="aaa005" label="医保个人编号" width="150"/>
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aae003" label="社保年月" width="90"/>
        <el-table-column prop="abp002" label="基数" width="90"/>
        <el-table-column prop="abp004" label="医疗单位缴" width="100"/>
        <el-table-column prop="abp003" label="医疗个人缴" width="100"/>
        <el-table-column prop="abp006" label="工伤单位缴" width="100"/>
        <el-table-column prop="abp007" label="生育单位缴" width="100"/>
        <el-table-column prop="abp008" label="合计" width="90"/>
        <el-table-column prop="ace10a" label="状态" width="80"/>
        <el-table-column prop="ace10b" label="说明" min-width="120"/>
      </el-table>
      <el-table v-if="bpRows.length && isSm() && rp.bucket === 'gz'" :data="bpRows" size="small" border max-height="260">
        <el-table-column prop="id" label="行ID" width="90"/>
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="abp001" label="基本工资" width="100"/>
        <el-table-column prop="abp002" label="应发工资" width="100"/>
        <el-table-column prop="abp005" label="医疗(个人)" width="100"/>
        <el-table-column prop="abp007" label="扣款合计" width="100"/>
        <el-table-column prop="abp008" label="实发工资" width="100"/>
      </el-table>
      <el-table v-if="bpRows.length && isHf()" :data="bpRows" size="small" border max-height="260">
        <el-table-column prop="id" label="行ID" width="90"/>
        <el-table-column prop="abp002" label="缴存状态/内容" width="120"/>
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aac002" label="证件/标识" width="170"/>
        <el-table-column prop="aaa001" label="个人编号" width="100"/>
        <el-table-column prop="abp003" label="基数/内容" width="110"/>
        <el-table-column prop="abp004" label="启封判定" width="90"/>
        <el-table-column prop="ace10a" label="校验/提交态" width="100"/>
        <el-table-column prop="ace10b" label="说明" min-width="120"/>
      </el-table>
      <el-table v-if="bpRows.length && !isHf()" :data="bpRows" size="small" border max-height="260">
        <el-table-column label="结果" width="80"><template #default="{row}">
          <el-tag size="small" :type="row.aaa90w === '1' ? 'success' : 'danger'">{{ row.aaa90w === '1' ? '参保成功' : '异议' }}</el-tag>
        </template></el-table-column>
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aac002" label="身份证号码" width="170"/>
        <el-table-column prop="aaa001" :label="rp.kind === 'medical' ? '申报基数' : '个人编号'" width="100"/>
        <el-table-column prop="abp001" :label="rp.kind === 'medical' ? '校验码串' : '实际参保时间'" width="110"/>
        <el-table-column prop="aae013" label="原因" min-width="140" show-overflow-tooltip/>
        <el-table-column prop="aab004" label="单位" min-width="150" show-overflow-tooltip/>
      </el-table>

      <div class="kv">流程=生成名册并推送(合同推导, 替代生产即 INVALID 的报增名册导入) →
        报增查询 → 成果确认(报增成功=登记实际参保时间+局端个人编号; 报增失败=登记异议) →
        成功校验 → 回盘提交(按 TC9W_BP 更新 TC90: 成功置 aaa203/atc90p='0', 异议转参保异议, 停保置 atc90p='1')。</div>

      <h4 v-if="!isHf()">补缴明细(TC9W_BP05) — 参保成功且 缴费截止年月&lt;当月≤工资截止年月 的合同按年算补</h4>
      <div style="margin-bottom:6px" v-if="!isHf()">
        <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
          area="补缴明细" btn="补缴明细查询" note="已移植过程 PRC_社保补缴明细(重建 TC9W_BP05, 险金引擎逐年分组计算)">
          <el-button size="small" type="primary" @click="loadBj">重建并查询补缴明细</el-button>
        </wb-dsf-tip>
        <span class="kv">对应东软 tdck 补缴明细(PRC_社保补缴明细, 险金引擎逐年分组计算)。</span>
      </div>
      <div v-if="bjLog && !isHf()" class="kv">{{ bjLog }}</div>
      <el-table v-if="bjRows.length && !isHf()" :data="bjRows" size="small" border max-height="260">
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aac002" label="身份证号码" width="170"/>
        <el-table-column prop="abp001" label="户口" width="70"/>
        <el-table-column prop="abp001_1" label="起止年月" width="110"/>
        <el-table-column prop="abp003" label="月数" width="60"/>
        <el-table-column prop="abp004" label="缴费基数" width="90"/>
        <el-table-column prop="abp005" label="单位应补(养老)" width="110"/>
        <el-table-column prop="abp006" label="单位应补(失业)" width="110"/>
        <el-table-column prop="abp007" label="个人应补(养老)" width="110"/>
        <el-table-column prop="abp008" label="个人应补(失业)" width="110"/>
        <el-table-column prop="abp00d" label="合计应补" width="100"/>
      </el-table>

      <el-dialog v-model="dlg.show" title="报增成功(成果确认)" width="380px">
        <el-form label-width="110px">
          <el-form-item label="姓名"><span>{{ dlg.name }}</span></el-form-item>
          <el-form-item v-if="!isHf()" label="实际参保时间">
            <el-date-picker v-model="dlg.abp001" type="date" value-format="YYYY-MM-DD" style="width:180px"/>
          </el-form-item>
          <el-form-item label="个人编号">
            <el-input v-model="dlg.aaa001" placeholder="局端分配的个人编号" style="width:180px"/>
          </el-form-item>
          <el-form-item v-if="rp.kind === 'medical'" label="申报基数">
            <el-input v-model="dlg.abp004" placeholder="空=合同基数" style="width:180px"/>
          </el-form-item>
          <el-form-item v-if="isHf()" label="公积金基数">
            <el-input v-model="dlg.abp004" placeholder="空=名册基数" style="width:180px"/>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dlg.show = false">取消</el-button>
          <el-button type="primary" @click="saveOk">登记</el-button>
        </template>
      </el-dialog>
    </div>`,
    setup() {
      loadUnits(); loadHeads();  // 顶层 onMounted 无实例=永不触发, 放 setup 内直调
      return { rp, rows, heads, units, bpRows, bpLog, dlg, selRows, bjRows, bjLog,
        isMed, isHf, fileInput, pickFile, uploadBp, push,
        loadHeads, loadRoster, openOk, saveOk, confirmNo, nextMonth, pass, loadBp, check,
        commit, loadBj, stTag, fcCheck, fcCommit, yyCheck, yyCommit, bjCheck, bjCommit, buchaQueRen, gzb, isSm, isGs, isMg, gsJmCheck, gsJmCommit, gsBcCheck, gsBcCommit };
    },
  };
})();
