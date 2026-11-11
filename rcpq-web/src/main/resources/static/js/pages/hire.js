/* 新签导入: 单人录入合同要素 → 批次(tc90_bp) → 员工导入校验 → (无拒绝行才)提交
   生成 AC01/TC90/TC90_LIST/TB91(20/2 待审), 之后转审核台。
   等价旧系统 tdbp 新签弹窗的校验/提交按钮; 端到端参照 e2e_business.py 步 1-2。 */
(() => {
  const { reactive, ref, computed, watch, onMounted } = Vue;
  const { api } = WB;

  const hire = reactive({
    aac002: "", aac003: "", aae00p: "1-派遣",
    aab001: null, atb900: null, atb930: null,
    atc90a: "1-固定期限", atc90c: api.YMD, atc90d: "", atc905: "6800",
    aae002: "202611", aaa301: "", aaa100: "220100000000-吉林省长春市", aaa203: "202611",
    aae005: "", atc90ax: "", atc90b: "", atb90a: "", atb90b: "0",
    log: "", busy: false,
  });
  const units = ref([]); const agreements = ref([]); const settleUnits = ref([]);
  const baseErr = ref(""); const batchRows = ref([]); const batchTable = ref("");

  async function loadBase() {
    baseErr.value = "";
    try {
      units.value = (await api.wb("/units")) || [];
      settleUnits.value = (await api.wb("/settle-units")) || [];
      if (!units.value.length) baseErr.value = "单位清单为空(接口通但无数据)";
    } catch (e) { baseErr.value = "基础数据加载失败: " + e.message; }
  }

  const settleOptions = computed(() => {
    const hit = settleUnits.value.filter(s => s.aab001 === hire.aab001);
    return hit.length ? hit : settleUnits.value;
  });

  watch(() => hire.aab001, async v => {
    hire.atb900 = null;
    hire.atb930 = null;
    agreements.value = v ? await api.wb("/agreements?aab001=" + v).catch(() => []) : [];
    if (agreements.value.length === 1) hire.atb900 = agreements.value[0].atb900;
  });

  onMounted(async () => {
    await loadBase();
    if (baseErr.value) setTimeout(() => { if (baseErr.value || !units.value.length) loadBase(); }, 2000);
  });

  async function run() {
    if (!hire.aac002 || !hire.aac003) { ElementPlus.ElMessage.warning("身份证/姓名必填"); return; }
    if (!hire.atb900 || !hire.atb930 || !hire.aab001) {
      ElementPlus.ElMessage.warning("单位 / 派遣协议 / 结算单元都要选"); return;
    }
    if (!hire.aae005 || !hire.atc90ax || !hire.atb90a) {
      ElementPlus.ElMessage.warning("联系电话 / 身份证有效期 / 单位服务费月标准必填(校验硬性要求)"); return;
    }
    hire.busy = true; hire.log = ""; batchRows.value = []; batchTable.value = "";
    try {
      const unit = units.value.find(u => u.aab001 === hire.aab001);
      const { ace100: ace } = await api.wb("/hire-batch", {
        aac002: hire.aac002, aac003: hire.aac003, aae00p: hire.aae00p,
        atb930: hire.atb930, atb900: hire.atb900, aab001: hire.aab001,
        atc90a: hire.atc90a, atc90c: hire.atc90c, atc90d: hire.atc90d || null,
        atc905: hire.atc905, aae002: hire.aae002, aaa401: "1-是", aaa100: hire.aaa100,
        aaa203: hire.aaa203, aaa301: hire.aaa301 || null,
        aae005: hire.aae005, atc90ax: hire.atc90ax,
        atc90b: hire.atc90b || hire.atc90c, atb90a: hire.atb90a, atb90b: hire.atb90b || "0",
        aab004: unit ? unit.aab004 : "单位名" });
      hire.log = "批次 " + ace + "\n";
      let rejected = false;
      try {
        await api.proc("PKG_派遣批量导入.PRC_员工导入校验",
          { ace100: String(ace), aae011: api.store.oper.aae011 });
        hire.log += "校验: 通过\n";
      } catch (e) { hire.log += "校验: " + e.message + "\n"; }
      const b = await api.wb("/batch/" + ace);
      batchRows.value = b.rows || []; batchTable.value = b.table || "";
      rejected = batchRows.value.some(r => r.ace10a === "2");
      if (rejected) {
        hire.log += "存在拒绝行(见下表原因), 不提交。修正后重新导入。\n";
      } else {
        await api.proc("PKG_派遣批量导入.PRC_员工导入提交",
          { ace100: String(ace), aae011: api.store.oper.aae011 });
        hire.log += "提交完成 → AC01/TC90 已生成, 转审核台(新签审核台页签)\n";
      }
    } catch (e) { hire.log += "错误: " + e.message + "\n"; }
    hire.busy = false;
  }

  WB.pages.hire = {
    template: `
    <div class="page">
      <h4>① 单位 / 协议 / 结算单元</h4>
      <div v-if="baseErr" class="bad">{{ baseErr }}
        <el-button size="small" type="warning" plain @click="loadBase">重试</el-button></div>
      <el-form inline label-width="120px">
        <el-form-item label="单位">
          <el-select v-model="hire.aab001" filterable placeholder="选单位" style="width:300px">
            <el-option v-for="u in units" :key="u.aab001" :value="u.aab001"
                       :label="u.aab001 + ' ' + u.aab004"/>
          </el-select>
        </el-form-item>
        <el-form-item label="派遣协议 ATB900">
          <el-select v-model="hire.atb900" filterable placeholder="选协议" style="width:150px">
            <el-option v-for="a in agreements" :key="a.atb900" :value="a.atb900"
                       :label="String(a.atb900)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="结算单元 ATB930">
          <el-select v-model="hire.atb930" filterable placeholder="选结算单元" style="width:300px">
            <el-option v-for="s in settleOptions" :key="s.atb930" :value="s.atb930"
                       :label="s.atb930 + ' ' + s.atb931"/>
          </el-select>
        </el-form-item>
      </el-form>
      <h4>② 合同要素</h4>
      <el-form label-width="130px" size="default">
        <el-row :gutter="8">
          <el-col :span="8"><el-form-item label="身份证 AAC002"><el-input v-model="hire.aac002"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="姓名 AAC003"><el-input v-model="hire.aac003"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="用工性质"><el-input v-model="hire.aae00p"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合同类别"><el-input v-model="hire.atc90a"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合同开始"><el-input v-model="hire.atc90c"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合同终止"><el-input v-model="hire.atc90d" placeholder="2028-11-01"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合同工资"><el-input v-model="hire.atc905"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="联系电话"><el-input v-model="hire.aae005" placeholder="13000000000"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="身份证有效期"><el-input v-model="hire.atc90ax" placeholder="2035-12-31"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="协议首签日期"><el-input v-model="hire.atc90b" placeholder="默认=合同开始"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单位服务费/月"><el-input v-model="hire.atb90a" placeholder="150"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="个人服务费/月"><el-input v-model="hire.atb90b" placeholder="0"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="费用开始年月"><el-input v-model="hire.aae002"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="养老基数"><el-input v-model="hire.aaa301" placeholder="6800"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="参保属地"><el-input v-model="hire.aaa100"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="参保年月"><el-input v-model="hire.aaa203"/></el-form-item></el-col>
        </el-row>
        <el-button type="primary" :loading="hire.busy" @click="run">③ 生成批次 → 校验 → 提交</el-button>
        <span class="kv">  等价旧系统: tdbp 新签弹窗的校验/提交按钮</span>
      </el-form>
      <pre class="log" v-if="hire.log">{{ hire.log }}</pre>
      <template v-if="batchRows.length">
        <h4>批次明细 ({{ batchTable }})</h4>
        <el-table :data="batchRows" size="small" border max-height="300">
          <el-table-column prop="aac003" label="姓名" width="90"/>
          <el-table-column prop="aac002" label="身份证" width="170"/>
          <el-table-column prop="atc90a" label="合同类别" width="110"/>
          <el-table-column prop="atc90c" label="开始" width="100"/>
          <el-table-column prop="atc90d" label="终止" width="100"/>
          <el-table-column prop="atc905" label="合同工资" width="90"/>
          <el-table-column label="校验" width="80">
            <template #default="{ row }">
              <el-tag :type="api.batchType(row.ace10a)" size="small">{{ api.batchLabel(row.ace10a) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ace10b" label="原因" min-width="200" show-overflow-tooltip/>
        </el-table>
      </template>
    </div>`,
    setup() {
      return { hire, units, agreements, settleOptions, baseErr, loadBase,
        batchRows, batchTable, run, api };
    },
  };
})();
