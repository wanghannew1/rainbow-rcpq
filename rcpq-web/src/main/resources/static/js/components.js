/* 可复用 UI 组件(对象定义, 由 index.html 启动脚本注册为全局组件)。 */
(() => {
  const { ref, onMounted } = Vue;
  const { api } = WB;

  /** 合同选择器: 身份证/姓名/合同号搜索 → 表格选择, @pick 回传整行 */
  WB.ui.contractPick = {
    name: "WbContractPick",
    emits: ["pick"],
    template: `
    <div>
      <el-form inline>
        <el-form-item label="搜合同">
          <el-input v-model="q" placeholder="身份证 / 姓名 / 合同号" style="width:220px"
                    @keyup.enter="search"/>
          <el-button type="primary" plain :loading="busy" @click="search">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table v-if="rows.length" :data="rows" size="small" border max-height="260">
        <el-table-column prop="atc900" label="合同号" width="90"/>
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aac002" label="身份证" width="170"/>
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="api.contractStateType(row.atb90r)" size="small">{{ api.contractState(row.atb90r) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="atc90a" label="类别" width="110"/>
        <el-table-column prop="atc90c" label="开始" width="100"/>
        <el-table-column prop="atc90d" label="终止" width="100"/>
        <el-table-column prop="aab004" label="单位" min-width="160" show-overflow-tooltip/>
        <el-table-column label="" width="70">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$emit('pick', row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="searched && !rows.length" class="kv">无匹配合同</div>
    </div>`,
    setup() {
      const q = ref(""); const rows = ref([]); const busy = ref(false); const searched = ref(false);
      async function search() {
        if (!q.value.trim()) { ElementPlus.ElMessage.warning("输入身份证/姓名/合同号"); return; }
        busy.value = true;
        try { rows.value = (await api.wb("/contract-search?q=" + encodeURIComponent(q.value.trim()))) || []; }
        catch (e) { ElementPlus.ElMessage.error(e.message); }
        busy.value = false; searched.value = true;
      }
      return { q, rows, busy, searched, search, api };
    },
  };
})();
