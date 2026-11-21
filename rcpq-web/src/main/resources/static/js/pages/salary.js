/* 工资核对: 编制 TC93 当月行(/wb/salary-setup, 从合同带单位/结算单元) →
   核对批次(TC93_BP1 + PRC_CHECK_TC93 三金额比对) → 提交(最低工资 80% 门槛, atc93p='0')。
   另接 8b 费用结算确认/取消(PKG_RCPQ_FZ.PRC_SALARYCOMPUTE/CANCLESALARYCOMPUTE, 东软 td/d/d):
   按结算单元+费用年月+次第逐未结算 TC93 行落账(atc93g='1'+折子贪心扣款+TC9P 标记),
   取消为忠实坏功能第六例(非孤儿行 TC31 无列报错即止)。
   端到端参照 e2e_business.py 步 5-7 与 e2e_import_sys.py §9。 */
(() => {
  const { reactive, ref, computed, onMounted } = Vue;
  const { api } = WB;

  const salary = reactive({ atc900: "", ym: "202611", atc93aa: 6800, atb930: null,
    contract: null, tc930: "", log: "", busy: false });
  const step = computed(() => (salary.atc93p ? 3 : (salary.batchDone ? 2 : (salary.tc930 ? 1 : 0))));
  const batchRows = ref([]);

  // 8b 费用结算确认/取消
  const stl = reactive({ atb930: null, atc931: "202611", atc932: "202611", atc937: "1",
    log: "", busy: false });
  const settleUnits = ref([]);
  const _now = () => new Date().getFullYear() + String(new Date().getMonth() + 1).padStart(2, "0");
  stl.atc931 = _now(); stl.atc932 = _now();

  async function settleRun(cancel) {
    if (!stl.atb930) { ElementPlus.ElMessage.warning("先选结算单元"); return; }
    if (cancel) {
      try { await ElementPlus.ElMessageBox.confirm(
        "取消结算: 非孤儿工资行(关联真实合同)会触发忠实坏功能第六例——TC31 无 atc930 列," +
        "旧系统运行时同样报错, TC9P 重置生效后中断。确认继续?", "结算取消"); }
      catch (e) { return; }
    }
    stl.busy = true; stl.log = "";
    try {
      const r = await api.proc(cancel ? "PKG_RCPQ_FZ.PRC_CANCLESALARYCOMPUTE"
                                      : "PKG_RCPQ_FZ.PRC_SALARYCOMPUTE",
        { atb930: String(stl.atb930), atc931: stl.atc931, atc932: stl.atc932,
          atc937: String(stl.atc937), aae011: api.store.oper.aae011 });
      stl.log = cancel
        ? `结算取消完成: 逆转 ${r.cancelled ?? 0} 行(孤儿行物理删=忠实行为)`
        : `结算确认完成: 落账 ${r.settled ?? 0} 行(atc93g='1'+折子扣款+TC9P 标记), 体检费孤儿行生成 ${r.orphanExamRows ?? 0}`;
      ElementPlus.ElMessage.success(stl.log.split("（")[0].split("(")[0]);
    } catch (e) { stl.log = (cancel ? "取消" : "确认") + "失败: " + e.message; }
    stl.busy = false;
  }

  async function loadSettleBase() {
    try { settleUnits.value = (await api.wb("/settle-units")) || []; } catch (e) { /* 保底 */ }
  }

  function onPick(row) {
    salary.atc900 = row.atc900; salary.atb930 = row.atb930; salary.contract = row;
  }

  async function run() {
    if (!salary.atc900) { ElementPlus.ElMessage.warning("先在下方搜索并选择合同"); return; }
    salary.busy = true; salary.log = ""; batchRows.value = [];
    salary.tc930 = ""; salary.batchDone = false; salary.atc93p = null;
    try {
      const setup = await api.wb("/salary-setup", { atc900: Number(salary.atc900),
        ym: salary.ym, atc93aa: String(salary.atc93aa ?? ""), atb930: Number(salary.atb930) });
      salary.tc930 = setup.atc930;
      salary.log = "① 编制: TC93 atc930=" + salary.tc930 + "  核对批次 " + setup.ace100 + "\n";
      await api.proc("PKG_IMP.PRC_CHECK_TC93", { ace100: String(setup.ace100),
        atb930: String(salary.atb930), atc931: salary.ym, atc932: salary.ym, atc937: "1" });
      const b = await api.wb("/batch/" + setup.ace100);
      batchRows.value = (b.rows || []).filter(r => r.aac003 === salary.contract.aac003);
      if (batchRows.value.some(r => r.ace10a === "2")) {
        salary.log += "② 核对: 存在拒绝行(三金额不一致?), 见下表, 不提交。\n";
      } else {
        salary.batchDone = true;
        salary.log += "② 核对: 通过(ace10a=3)\n";
        await api.proc("PKG_IMP.PRC_COMMIT_TC93", { ace100: String(setup.ace100),
          atc932: salary.ym, atc931: salary.ym, ...api.operAae() });
        salary.atc93p = "0";
        salary.log += "③ 提交: 完成(最低工资 80% 门槛已判, atc93p='0')\n";
      }
    } catch (e) { salary.log += "错误: " + e.message + "\n"; }
    salary.busy = false;
  }

  WB.pages.salary = {
    template: `
    <div class="page">
      <el-steps :active="step" simple style="margin-bottom:14px">
        <el-step title="① 编制 TC93" :description="salary.tc930 ? 'atc930=' + salary.tc930 : ''"/>
        <el-step title="② 核对" :description="salary.batchDone ? 'ace10a=3' : ''"/>
        <el-step title="③ 提交" :description="salary.atc93p ? 'atc93p=' + salary.atc93p : ''"/>
      </el-steps>
      <el-form inline label-width="110px">
        <el-form-item label="年月">
          <el-date-picker v-model="salary.ym" type="month" value-format="YYYYMM"
                          placeholder="202611" style="width:120px"/></el-form-item>
        <el-form-item label="本次工资总额">
          <el-input-number v-model="salary.atc93aa" :min="0" :precision="2" :controls="false"
                           placeholder="6800" style="width:130px"/></el-form-item>
      </el-form>
      <wb-contract-pick @pick="onPick"/>
      <template v-if="salary.contract">
        <el-descriptions :column="4" border size="small" style="margin:10px 0">
          <el-descriptions-item label="合同号">{{ salary.contract.atc900 }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ salary.contract.aac003 }}</el-descriptions-item>
          <el-descriptions-item label="结算单元">{{ salary.contract.atb930 }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ salary.contract.aab004 }}</el-descriptions-item>
        </el-descriptions>
        <wb-dsf-tip menu="工资(薪酬)" page="td/d/g 工资核对(工资次第信息)"
          area="导入核对批次 TC93_BP1" btn="核对/提交(PKG_IMP.CHECK_TC93)"
          note="新系统一键编制 TC93 当月行→核对→提交; 核对含三金额比对/最低工资 80% 门槛">
          <el-button type="primary" :loading="salary.busy" @click="run">一键: 编制 → 核对 → 提交</el-button>
        </wb-dsf-tip>
      </template>
      <pre class="log" v-if="salary.log">{{ salary.log }}</pre>
      <template v-if="batchRows.length">
        <h4>核对批次明细 (tc93_bp1)</h4>
        <el-table :data="batchRows" size="small" border>
          <el-table-column prop="aac003" label="姓名" width="100"/>
          <el-table-column prop="atc93aa" label="本次工资总额" width="120"/>
          <el-table-column label="核对" width="80">
            <template #default="{ row }">
              <el-tag :type="api.batchType(row.ace10a)" size="small">{{ api.batchLabel(row.ace10a) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ace10b" label="原因" min-width="220" show-overflow-tooltip/>
        </el-table>
      </template>
      <div class="kv">核对逻辑: 找同键 TC93 比对工资总额/年终奖/经济补偿金; 提交判 AA99 最低工资 80% 门槛</div>

      <h4>费用结算确认 / 取消（东软 td/d/d 8b · 按结算单元+费用年月+次第整批落账）</h4>
      <el-form inline label-width="110px">
        <el-form-item label="结算单元">
          <el-select v-model="stl.atb930" filterable placeholder="选结算单元" style="width:240px">
            <el-option v-for="s in settleUnits" :key="s.atb930" :value="s.atb930"
                       :label="s.atb930 + ' ' + s.atb931"/>
          </el-select></el-form-item>
        <el-form-item label="费用年月">
          <el-date-picker v-model="stl.atc931" type="month" value-format="YYYYMM" style="width:120px"/></el-form-item>
        <el-form-item label="工资年月">
          <el-date-picker v-model="stl.atc932" type="month" value-format="YYYYMM" style="width:120px"/></el-form-item>
        <el-form-item label="次第">
          <el-input-number v-model="stl.atc937" :min="1" :max="9" :controls="false" style="width:70px"/></el-form-item>
        <el-form-item>
          <wb-dsf-tip menu="工资(薪酬)" page="td/d/d 费用结算确认" area="结算工作流" btn="结算确认"
            note="PKG_RCPQ_FZ.PRC_SALARYCOMPUTE — 逐未结算 TC93 行(atc93g<>'1'): 金额合计→折子贪心扣款(TC91/TC91_LOG)→atc93g='1'+TC9P 标记→首次工资当月 AC10 落位">
            <el-button type="primary" :loading="stl.busy" @click="settleRun(false)">结算确认</el-button>
          </wb-dsf-tip>
          <wb-dsf-tip menu="工资(薪酬)" page="td/d/d 费用结算确认" area="结算工作流" btn="取消结算"
            note="PKG_RCPQ_FZ.PRC_CANCLESALARYCOMPUTE — 忠实坏功能第六例: 非孤儿行 TC31 无 atc930 列报错即止(旧系统同样报错); 仅孤儿行(atc900=0)可物理删逆转">
            <el-button type="warning" plain :loading="stl.busy" @click="settleRun(true)">取消结算</el-button>
          </wb-dsf-tip>
        </el-form-item>
      </el-form>
      <pre class="log" v-if="stl.log">{{ stl.log }}</pre>
      <div class="kv">结算与核对的关系: 核对提交只置 atc93p='0'(已核对), 结算确认才置 atc93g='1'(已结算落账+折子扣款)</div>
    </div>`,
    setup() {
      onMounted(loadSettleBase);
      return { salary, step, batchRows, onPick, run, stl, settleUnits, settleRun, api };
    },
  };
})();
