/* 新签审核台: 待审队列(TB91 类型 20/标志 2) → 通过(doXinqCommit: atb90r=3/atb912=3)
   或 驳回(doXinqCancle: 删 TC90+TB91)。行编辑保存模型, 整行提交。
   端到端参照 e2e_business.py 步 3。 */
(() => {
  const { ref, reactive, onMounted } = Vue;
  const { api } = WB;
  const { ElMessage, ElMessageBox } = ElementPlus;

  const queue = ref([]); const loading = ref(false);
  const detail = reactive({ row: null });
  const htCodes = ref([]);
  const htMap = Vue.computed(() =>
    Object.fromEntries(htCodes.value.map(c => [c.split("-")[0], c])));

  async function loadQueue() {
    loading.value = true;
    try { queue.value = (await api.wb("/audit-queue")) || []; }
    catch (e) { ElMessage.error(e.message); }
    loading.value = false;
  }

  function openRow(row) { detail.row = row; }

  async function commit(row) {
    try {
      await api.interaction("REQ-tdbf__dialog_xinqcl-commit", {
        tc90: { atc900: row.atc900, atb90r: "3", ...api.operBae() },
        tb91: { atb910: row.atb910, atb91z: row.atc900, atb911: "20", atb912: "3",
                bae036: api.nowLocal(), ...api.operBae() },
      });
      ElMessage.success("审核通过, 合同已生效(atb90r=3)");
      detail.row = null;
      loadQueue();
    } catch (e) { ElMessage.error("审核失败: " + e.message); }
  }

  async function cancle(row) {
    try {
      await ElMessageBox.confirm(
        `驳回将删除合同 ${row.atc900}(${row.aac003}) 及其审核流水, 不可恢复。确认?`,
        "驳回新签", { type: "warning", confirmButtonText: "驳回", cancelButtonText: "取消" });
    } catch (e) { return; }
    try {
      await api.interaction("REQ-tdbf_dialog_xinqcl-cancle",
        { tc90: { atc900: row.atc900 }, tb91: { atb910: row.atb910 } });
      ElMessage.success("已驳回, TC90/TB91 已删");
      detail.row = null;
      loadQueue();
    } catch (e) { ElMessage.error("驳回失败: " + e.message); }
  }

  WB.pages.audit = {
    template: `
    <div class="page">
      <el-form inline>
        <el-form-item>
          <el-button :loading="loading" @click="loadQueue">刷新队列</el-button>
          <span class="kv">  待审 {{ queue.length }} 条(封顶 200)</span>
        </el-form-item>
      </el-form>
      <el-table :data="queue" size="small" border max-height="420" highlight-current-row
                @row-click="openRow">
        <el-table-column prop="atc900" label="合同号" width="90"/>
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aac002" label="身份证" width="170"/>
        <el-table-column label="合同类别" width="120">
          <template #default="{ row }">{{ htMap[row.atc90a] || row.atc90a }}</template>
        </el-table-column>
        <el-table-column prop="atc90c" label="开始" width="100"/>
        <el-table-column prop="atc90d" label="终止" width="100"/>
        <el-table-column prop="atc905" label="合同工资" width="90"/>
        <el-table-column prop="aab004" label="单位" min-width="150" show-overflow-tooltip/>
        <el-table-column label="导入时间" width="150">
          <template #default="{ row }">{{ api.fmtDT(row.aae036) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button size="small" type="success" @click.stop="commit(row)">通过</el-button>
            <el-button size="small" type="danger" plain @click.stop="cancle(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template v-if="detail.row">
        <h4>选中合同 {{ detail.row.atc900 }}(当前 atb90r={{ detail.row.atb90r }})</h4>
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="atc900">{{ detail.row.atc900 }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detail.row.aac003 }}</el-descriptions-item>
          <el-descriptions-item label="身份证">{{ detail.row.aac002 }}</el-descriptions-item>
          <el-descriptions-item label="合同工资">{{ detail.row.atc905 }}</el-descriptions-item>
          <el-descriptions-item label="合同类别">{{ detail.row.atc90a }}</el-descriptions-item>
          <el-descriptions-item label="开始">{{ detail.row.atc90c }}</el-descriptions-item>
          <el-descriptions-item label="终止">{{ detail.row.atc90d }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ detail.row.aab004 }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <div class="kv">通过 = doXinqCommit(整行 atb90r='3'/atb912='3' 提交, 审核人=当前经办人);
        驳回 = doXinqCancle(删 TC90+TB91)</div>
    </div>`,
    setup() {
      onMounted(async () => {
        loadQueue();
        htCodes.value = await api.codes("ATC90A");
      });
      return { queue, loading, detail, htMap, loadQueue, openRow, commit, cancle, api };
    },
  };
})();
