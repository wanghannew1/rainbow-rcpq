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

  /** 东软对应位置漂浮提示(全站标准, 2026-11-11 定调): 每个操作按键声明旧系统
     菜单/模块、页面(码+名称)、板块、按键名, 悬停才显示, 不占界面。
     用法: <wb-dsf-tip menu=".." page=".." area=".." btn=".." note=".."><el-button../></wb-dsf-tip>
     note 写新旧差异(如"新系统新增能力"/"沿用旧系统报错行为"), 东软无对应的写明。 */
  WB.ui.dsfTip = {
    name: "WbDsfTip",
    props: { menu: String, page: String, area: String, btn: String, note: String },
    template: `
    <el-tooltip placement="bottom-start" effect="light" :hide-after="0">
      <template #content>
        <div style="max-width:380px;font-size:12px;line-height:1.8">
          <div style="font-weight:600">东软系统对应位置</div>
          <div v-if="menu">菜单/模块：{{ menu }}</div>
          <div v-if="page">页面：{{ page }}</div>
          <div v-if="area">板块：{{ area }}</div>
          <div v-if="btn">按键：{{ btn }}</div>
          <div v-if="note" style="color:#909399;margin-top:2px">{{ note }}</div>
        </div>
      </template>
      <span style="display:inline-block"><slot/></span>
    </el-tooltip>`,
  };
})();
