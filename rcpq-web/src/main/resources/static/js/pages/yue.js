/* 月转款(W4c-2, 仿东软 tdcaz 等六页): 六险种月情况重建(/proc) → 汇总/明细查询
   (/wb/yue) → 月转款确认(/proc TABNAME 白名单)。六线: 社保 TJ0A / 省医保 TJ0B /
   市医保 TJ0C / 省公积金 TJ0D / 市公积金 TJ0E / 工伤 TJ0F。 */
(() => {
  const { reactive, ref, onMounted } = Vue;
  const { api } = WB;

  const LINES = [
    { key: "social", label: "社保", tab: "TJ0A", proc: "PRC_社保月情况",
      page: "td/c/az 社保月转款情况(4)" },
    { key: "smedical", label: "省医保", tab: "TJ0B", proc: "PRC_省医保月情况",
      page: "td/c/bb 省医保月转款情况(14)" },
    { key: "medical", label: "市医保", tab: "TJ0C", proc: "PRC_市医保月情况",
      page: "td/c/ba 市医保月转款情况(8)" },
    { key: "shenggjj", label: "省公积金", tab: "TJ0D", proc: "PRC_省公积金月情况",
      page: "td/c/bd 省公积金月转款情况(19)" },
    { key: "shigjj", label: "市公积金", tab: "TJ0E", proc: "PRC_市公积金月情况",
      page: "td/c/bc 市公积金月转款情况(18)" },
    { key: "sgongshang", label: "省工伤", tab: "TJ0F", proc: "PRC_工伤月情况",
      page: "td/c/bq 省工伤月转款情况(25)" },
  ];

  const rp = reactive({ ym: "202611", kind: "social", aab001: null, aaa030: null, bqOnly: false,
    aaa020: "", log: "", busy: false,
    dfRows: [], dfBusy: false, dfLog: "" });
  const units = ref([]); const heads = ref([]); const heads2 = ref([]);
  const rows = ref([]); const detailRows = ref([]);
  const cur = () => LINES.find(x => x.key === rp.kind) || LINES[0];

  const districts = ref([]);
  async function loadHeads() {
    let pushed = [];
    try { pushed = (await api.wb(`/hz/heads?ym=${rp.ym}&kind=social`)) || []; } catch (e) { console.log("[yue] hz/heads err", e.message); }
    // 主档案兜底: 户头(去重 4 项)与属地(独立 2 项)分列, 不做笛卡尔合并(合并导致户头重复)
    const hs = (await api.wb("/aaa030s").catch(() => [])) || [];
    const ds = (await api.wb("/districts").catch(() => [])) || [];
    if (!headsMaster.value.length)
      headsMaster.value = hs.map(h => ({ aaa030: h.aaa030, aaa031: h.aaa031, aaa100: "", aaa101: "" }));
    districts.value = ds;
    const seen = new Set(headsMaster.value.map(m => m.aaa030 + "-" + m.aaa100));
    heads.value = headsMaster.value.concat(pushed.filter(x => !seen.has(x.aaa030 + "-" + x.aaa100)));
  }

  async function rebuild() {
    const L = cur();
    if (!rp.aaa030 || !rp.aaa020) { ElementPlus.ElMessage.warning("先选户头与参保属地"); return; }
    rp.busy = true; rp.log = "";
    try {
      const r = await api.proc("PKG_派遣保险月情况." + L.proc,
        { aae002: rp.ym, aaa020: rp.aaa020, aaa030: rp.aaa030, aae011: api.store.oper.aae011 });
      if (r.skipped) rp.log = `${L.label}: ${r.skipped}`;
      else rp.log = `${L.label} 重建完成: 单元 ${r.units ?? "-"} / 明细 ${r.rows ?? "-"} 行`;
      await load();
    } catch (e) { rp.log = "重建失败: " + e.message; }
    rp.busy = false;
  }

  async function load() {
    if (!rp.aaa030 || !rp.aaa020) { ElementPlus.ElMessage.warning("先选户头与参保属地"); return; }
    try {
      rows.value = (await api.wb(`/yue/main?ym=${rp.ym}&tab=${cur().tab}&aaa030=${rp.aaa030}&aaa020=${encodeURIComponent(rp.aaa020)}`)) || [];
      rp.log = `汇总 ${rows.value.length} 行(已确认 ${rows.value.filter(r => r.aaezzz === "1").length})`;
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
  }

  async function loadDetail(row) {
    try {
      detailRows.value = (await api.wb(`/yue/detail?ym=${rp.ym}&tab=${cur().tab}&aaa030=${rp.aaa030}&aaa020=${encodeURIComponent(rp.aaa020)}&atb930=${row.atb930}`)) || [];
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
  }

  async function confirm() {
    const L = cur();
    try {
      await ElementPlus.ElMessageBox.confirm(
        `月转款确认将把 ${L.label} 本月台账置「已确认」(AAEZZZ='1', 已确认月重建将静默跳过), 确认?`, "月转款确认");
      const r = await api.proc("PKG_派遣保险月情况.PRC_月转款确认",
        { tabname: L.tab, aae002: rp.ym, aaa020: rp.aaa020, aaa030: rp.aaa030, aae011: api.store.oper.aae011 });
      rp.log = `确认完成: ${r.confirmed ?? 0} 行置已确认`;
      await load();
    } catch (e) { if (e !== "cancel") rp.log = "确认失败: " + e.message; }
  }

  function stTag(s) {
    return s === "1" ? "已确认" : (s === "0" ? "未确认" : (s || "-"));
  }

  // ---- W4d-3 补缴列 drill-down: 表头补缴列随险种而异(忠实口径) ----
  function bqNum(row) {
    return row.atj0a2_b ?? row.atj0b2_b ?? row.atj0f2_b1 ?? null;
  }
  function bqSum(row) {
    if (row.atj0a7_b != null) return row.atj0a7_b;
    if (row.atj0b5_b != null) return row.atj0b5_b;
    if (row.atj0f5_b1 != null || row.atj0f5_b2 != null)
      return (Number(row.atj0f5_b1) || 0) + (Number(row.atj0f5_b2) || 0);
    return null;
  }
  function bqRows() {
    return rp.bqOnly ? detailRows.value.filter(r => r.atj000 === "2") : detailRows.value;
  }

  // ---- W4d-3 月垫付(tdce:53): PRC_月垫付情况 + TJ0Z 读侧 ----
  const TTYPE_LABEL = { "11": "社保", "21": "省医保", "22": "市医保",
                        "31": "省工伤", "32": "市工伤", "41": "省公积金", "42": "市公积金" };
  async function dfCreate() {
    if (!rp.aaa030) { ElementPlus.ElMessage.warning("先选户头"); return; }
    rp.dfBusy = true; rp.dfLog = "";
    try {
      const r = await api.proc("PKG_派遣保险月情况.PRC_月垫付情况",
        { aae002: rp.ym, aaa030: rp.aaa030, aae011: api.store.oper.aae011 });
      rp.dfLog = `垫付台账生成完成: ${r.inserted ?? "-"} 行(硬序 社保→医保→工伤→公积金 清欠级联)`;
      await dfLoad();
    } catch (e) { rp.dfLog = "生成失败: " + e.message; }
    rp.dfBusy = false;
  }
  async function dfLoad() {
    if (!rp.aaa030) { ElementPlus.ElMessage.warning("先选户头"); return; }
    try {
      rp.dfRows = (await api.wb(`/yue/df?ym=${rp.ym}&aaa030=${rp.aaa030}`)) || [];
      rp.dfLog = `垫付台账 ${rp.dfRows.length} 行(垫付 ${rp.dfRows.filter(r => r.dfsign === "1").length})`;
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
  }

  const numCols = ref([]);
  function pick(row, keys) {
    const out = [];
    for (const k of keys) {
      const v = row[k];
      if (typeof v === "number" || (typeof v === "string" && v && !isNaN(v))) out.push(v);
    }
    return out;
  }

  WB.pages.yue = {
    template: `
    <div class="page">
      <el-form inline>
        <el-form-item label="险种">
          <el-select v-model="rp.kind" style="width:110px" @change="rows = []; detailRows = []">
            <el-option v-for="l in LINES" :key="l.key" :value="l.key" :label="l.label"/>
          </el-select>
        </el-form-item>
        <el-form-item label="年月">
          <el-date-picker v-model="rp.ym" type="month" value-format="YYYYMM" style="width:120px"/>
        </el-form-item>
        <el-form-item>
          <wb-dsf-tip menu="员工异动/补退费管理(六险种)" :page="cur().page"
            area="月转款情况" btn="重建(月情况)" note="调对应 PRC_*月情况: 守卫(减员未审核/已确认跳过)→清表→按单元×在保员工经险金引擎重建 TJ 台账">
            <el-button type="primary" :loading="rp.busy" @click="rebuild">重建月台账</el-button>
          </wb-dsf-tip>
          <wb-dsf-tip menu="员工异动/补退费管理(六险种)" :page="cur().page"
            area="月转款情况" btn="查询">
            <el-button @click="load">查询台账</el-button>
          </wb-dsf-tip>
        </el-form-item>
        <el-form-item label="户头">
          <el-select v-model="rp.aaa030" clearable style="width:200px" @change="rows = []; detailRows = []; loadHeads()">
            <el-option v-for="h in heads" :key="h.aaa030 + '-' + h.aaa100" :value="h.aaa030"
                       :label="h.aaa030 + ' ' + (h.aaa031 || '')"/>
          </el-select>
        </el-form-item>
        <el-form-item label="参保属地">
          <el-select v-model="rp.aaa020" clearable style="width:200px">
            <el-option v-for="d in districts" :key="d.id" :value="d.id" :label="(d.label || '') + ' ' + d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <wb-dsf-tip menu="员工异动/补退费管理(六险种)" :page="cur().page"
            area="月转款确认" btn="月转款确认" note="PRC_月转款确认: TABNAME 白名单(TJ0A~F)→主表 AAEZZZ='1'+经办, 明细 AAE013='1'; 已确认月重建静默跳过">
            <el-button size="small" type="danger" @click="confirm">月转款确认</el-button>
          </wb-dsf-tip>
        </el-form-item>
      </el-form>
      <div v-if="rp.log" class="kv">{{ rp.log }}</div>

      <h4>{{ cur().label }}月台账汇总</h4>
      <el-table :data="rows" size="small" border max-height="300" @row-click="loadDetail" highlight-current-row>
        <el-table-column prop="atb931" label="结算单元" min-width="140" show-overflow-tooltip/>
        <el-table-column prop="aab004" label="单位" min-width="150" show-overflow-tooltip/>
        <el-table-column label="人数" width="70"><template #default="{row}">{{ row.atj0a2 ?? row.atj0b2 ?? row.atj0c2 ?? row.atj0d2 ?? row.atj0e2 ?? row.atj0f2 }}</template></el-table-column>
        <el-table-column label="应收合计" width="110"><template #default="{row}">{{ row.atj0a7 ?? row.atj0b5 ?? row.atj0c5 ?? row.atj0d5 ?? row.atj0e5 ?? row.atj0f5 }}</template></el-table-column>
        <el-table-column label="补缴人数" width="90"><template #default="{row}">{{ bqNum(row) ?? "-" }}</template></el-table-column>
        <el-table-column label="补缴合计" width="110"><template #default="{row}">{{ bqSum(row) ?? "-" }}</template></el-table-column>
        <el-table-column label="状态" width="80"><template #default="{row}">
          <el-tag size="small" :type="row.aaezzz === '1' ? 'success' : 'info'">{{ stTag(row.aaezzz) }}</el-tag>
        </template></el-table-column>
        <el-table-column prop="aae036" label="经办时间" width="150"/>
      </el-table>
      <div class="kv">点行看明细; 应收合计口径因险种而异(社保 ATJ0A7 含补缴滞纳金/工伤 ATJ0F5=工伤单位缴 等, 旧系统忠实)。补缴列仅社保/省医保/工伤有表头 _B 口径(市医保/公积金补缴藏于明细 ATJ000='2' 行)。</div>

      <h4>明细
        <el-checkbox v-model="rp.bqOnly" size="small" style="margin-left:12px">只看补缴(ATJ000='2')</el-checkbox>
      </h4>
      <el-table v-if="detailRows.length" :data="bqRows()" size="small" border max-height="300">
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aac002" label="身份证号码" width="170"/>
        <el-table-column prop="aaa001" label="个人编号" width="100"/>
        <el-table-column prop="atb931" label="结算单元" min-width="130" show-overflow-tooltip/>
        <el-table-column label="缴费/补缴" width="90"><template #default="{row}">
          <el-tag size="small" :type="row.atj000 === '2' ? 'warning' : 'success'">{{ row.atj000 === '2' ? '补缴' : '缴费' }}</el-tag>
        </template></el-table-column>
        <el-table-column prop="atj0aa" label="当月新增" width="80"/>
        <template v-for="k in Object.keys(detailRows[0]).filter(k => !['aae002','atc900','atj000','atb930','atb931','aab001','aaa030','aaa031','aaa020','aaa021','aac003','aac002','aaa001','atj0aa','aae011','aae017','aae036','aae019','aae020','aae022'].includes(k))" :key="k">
          <el-table-column :prop="k" :label="k" width="105"/>
        </template>
      </el-table>

      <h4>月垫付台账 (TJ0Z, tdce:53 保险月垫付情况)</h4>
      <div style="margin-bottom:8px">
        <wb-dsf-tip menu="员工管理" page="tdce(53) 保险月垫付情况" area="月垫付" btn="生成垫付情况"
          note="PRC_月垫付情况: 编排器硬序 社保11→医保21/22→工伤31/32→公积金41/42(清欠金额依次扣除, 次序不可调); V_TB96 三分支判定 — 无结算记录/欠费('1','2')→全额垫付, 已到账('0')且额度不足→差额(公式只减 BX_ZJ 不减前序额度, 旧系统忠实)">
          <el-button type="warning" :loading="rp.dfBusy" @click="dfCreate">生成垫付台账</el-button>
        </wb-dsf-tip>
        <el-button size="small" @click="dfLoad">查询垫付台账</el-button>
      </div>
      <div v-if="rp.dfLog" class="kv">{{ rp.dfLog }}</div>
      <el-table v-if="rp.dfRows.length" :data="rp.dfRows" size="small" border max-height="300">
        <el-table-column label="险种" width="90"><template #default="{row}">{{ ttypeLabel(row.ttype) }}</template></el-table-column>
        <el-table-column prop="atb931" label="结算单元" min-width="130" show-overflow-tooltip/>
        <el-table-column prop="aab004" label="单位" min-width="150" show-overflow-tooltip/>
        <el-table-column prop="pnum_z" label="缴费人数" width="85"/>
        <el-table-column prop="mnum_sum_z" label="缴费合计" width="105"/>
        <el-table-column prop="pnum_b" label="补缴人数" width="85"/>
        <el-table-column prop="mnum_sum_b" label="补缴合计" width="105"/>
        <el-table-column prop="mnum_sum" label="转款合计" width="105"/>
        <el-table-column label="垫付" width="80"><template #default="{row}">
          <el-tag size="small" :type="row.dfsign === '1' ? 'danger' : 'info'">{{ row.dfsign === '1' ? '垫付' : '未垫' }}</el-tag>
        </template></el-table-column>
        <el-table-column prop="dfmoney" label="垫付金额" width="105"/>
        <el-table-column prop="zzz019" label="业务员" width="90"/>
        <el-table-column prop="aae036" label="经办时间" width="150"/>
      </el-table>
    </div>`,
    setup() {
      // 顶层 onMounted 无组件实例=永不触发(Vue 生产版静默 no-op), 加载放 setup 内直调
      api.wb("/units").then(u => { units.value = u || []; }).catch(() => {});
      loadHeads();
      return { rp, LINES, units, heads, districts, rows, detailRows, cur,
        loadHeads, rebuild, load, loadDetail, confirm, stTag,
        bqNum, bqSum, bqRows, dfCreate, dfLoad,
        ttypeLabel: (t) => TTYPE_LABEL[t] || (t || "-") };
    },
  };
})();
