/* 到账 / 发票: 银行回单导入 TB97(工行/建行/吉林银行 三银行) 与 发票导入 TB99(tb99_bp)。
   两路入口: ① 单笔表单(工行, /wb/arrival-batch) ② Excel 批量(/import/sys tb97_bp/bp01/bp02,
   东软 td/l/y 三银行弹窗工作台化; 建行 5 列含摘要, 吉林 4 列)。
   端到端参照 e2e_business.py 步 8-9 与 e2e_import_sys.py §8。 */
(() => {
  const { reactive, ref, onMounted } = Vue;
  const { api } = WB;

  const arr = reactive({ abp001: api.YMD, abp002: "", abp005: null, log: "", busy: false });
  const inv = reactive({ invTitle: "", abp004: null, log: "", busy: false });
  const arrRows = ref([]); const invRows = ref([]);
  const arrivals = ref([]); const invoices = ref([]);

  // 三银行 Excel 批量(东软 td/l/y 弹窗): 校验/提交过程各银行独立
  const BANKS = [
    { key: "gs", id: "tb97_bp", label: "工商银行", sheet: "到账回盘（工商）",
      check: "PRC_工行到账导入校验", commit: "PRC_工行到账导入提交" },
    { key: "jh", id: "tb97_bp01", label: "建设银行", sheet: "到账回盘（建行）",
      check: "PRC_建行到账导入校验", commit: "PRC_建行到账导入提交",
      note: "建行回单 5 列: 到账日期(填 8 位 20261101)/金额/对方户名/对方账号/摘要" },
    { key: "jlyh", id: "tb97_bp02", label: "吉林银行", sheet: "到账回盘（吉林银行）",
      check: "PRC_吉林银行到账导入校验", commit: "PRC_吉林银行到账导入提交",
      note: "吉林银行回单 4 列: 到账日期(填 8 位 20261101)/金额/对方户名/对方账号" },
  ];
  const bk = reactive({ key: "gs", file: null, ace: null, log: "", rows: [], table: "", busy: false });
  const curBank = () => BANKS.find(b => b.key === bk.key) || BANKS[0];

  function onBankFile(ev) { bk.file = ev.target.files[0] || null; }
  function pickBankFile() { document.getElementById("bank-file")?.click(); }

  async function uploadBank() {
    const b = curBank();
    if (!bk.file) { ElementPlus.ElMessage.warning("先选择银行回单 Excel"); return; }
    bk.busy = true; bk.log = ""; bk.rows = [];
    try {
      const fd = new FormData();
      fd.append("file", bk.file);
      fd.append("parameters", JSON.stringify({ aae011: api.store.oper.aae011 }));
      const resp = await fetch("/import/sys/" + b.id, { method: "POST", body: fd });
      const j = await resp.json();
      if (!j.succeed) { bk.log = "导入失败: " + (j.msg || resp.status); ElementPlus.ElMessage.error(j.msg || "导入失败"); }
      else {
        bk.ace = (j.ce10 || [])[0];
        const bb = await api.wb("/batch/" + bk.ace);
        bk.rows = bb.rows || []; bk.table = bb.table || "";
        bk.log = `导入成功: 批次 ${bk.ace}(${bk.table}) — 共 ${bk.rows.length} 行; 先【校验】再【提交】`;
      }
    } catch (e) { bk.log = "导入异常: " + e.message; }
    bk.busy = false;
  }

  async function checkBank() {
    const b = curBank();
    if (!bk.ace) { ElementPlus.ElMessage.warning("先上传 Excel 生成批次"); return; }
    bk.busy = true;
    try {
      await api.proc("PKG_派遣批量导入." + b.check, { ace100: String(bk.ace) });
      const bb = await api.wb("/batch/" + bk.ace);
      bk.rows = bb.rows || [];
      const bad = bk.rows.filter(r => r.ace10a === "2").length;
      bk.log = "校验完成: 共 " + bk.rows.length + " 行" + (bad ? `, 拒绝 ${bad} 行(见原因列)` : ", 全部通过");
    } catch (e) { bk.log = "校验失败: " + e.message; }
    bk.busy = false;
  }

  async function commitBank() {
    const b = curBank();
    if (!bk.ace) { ElementPlus.ElMessage.warning("先上传并校验"); return; }
    try { await ElMessageBox.confirm(`提交批次 ${bk.ace} 到 TB97(${b.label})? 提交后可在"最近到账"中核销。`, "到账提交"); }
    catch (e) { return; }
    bk.busy = true;
    try {
      await api.proc("PKG_派遣批量导入." + b.commit,
        { ace100: String(bk.ace), aae011: api.store.oper.aae011 });
      const bb = await api.wb("/batch/" + bk.ace);
      bk.rows = bb.rows || [];
      bk.log = (bk.log || "") + "\n提交完成 → TB97(atc900=-1 待核销哨兵), 请在到账核销流程认领";
      ElementPlus.ElMessage.success("提交完成");
      loadLists();
    } catch (e) { bk.log = (bk.log || "") + "\n提交失败: " + e.message; }
    bk.busy = false;
  }

  async function downloadBankTemplate() {
    const b = curBank();
    try {
      const resp = await fetch("/import/sys/" + b.id + "/template");
      if (!resp.ok) { ElementPlus.ElMessage.error("模板未配置: " + b.id); return; }
      const blob = await resp.blob();
      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = b.sheet + "-导入模板.xlsx";
      a.click();
      URL.revokeObjectURL(a.href);
    } catch (e) { ElementPlus.ElMessage.error("模板下载失败: " + e.message); }
  }

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
        <el-form-item><wb-dsf-tip menu="收款(到账核销)" page="td/l/y 银行到账导入(工行/建行/吉林银行)" area="到账批次 TB97_BP" btn="导入" note="银行回单 Excel→TB97_BP→TB97, 新系统工作台化">
          <el-button type="primary" :loading="arr.busy" @click="arriveRun">导入 TB97</el-button>
        </wb-dsf-tip></el-form-item>
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

      <h4>银行回单 Excel 批量(东软 td/l/y 三银行弹窗)</h4>
      <el-form inline>
        <el-form-item label="银行">
          <el-radio-group v-model="bk.key" @change="bk.file = null; bk.ace = null; bk.rows = []; bk.log = ''">
            <el-radio-button v-for="b in banks" :key="b.key" :value="b.key">{{ b.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <wb-dsf-tip menu="收款(到账核销)" page="td/l/y 银行到账导入(工行/建行/吉林银行)" area="到账批次" btn="模板下载"
            :note="'东软: 银行弹窗[模板下载]; 新系统由 sys_import_mapping 反向生成(' + curBank().sheet + ')'">
            <el-button size="small" @click="downloadBankTemplate">下载模板</el-button>
          </wb-dsf-tip>
          <input type="file" accept=".xls,.xlsx" style="display:none" id="bank-file" @change="onBankFile"/>
          <el-button size="small" type="primary" plain @click="pickBankFile">选择 Excel</el-button>
          <el-button size="small" plain :disabled="!bk.file || bk.busy" @click="uploadBank">上传导入</el-button>
          <wb-dsf-tip menu="收款(到账核销)" page="td/l/y 银行到账导入" area="到账批次" btn="校验"
            :note="curBank().check + ' — 日期/金额格式逐行检查, 结果写回批次行'">
            <el-button size="small" type="warning" plain :disabled="!bk.ace || bk.busy" @click="checkBank">校验</el-button>
          </wb-dsf-tip>
          <wb-dsf-tip menu="收款(到账核销)" page="td/l/y 银行到账导入" area="到账批次" btn="提交"
            :note="curBank().commit + ' — 批次行落 TB97(atc900=-1 待核销哨兵), 之后到账核销认领'">
            <el-button size="small" type="danger" plain :disabled="!bk.ace || bk.busy" @click="commitBank">提交</el-button>
          </wb-dsf-tip>
          <span style="margin-left:8px" v-if="bk.file">{{ bk.file.name }}</span>
        </el-form-item>
      </el-form>
      <div class="kv" v-if="curBank().note">{{ curBank().note }}</div>
      <pre class="log" v-if="bk.log">{{ bk.log }}</pre>
      <el-table v-if="bk.rows.length" :data="bk.rows" size="small" border style="margin-bottom:8px">
        <el-table-column prop="abp001" label="日期" width="110"/>
        <el-table-column prop="abp002" label="金额" width="100"/>
        <el-table-column prop="abp003" label="对方户名" min-width="180"/>
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
        <el-form-item><wb-dsf-tip menu="收款(开票)" page="td/l/b 发票导入" area="发票批次 TB99_BP" btn="导入" note="发票 Excel→TB99_BP→TB99, 8 类费项目金额合计生成 atb993 项目编码串">
          <el-button type="primary" :loading="inv.busy" @click="invoiceRun">导入 TB99</el-button>
        </wb-dsf-tip></el-form-item>
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
        <el-table-column label="日期" width="100">
          <template #default="{ row }">{{ api.fmtD(row.atb974) }}</template>
        </el-table-column>
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
      return { arr, inv, arrRows, invRows, arrivals, invoices, arriveRun, invoiceRun,
        bk, banks: BANKS, curBank, onBankFile, pickBankFile,
        uploadBank, checkBank, commitBank, downloadBankTemplate, api };
    },
  };
})();
