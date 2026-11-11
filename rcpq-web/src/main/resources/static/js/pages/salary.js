/* 工资核对: 编制 TC93 当月行(/wb/salary-setup, 从合同带单位/结算单元) →
   核对批次(TC93_BP1 + PRC_CHECK_TC93 三金额比对) → 提交(最低工资 80% 门槛, atc93p='0')。
   端到端参照 e2e_business.py 步 5-7。 */
(() => {
  const { reactive, ref, computed } = Vue;
  const { api } = WB;

  const salary = reactive({ atc900: "", ym: "202611", atc93aa: 6800, atb930: null,
    contract: null, tc930: "", log: "", busy: false });
  const step = computed(() => (salary.atc93p ? 3 : (salary.batchDone ? 2 : (salary.tc930 ? 1 : 0))));
  const batchRows = ref([]);

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
    </div>`,
    setup() { return { salary, step, batchRows, onPick, run, api }; },
  };
})();
