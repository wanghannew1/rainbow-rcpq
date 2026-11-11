/* 五险一金报盘: 从员工合同推导社保/医保 报增/报减名册, 列规范=东软名册网格
   (社保 tc9wStore_grid td/h/y; 医保 tc9w1Store_grid td/c/ay), 导出按局方模板
   (resources/baopan, POI 底稿生成): 社保→参保登记/停保 .xlsx; 医保→新参保/减员
   申报 .xls 按医保参保属地拆省(2299)/市打包 .zip。
   旧 TC9W/TC9W1 工作表新库为空(报增导入包生产即 INVALID 未移植), 故由合同变化推导。 */
(() => {
  const { reactive, ref, onMounted } = Vue;
  const { api } = WB;

  const rp = reactive({ kind: "social", flow: "add", ym: "202611", aab001: null,
    log: "", busy: false });
  const cols = ref([]); const rows = ref([]); const units = ref([]);

  const kindName = () => rp.kind === "social" ? "社保" : "医保";
  const flowName = () => rp.flow === "add" ? "报增名册" : "报减名册";

  onMounted(async () => {
    try { units.value = (await api.wb("/units")) || []; } catch (e) { /* 保底 */ }
  });

  async function load() {
    if (!rp.ym) { ElementPlus.ElMessage.warning("选年月"); return; }
    rp.busy = true; rp.log = "";
    try {
      const q = `kind=${rp.kind}&flow=${rp.flow}&ym=${rp.ym}` +
        (rp.aab001 ? `&aab001=${rp.aab001}` : "");
      const j = await api.wb("/roster?" + q);
      cols.value = j.columns || []; rows.value = j.rows || [];
      rp.log = `${kindName()}${flowName()} ${rp.ym}: 共 ${rows.value.length} 人` +
        (rows.value.length ? "" : "(该月无此类型变动)");
    } catch (e) { ElementPlus.ElMessage.error(e.message); }
    rp.busy = false;
  }

  function exportXls() {
    if (!rows.value.length) { ElementPlus.ElMessage.warning("先查询出名册再导出"); return; }
    const q = `kind=${rp.kind}&flow=${rp.flow}&ym=${rp.ym}` +
      (rp.aab001 ? `&aab001=${rp.aab001}` : "");
    window.open("/wb/roster-export?" + q);
  }

  WB.pages.social = {
    template: `
    <div class="page">
      <el-form inline>
        <el-form-item label="险种">
          <el-select v-model="rp.kind" style="width:110px" @change="rows = []; cols = []">
            <el-option value="social" label="社保"/>
            <el-option value="medical" label="医保"/>
          </el-select>
        </el-form-item>
        <el-form-item label="名册">
          <el-select v-model="rp.flow" style="width:110px" @change="rows = []; cols = []">
            <el-option value="add" label="报增"/>
            <el-option value="cut" label="报减"/>
          </el-select>
        </el-form-item>
        <el-form-item label="年月">
          <el-date-picker v-model="rp.ym" type="month" value-format="YYYYMM"
                          placeholder="202611" style="width:120px"/>
        </el-form-item>
        <el-form-item label="单位(可空)">
          <el-select v-model="rp.aab001" filterable clearable placeholder="全部单位"
                     style="width:260px">
            <el-option v-for="u in units" :key="u.aab001" :value="u.aab001"
                       :label="u.aab001 + ' ' + u.aab004"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="rp.busy" @click="load">生成名册</el-button>
          <el-button type="success" @click="exportXls">导出报盘文件</el-button>
        </el-form-item>
      </el-form>
      <div v-if="rp.log" class="kv">{{ rp.log }}</div>
      <el-table v-if="cols.length" :data="rows" size="small" border max-height="520">
        <el-table-column v-for="c in cols" :key="c.key" :prop="c.key"
                         :label="c.label" min-width="90" show-overflow-tooltip/>
      </el-table>
      <div class="kv">增员=当月合同开始(atc90c)且生效; 减员=当月合同终止(atc90i)。
        导出=局方模板底稿生成: 社保增员→企业职工批量参保登记.xlsx, 社保减员→单位职工停保.xlsx;
        医保按参保属地拆省(2299)/市两文件→打包 zip(001批量新参保 / 批量减员申报.xls)。
        单位过滤时社保增员模板回填单位编号/名称。个人编号由局端分配, 报盘中留空。</div>
    </div>`,
    setup() { return { rp, cols, rows, units, load, exportXls }; },
  };
})();
