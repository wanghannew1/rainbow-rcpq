/* 变更与退出: 合同续签(批次 tc90_bp3, 匹配生效合同, 写 TC90_QDLS + atc90f='2')
   与 合同终止(批次 tc90_bp1, 有效合同转审核 TB911=23, 已终审直接落报减年月)。
   端到端参照 e2e_business.py 步 10-11。 */
(() => {
  const { reactive, ref } = Vue;
  const { api } = WB;

  const renew = reactive({ aac002: "", aac003: "", atc90c: "", atc90d: "", contract: null,
    log: "", busy: false });
  const term = reactive({ aac002: "", aac003: "", atc90i: "", atc90av: "", contract: null,
    log: "", busy: false });
  const renewRows = ref([]); const termRows = ref([]);

  function pickRenew(row) {
    renew.contract = row; renew.aac002 = row.aac002; renew.aac003 = row.aac003;
    if (row.atc90d) renew.atc90c = row.atc90d; // 默认次日起新约, 由业务员改
  }
  function pickTerm(row) {
    term.contract = row; term.aac002 = row.aac002; term.aac003 = row.aac003;
  }

  async function renewRun() {
    renew.busy = true; renew.log = ""; renewRows.value = [];
    try {
      const { ace100: ace } = await api.wb("/renew-batch",
        { aac002: renew.aac002, aac003: renew.aac003, atc90c: renew.atc90c, atc90d: renew.atc90d });
      await api.proc("PKG_派遣批量导入.PRC_员工续签校验",
        { ace100: String(ace), aae011: api.store.oper.aae011 });
      const b = await api.wb("/batch/" + ace);
      renewRows.value = b.rows || [];
      if (renewRows.value.some(r => r.ace10a === "2")) {
        renew.log = "校验存在拒绝行(无生效合同?), 见下表, 不提交。\n";
      } else {
        await api.proc("PKG_派遣批量导入.PRC_员工续签提交",
          { ace100: String(ace), aae011: api.store.oper.aae011 });
        renew.log = "续签完成: atc90f='2', 签订历史已入 TC90_QDLS\n";
      }
    } catch (e) { renew.log = "错误: " + e.message + "\n"; }
    renew.busy = false;
  }

  async function termRun() {
    term.busy = true; term.log = ""; termRows.value = [];
    try {
      const { ace100: ace } = await api.wb("/terminate-batch",
        { aac002: term.aac002, aac003: term.aac003, atc90i: term.atc90i, atc90av: term.atc90av });
      await api.proc("PKG_派遣批量导入.PRC_员工终止导入校验",
        { ace100: String(ace), aae011: api.store.oper.aae011 });
      const b = await api.wb("/batch/" + ace);
      termRows.value = b.rows || [];
      if (termRows.value.some(r => r.ace10a === "2")) {
        term.log = "校验存在拒绝行, 见下表, 不提交。\n";
      } else {
        await api.proc("PKG_派遣批量导入.PRC_员工终止导入提交",
          { ace100: String(ace), aae011: api.store.oper.aae011 });
        term.log = "终止已提交: 生效合同转审核(TB91 23), 已终审合同直接落报减年月\n";
      }
    } catch (e) { term.log = "错误: " + e.message + "\n"; }
    term.busy = false;
  }

  WB.pages.change = {
    template: `
    <div class="page">
      <h4>合同续签(批次 TC90_BP3; 找生效合同 atb90r='3' 带出单位要素)</h4>
      <wb-contract-pick @pick="pickRenew"/>
      <el-form inline style="margin-top:8px">
        <el-form-item label="身份证"><el-input v-model="renew.aac002" style="width:170px"/></el-form-item>
        <el-form-item label="姓名"><el-input v-model="renew.aac003" style="width:120px"/></el-form-item>
        <el-form-item label="新开始">
          <el-date-picker v-model="renew.atc90c" type="date" value-format="YYYY-MM-DD"
                          placeholder="2028-12-01" style="width:140px"/></el-form-item>
        <el-form-item label="新终止">
          <el-date-picker v-model="renew.atc90d" type="date" value-format="YYYY-MM-DD"
                          placeholder="2030-12-01" style="width:140px"/></el-form-item>
        <el-form-item><el-button type="primary" :loading="renew.busy" @click="renewRun">校验+提交</el-button></el-form-item>
      </el-form>
      <pre class="log" v-if="renew.log">{{ renew.log }}</pre>
      <el-table v-if="renewRows.length" :data="renewRows" size="small" border style="margin-bottom:8px">
        <el-table-column prop="aac003" label="姓名" width="100"/>
        <el-table-column prop="atc90a" label="新合同类别" width="110"/>
        <el-table-column prop="atc90c" label="新开始" width="100"/>
        <el-table-column prop="atc90d" label="新终止" width="100"/>
        <el-table-column label="校验" width="80">
          <template #default="{ row }">
            <el-tag :type="api.batchType(row.ace10a)" size="small">{{ api.batchLabel(row.ace10a) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ace10b" label="原因" min-width="200" show-overflow-tooltip/>
      </el-table>

      <h4>合同终止(批次 TC90_BP1; 终止日期/工资结束年月, 终止原因固定 09-职工个人原因)</h4>
      <wb-contract-pick @pick="pickTerm"/>
      <el-form inline style="margin-top:8px">
        <el-form-item label="身份证"><el-input v-model="term.aac002" style="width:170px"/></el-form-item>
        <el-form-item label="姓名"><el-input v-model="term.aac003" style="width:120px"/></el-form-item>
        <el-form-item label="终止日期">
          <el-date-picker v-model="term.atc90i" type="date" value-format="YYYY-MM-DD"
                          placeholder="2028-12-31" style="width:140px"/></el-form-item>
        <el-form-item label="工资结束年月">
          <el-date-picker v-model="term.atc90av" type="month" value-format="YYYYMM"
                          placeholder="202812" style="width:120px"/></el-form-item>
        <el-form-item><el-button type="danger" :loading="term.busy" @click="termRun">校验+提交</el-button></el-form-item>
      </el-form>
      <pre class="log" v-if="term.log">{{ term.log }}</pre>
      <el-table v-if="termRows.length" :data="termRows" size="small" border>
        <el-table-column prop="aac003" label="姓名" width="100"/>
        <el-table-column prop="atc90i" label="终止日期" width="110"/>
        <el-table-column prop="aae003" label="费用结束" width="100"/>
        <el-table-column prop="atc90k" label="终止原因" width="140"/>
        <el-table-column label="校验" width="80">
          <template #default="{ row }">
            <el-tag :type="api.batchType(row.ace10a)" size="small">{{ api.batchLabel(row.ace10a) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ace10b" label="原因" min-width="200" show-overflow-tooltip/>
      </el-table>
    </div>`,
    setup() {
      return { renew, term, renewRows, termRows, pickRenew, pickTerm, renewRun, termRun, api };
    },
  };
})();
