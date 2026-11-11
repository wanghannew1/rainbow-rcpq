/* 到账 / 发票: 银行回单导入 TB97(tb97_bp+工行校验/提交) 与 发票导入 TB99(tb99_bp)。
   端到端参照 e2e_business.py 步 8-9。 */
(() => {
  const { reactive, ref, onMounted } = Vue;
  const { api } = WB;

  const arr = reactive({ abp001: api.YMD, abp002: "", abp005: null, log: "", busy: false });
  const inv = reactive({ invTitle: "", abp004: null, log: "", busy: false });
  const arrRows = ref([]); const invRows = ref([]);
  const arrivals = ref([]); const invoices = ref([]);

  async function loadLists() {
    try {
      arrivals.value = (await api.wb("/arrivals")) || [];
      invoices.value = (await api.wb("/invoices")) || [];
    } catch (e) { /* 列表失败不阻断导入 */ }
  }

  async function arriveRun() {
    arr.busy = true; arr.log = ""; arrRows.value = [];
    try {
      const { ace100: ace } = await api.wb("/arrival-batch",
        { abp001: arr.abp001, abp002: arr.abp002, abp005: String(arr.abp005 ?? "") });
      await api.proc("PKG_派遣批量导入.PRC_工行到账导入校验", { ace100: String(ace) });
      const b = await api.wb("/batch/" + ace);
      arrRows.value = b.rows || [];
      if (arrRows.value.some(r => r.ace10a === "2")) {
        arr.log = "校验存在拒绝行(见下表), 不提交。\n";
      } else {
        await api.proc("PKG_派遣批量导入.PRC_工行到账导入提交",
          { ace100: String(ace), aae011: api.store.oper.aae011 });
        arr.log = "到账已导入 TB97 (户名 " + arr.abp002 + ")\n";
        loadLists();
      }
    } catch (e) { arr.log = "错误: " + e.message + "\n"; }
    arr.busy = false;
  }

  async function invoiceRun() {
    inv.busy = true; inv.log = ""; invRows.value = [];
    try {
      const { ace100: ace } = await api.wb("/invoice-batch",
        { abp001: inv.invTitle, abp004: String(inv.abp004 ?? "") });
      await api.proc("PKG_派遣批量导入.PRC_发票导入校验", { ace100: String(ace) });
      const b = await api.wb("/batch/" + ace);
      invRows.value = b.rows || [];
      if (invRows.value.some(r => r.ace10a === "2")) {
        inv.log = "校验存在拒绝行(见下表), 不提交。\n";
      } else {
        await api.proc("PKG_派遣批量导入.PRC_发票导入提交",
          { ace100: String(ace), aae011: api.store.oper.aae011 });
        inv.log = "发票已导入 TB99 (抬头 " + inv.invTitle + ")\n";
        loadLists();
      }
    } catch (e) { inv.log = "错误: " + e.message + "\n"; }
    inv.busy = false;
  }

  WB.pages.money = {
    template: `
    <div class="page">
      <h4>银行到账(工行模板: 日期/户名/金额)</h4>
      <el-form inline>
        <el-form-item label="到账日期">
          <el-date-picker v-model="arr.abp001" type="date" value-format="YYYY-MM-DD"
                          style="width:140px"/></el-form-item>
        <el-form-item label="对方户名"><el-input v-model="arr.abp002" style="width:220px"/></el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="arr.abp005" :min="0" :precision="2" :controls="false"
                           style="width:130px"/></el-form-item>
        <el-form-item><el-button type="primary" :loading="arr.busy" @click="arriveRun">导入 TB97</el-button></el-form-item>
      </el-form>
      <pre class="log" v-if="arr.log">{{ arr.log }}</pre>
      <el-table v-if="arrRows.length" :data="arrRows" size="small" border style="margin-bottom:8px">
        <el-table-column prop="abp001" label="日期" width="110"/>
        <el-table-column prop="abp002" label="户名" min-width="200"/>
        <el-table-column prop="abp005" label="金额" width="100"/>
        <el-table-column label="校验" width="80">
          <template #default="{ row }">
            <el-tag :type="api.batchType(row.ace10a)" size="small">{{ api.batchLabel(row.ace10a) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ace10b" label="原因" min-width="180" show-overflow-tooltip/>
      </el-table>

      <h4>发票(抬头/服务费金额; 固定借发票+普票+人事代理服务费)</h4>
      <el-form inline>
        <el-form-item label="发票抬头"><el-input v-model="inv.invTitle" style="width:220px"/></el-form-item>
        <el-form-item label="服务费金额">
          <el-input-number v-model="inv.abp004" :min="0" :precision="2" :controls="false"
                           style="width:130px"/></el-form-item>
        <el-form-item><el-button type="primary" :loading="inv.busy" @click="invoiceRun">导入 TB99</el-button></el-form-item>
      </el-form>
      <pre class="log" v-if="inv.log">{{ inv.log }}</pre>
      <el-table v-if="invRows.length" :data="invRows" size="small" border style="margin-bottom:8px">
        <el-table-column prop="abp001" label="抬头" min-width="200"/>
        <el-table-column prop="abp004" label="金额" width="100"/>
        <el-table-column label="校验" width="80">
          <template #default="{ row }">
            <el-tag :type="api.batchType(row.ace10a)" size="small">{{ api.batchLabel(row.ace10a) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ace10b" label="原因" min-width="180" show-overflow-tooltip/>
      </el-table>

      <h4>最近到账 (TB97 前 50)</h4>
      <el-table :data="arrivals" size="small" border max-height="260">
        <el-table-column prop="atb970" label="到账号" width="90"/>
        <el-table-column prop="atb974" label="日期" width="100"/>
        <el-table-column prop="aab004" label="单位" min-width="180" show-overflow-tooltip/>
        <el-table-column prop="atb931" label="结算单元" min-width="160" show-overflow-tooltip/>
        <el-table-column prop="atb971" label="单位缴" width="90"/>
        <el-table-column prop="atb972" label="个人缴" width="90"/>
        <el-table-column prop="atb973" label="合计" width="90"/>
      </el-table>
      <h4>最近发票 (TB99 前 50)</h4>
      <el-table :data="invoices" size="small" border max-height="260">
        <el-table-column prop="atb990" label="发票号" width="90"/>
        <el-table-column prop="atb99y" label="抬头" min-width="200" show-overflow-tooltip/>
        <el-table-column prop="atb994" label="金额" width="100"/>
        <el-table-column prop="aab004" label="单位" min-width="180" show-overflow-tooltip/>
        <el-table-column prop="atb993" label="项目串" min-width="160" show-overflow-tooltip/>
      </el-table>
    </div>`,
    setup() {
      onMounted(loadLists);
      return { arr, inv, arrRows, invRows, arrivals, invoices, arriveRun, invoiceRun, api };
    },
  };
})();
