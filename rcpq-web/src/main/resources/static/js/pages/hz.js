/* 社保报增回盘(M2, 仿东软 tdck): 名册由合同推导推送 TC9W → 报增查询 → 成果确认
   (报增成功=写 TC9W_BP aaa90w='1'/报增失败异议='2') → 延续至下月/审核通过(复用
   PKG_保险人员增减 忠实行为) → 成功校验/回盘提交(/proc 已移植过程直调)。
   旧"报增名册导入"为生产 INVALID 死包不移植; 局方回盘文件导入留 M5(TC9W_HZ)。 */
(() => {
  const { reactive, ref, onMounted } = Vue;
  const { api } = WB;
  const { ElMessageBox } = ElementPlus;

  const rp = reactive({ ym: "202611", kind: "social", aab001: null, aaa030: null,
    aaa020: "", log: "", busy: false });
  const isMed = () => rp.kind === "medical";
  const procName = (base) => isMed()
    ? "PKG_保险人员增减.PRC_市医保" + base
    : "PKG_保险人员增减.PRC_社保异动回盘" + base;
  const kindQS = () => "&kind=" + rp.kind;
  const rows = ref([]); const heads = ref([]); const units = ref([]);
  const bpRows = ref([]); const bpLog = ref("");
  const bjRows = ref([]); const bjLog = ref("");
  const dlg = reactive({ show: false, atc9w0: null, name: "", abp001: "", abp004: "", aaa001: "" });

  onMounted(async () => {
    try { units.value = (await api.wb("/units")) || []; } catch (e) { /* 保底 */ }
  });

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
      bpRows.value = (await api.wb(`/hz/bp?ym=${rp.ym}${kindQS()}&aaa030=${rp.aaa030}&aaa020=${encodeURIComponent(rp.aaa020)}`)) || [];
      bpLog.value = `回盘批次(${rp.ym} 户头${rp.aaa030}): 共 ${bpRows.value.length} 行(成功 ${bpRows.value.filter(r => r.aaa90w === "1").length}/异议 ${bpRows.value.filter(r => r.aaa90w === "2").length})`;
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
      await ElMessageBox.confirm("回盘提交将按 TC9W_BP 成果更新合同社保状态(参保成功/异议/停保), 确认?", "回盘提交");
      const r = await api.proc(procName("提交"),
        { aae002: rp.ym, aaa030: rp.aaa030, aaa020: rp.aaa020, aae011: api.store.oper.aae011 });
      bpLog.value = `回盘提交完成: 停保 ${r.stopApplied ?? 0} / 参保成功 ${r.successApplied ?? 0} / 异议 ${r.objectionApplied ?? 0}`;
      loadRoster();
    } catch (e) { if (e !== "cancel") bpLog.value = "提交失败: " + e.message; }
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
          <el-select v-model="rp.kind" style="width:110px" @change="rows = []; heads = []; bpRows = []">
            <el-option value="social" label="社保"/>
            <el-option value="medical" label="市医保"/>
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
      </el-form>
      <div v-if="rp.log" class="kv">{{ rp.log }}</div>

      <h4>名册工作表(TC9W, 报增)</h4>
      <div style="margin-bottom:6px">
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
            <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
              area="成果确认" btn="报增成功(申报结果导入的单行等价)" note="写 TC9W_BP 参保成功(实际参保时间+个人编号), 回盘提交后更新合同">
              <el-button size="small" type="success" link @click="openOk(row)">报增成功</el-button>
            </wb-dsf-tip>
            <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
              area="成果确认" btn="报增失败导入" note="登记异议(aaa90w='2'+原因), 回盘提交后转参保异议">
              <el-button size="small" type="danger" link @click="confirmNo(row)">报增失败</el-button>
            </wb-dsf-tip>
          </template>
        </el-table-column>
      </el-table>

      <h4>回盘批次(TC9W_BP) — 成功校验 → 回盘提交</h4>
      <div style="margin-bottom:6px">
        <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
          area="成果确认(回盘批次 tc9w_bpStore)" btn="批次查询" note="新系统按年月/户头/属地查 TC9W_BP">
          <el-button size="small" @click="loadBp">查询批次</el-button>
        </wb-dsf-tip>
        <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
          area="成果确认" btn="报增成功校验" note="已移植过程 PRC_社保异动回盘校验(清洗+逐行校验, 待审置已校验)">
          <el-button size="small" type="primary" @click="check">成功校验</el-button>
        </wb-dsf-tip>
        <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
          area="回盘" btn="回盘提交" note="已移植过程 PRC_社保异动回盘提交(停保/参保成功/异议三路更新 TC90+盖章)">
          <el-button size="small" type="danger" @click="commit">回盘提交</el-button>
        </wb-dsf-tip>
        <span class="kv">对应东软 tdck 成果确认: 校验/提交为已移植过程 PRC_社保异动回盘校验/提交。</span>
      </div>
      <div v-if="bpLog" class="kv">{{ bpLog }}</div>
      <el-table v-if="bpRows.length" :data="bpRows" size="small" border max-height="260">
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

      <h4>补缴明细(TC9W_BP05) — 参保成功且 缴费截止年月&lt;当月≤工资截止年月 的合同按年算补</h4>
      <div style="margin-bottom:6px">
        <wb-dsf-tip menu="员工异动/补退费管理(社保)" page="td/c/k 社保异动回盘/补退费"
          area="补缴明细" btn="补缴明细查询" note="已移植过程 PRC_社保补缴明细(重建 TC9W_BP05, 险金引擎逐年分组计算)">
          <el-button size="small" type="primary" @click="loadBj">重建并查询补缴明细</el-button>
        </wb-dsf-tip>
        <span class="kv">对应东软 tdck 补缴明细(PRC_社保补缴明细, 险金引擎逐年分组计算)。</span>
      </div>
      <div v-if="bjLog" class="kv">{{ bjLog }}</div>
      <el-table v-if="bjRows.length" :data="bjRows" size="small" border max-height="260">
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
          <el-form-item label="实际参保时间">
            <el-date-picker v-model="dlg.abp001" type="date" value-format="YYYY-MM-DD" style="width:180px"/>
          </el-form-item>
          <el-form-item label="个人编号">
            <el-input v-model="dlg.aaa001" placeholder="局端分配的个人编号" style="width:180px"/>
          </el-form-item>
          <el-form-item v-if="rp.kind === 'medical'" label="申报基数">
            <el-input v-model="dlg.abp004" placeholder="空=合同基数" style="width:180px"/>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dlg.show = false">取消</el-button>
          <el-button type="primary" @click="saveOk">登记</el-button>
        </template>
      </el-dialog>
    </div>`,
    setup() {
      return { rp, rows, heads, units, bpRows, bpLog, dlg, selRows, bjRows, bjLog,
        isMed, push,
        loadHeads, loadRoster, openOk, saveOk, confirmNo, nextMonth, pass, loadBp, check,
        commit, loadBj, stTag };
    },
  };
})();
