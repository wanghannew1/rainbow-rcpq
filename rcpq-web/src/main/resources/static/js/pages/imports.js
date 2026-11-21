/* 合同批量导入(东软 tdb/p 四弹窗工作台化): 模板下载 → Excel 上传(/import/sys 通用引擎落批次)
   → 校验(/proc) → 批次预览 → 提交 → 转对应后手(新签→审核台; 续签/更新/新增保险→直接落账)。
   四路模板: 新签 tc90_bp(54 列=员工合同及参保信息报盘表) / 更新 tc90_bp2(15 列) /
   续签 tc90_bp3(9 列) / 新增保险 tc90_bp4(13 列)。
   东软原版: tdbp_cont.jsp 同页五弹窗(新签/终止/更新/续签/新增保险); 终止批量走 change 页
   (/wb/terminate-batch 表单路线), 本页补齐其余四路的 Excel 面。
   单位/协议/结算单元与费用开始年月(aae002/aaa203)不在模板列内 —— 东软即弹窗公共参数,
   经 parameters 透传写入批次行; 费用年月恒填下月(AA95 时限守卫: 每月 5 号后当月不可新办)。 */
(() => {
  const { reactive, ref, computed, watch, onMounted } = Vue;
  const { api } = WB;
  const PKG = "PKG_派遣批量导入.";

  const TABS = [
    { key: "bp", id: "tc90_bp", label: "新签导入", sheet: "人才派遣员工合同报盘表",
      cols: 54, head: 8, check: "PRC_员工导入校验", commit: "PRC_员工导入提交",
      page: "td/b/p 派遣员工管理·新签导入弹窗",
      note: "Excel 带 人/合同/参保 54 列; 模板前 8 行为表头(列头在第 8 行); 提交后生成 AC01/TC90/TC90_LIST/TB91(20·2 待审), 转新签审核台" },
    { key: "bp2", id: "tc90_bp2", label: "更新导入", sheet: "人才派遣员工合同更新表",
      cols: 15, head: 4, check: "PRC_员工更新校验", commit: "PRC_员工更新提交",
      page: "td/b/p 派遣员工管理·更新导入弹窗",
      note: "按【身份证+姓名+结算单元】匹配生效合同, 只更新填了的列(联系电话/员工号/部门/岗位/职业病/服务费/工会/银行账号等); 提交直接落账 TC90+TC90_LIST" },
    { key: "bp3", id: "tc90_bp3", label: "续签导入", sheet: "人才派遣员工合同续签表",
      cols: 9, head: 4, check: "PRC_员工续签校验", commit: "PRC_员工续签提交",
      page: "td/b/p 派遣员工管理·续签导入弹窗",
      note: "按【身份证+姓名】匹配生效合同(atb90r='3'); 提交写签订历史 TC90_QDLS 并置 atc90f='2'(已续签)" },
    { key: "bp4", id: "tc90_bp4", label: "新增保险导入", sheet: "人才派遣员工新增保险表",
      cols: 13, head: 4, check: "PRC_新增保险校验", commit: "PRC_新增保险提交",
      page: "td/b/p 派遣员工管理·新增保险导入弹窗",
      note: "给在保合同新增险种: 填各险种参保属地/参保年月/缴费基数; 匹配【身份证+姓名】有效合同" },
  ];

  const st = reactive({
    tab: "bp", aab001: null, atb900: null, atb930: null,
    baseErr: "", busy: false,
  });
  // 新签弹窗参数(东软弹窗字段, 不在 54 列模板内 → parameters 透传): 民族/人员类别等默认值
  // 与 /wb/hire-batch 单人路线一致; 联系电话+证件有效期是校验硬性必填, 服务费随单位协议填写
  const bp = reactive({
    aac005: "01-汉族", aac011: "", aac012: "91-其他人员", aac009: "10-城镇失业",
    aac013: "1-城镇新增", aae00p: "1-派遣", aae005: "", atc90ax: "", atc902: "",
    atb90a: null, atb90b: "0",
  });
  const ygCodes = ref([]);   // AAE00P 人员类别码表(同 hire 页下拉)
  // 每路独立的批次运行态: {file, ace, baz002, overlong, log, rows, table}
  const runs = reactive({});
  TABS.forEach(t => { runs[t.key] = reactive({ file: null, ace: null, baz002: null,
    overlong: [], log: "", rows: [], table: "", busy: false }); });

  const units = ref([]); const agreementsAll = ref([]); const settleUnits = ref([]);
  const agreements = computed(() => st.aab001
    ? agreementsAll.value.filter(a => a.aab001 === st.aab001) : agreementsAll.value);
  const settleOptions = computed(() => st.aab001
    ? settleUnits.value.filter(s => s.aab001 === st.aab001) : settleUnits.value);
  const cur = () => TABS.find(x => x.key === st.tab) || TABS[0];

  // 三向联动(同 hire 页): 单位变化 → 协议/结算单元过滤 + 失配清空 + 唯一自动选中
  watch(() => st.aab001, () => {
    const ss = settleOptions.value;
    if (!st.atb930 || !ss.some(s => s.atb930 === st.atb930)) {
      st.atb930 = ss.length === 1 ? ss[0].atb930 : null;
    }
    const ags = agreements.value;
    if (!st.atb900 || !ags.some(a => a.atb900 === st.atb900)) {
      st.atb900 = ags.length === 1 ? ags[0].atb900 : null;
    }
  });
  function onAgreementChange(v) {
    const a = agreementsAll.value.find(x => x.atb900 === v);
    if (a && st.aab001 !== a.aab001) st.aab001 = a.aab001;
  }
  function onSettleChange(v) {
    const s = settleUnits.value.find(x => x.atb930 === v);
    if (s) {
      if (s.atb900 && st.atb900 !== s.atb900) st.atb900 = s.atb900;
      if (st.aab001 !== s.aab001) st.aab001 = s.aab001;
    }
  }

  // 费用开始年月: AA95 时限守卫要求下月起(与 e2e 同法, 动态算不硬编码)
  function nextMonth() {
    const d = new Date();
    const y = d.getFullYear() + (d.getMonth() === 11 ? 1 : 0);
    const m = (d.getMonth() + 1) % 12 + 1;
    return `${y}${String(m).padStart(2, "0")}`;
  }

  async function loadBase() {
    st.baseErr = "";
    try {
      units.value = (await api.wb("/units")) || [];
      settleUnits.value = (await api.wb("/settle-units")) || [];
      agreementsAll.value = (await api.wb("/agreements").catch(() => [])) || [];
      ygCodes.value = await api.codes("AAE00P");
      if (!units.value.length) st.baseErr = "单位清单为空(接口通但无数据)";
    } catch (e) { st.baseErr = "基础数据加载失败: " + e.message; }
  }

  function onFile(t, ev) { runs[t.key].file = ev.target.files[0] || null; }

  function pickFile(t) { document.getElementById("file_" + t.key)?.click(); }

  function summary(t) {
    const rows = runs[t.key].rows || [];
    const bad = rows.filter(r => r.ace10a === "2").length;
    return `共 ${rows.length} 行` + (bad ? `, 拒绝 ${bad} 行` : ", 全部可提交");
  }

  async function loadBatch(t) {
    const r = runs[t.key];
    if (!r.ace) return;
    try {
      const b = await api.wb("/batch/" + r.ace);
      r.rows = b.rows || []; r.table = b.table || "";
    } catch (e) { r.log += "批次查询失败: " + e.message + "\n"; }
  }

  // 东软弹窗[导入]键 → 通用导入引擎: POST /import/sys/{模板} (file + parameters 透传列)
  async function upload(t) {
    const r = runs[t.key];
    if (!r.file) { ElementPlus.ElMessage.warning("先选择模板 Excel"); return; }
    if (!st.aab001 || !st.atb900 || !st.atb930) {
      ElementPlus.ElMessage.warning("单位 / 派遣协议 / 结算单元都要选(东软弹窗公共参数, 透传批次行)"); return;
    }
    if (t.key === "bp" && (!bp.aae005 || !bp.atc90ax || !bp.aae00p)) {
      ElementPlus.ElMessage.warning("新签: 人员类别 / 联系电话 / 身份证有效期必填(校验硬性要求)"); return;
    }
    const unit = units.value.find(u => u.aab001 === st.aab001);
    const params = {
      aab001: st.aab001, aab004: unit ? unit.aab004 : "单位名",
      atb900: st.atb900, atb930: st.atb930,
      aae002: nextMonth(), aaa203: nextMonth(),   // 费用开始年月: 恒下月(AA95 守卫)
      aae011: api.store.oper.aae011,
    };
    if (t.key === "bp") {
      Object.assign(params, { aac005: bp.aac005, aac011: bp.aac011 || null,
        aac012: bp.aac012, aac009: bp.aac009, aac013: bp.aac013, aae00p: bp.aae00p,
        aae005: bp.aae005, atc90ax: bp.atc90ax, atc902: bp.atc902 || null,
        atb90a: bp.atb90a == null ? null : String(bp.atb90a), atb90b: String(bp.atb90b ?? "0") });
    }
    r.busy = true; r.log = ""; r.rows = []; r.overlong = [];
    try {
      const fd = new FormData();
      fd.append("file", r.file);
      fd.append("parameters", JSON.stringify(params));
      const resp = await fetch("/import/sys/" + t.id, { method: "POST", body: fd });
      const j = await resp.json();
      if (!j.succeed) {
        r.log = "导入失败: " + (j.msg || resp.status);
        ElementPlus.ElMessage.error(j.msg || "导入失败");
      } else {
        r.ace = (j.ce10 || [])[0]; r.baz002 = j.baz002;
        r.overlong = j.errors || [];
        r.log = `导入成功: 批次 ${r.ace}, 流水号 ${r.baz002}`;
        if (r.overlong.length) r.log += `, 超长列 ${r.overlong.length} 处(仍已入库, 前 5: ${r.overlong.slice(0, 5).join(" / ")})`;
        await loadBatch(t);
        r.log += "\n" + summary(t) + " — 先点【校验】, 全过再【提交】";
      }
    } catch (e) { r.log = "导入异常: " + e.message; }
    r.busy = false;
  }

  // 东软弹窗[校验]键 → /proc 校验过程(逐行规则检查, 结果写回批次行 ace10a/ace10b)
  async function check(t) {
    const r = runs[t.key];
    if (!r.ace) { ElementPlus.ElMessage.warning("先上传 Excel 生成批次"); return; }
    r.busy = true;
    try {
      await api.proc(PKG + t.check, { ace100: String(r.ace), aae011: api.store.oper.aae011 });
      await loadBatch(t);
      r.log = "校验完成: " + summary(t) + (runs[t.key].rows.some(x => x.ace10a === "2")
        ? " — 有拒绝行(见表原因列), 修正后重新导入" : " — 全部通过, 可提交");
    } catch (e) { r.log = "校验失败: " + e.message; }
    r.busy = false;
  }

  // 东软弹窗[提交]键 → /proc 提交过程(有拒绝行时提交会跳过拒绝行/全跳, 先校验再提交)
  async function commit(t) {
    const r = runs[t.key];
    if (!r.ace) { ElementPlus.ElMessage.warning("先上传并校验"); return; }
    try {
      await ElMessageBox.confirm(
        `提交批次 ${r.ace}(${summary(t)})?\n提交后: ${t.key === "bp" ? "生成 AC01/TC90 待审行, 转新签审核台" : "直接落账合同表"}`,
        "导入提交");
    } catch (e) { return; }
    r.busy = true;
    try {
      await api.proc(PKG + t.commit, { ace100: String(r.ace), aae011: api.store.oper.aae011 });
      await loadBatch(t);
      r.log = (r.log || "") + "\n提交完成: " + summary(t) +
        (t.key === "bp" ? " — 转【新签审核台】页签复核" : " — 合同已落账, 可在合同查询面核对");
      ElementPlus.ElMessage.success("提交完成");
    } catch (e) { r.log = (r.log || "") + "\n提交失败: " + e.message; }
    r.busy = false;
  }

  // 东软弹窗[模板下载]键: 新系统由 sys_import_mapping 反向生成(旧系统为静态 .xls)
  async function downloadTemplate(t) {
    try {
      const resp = await fetch("/import/sys/" + t.id + "/template");
      if (!resp.ok) { ElementPlus.ElMessage.error("模板未配置: " + t.id); return; }
      const blob = await resp.blob();
      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = t.sheet + "-导入模板.xlsx";
      a.click();
      URL.revokeObjectURL(a.href);
    } catch (e) { ElementPlus.ElMessage.error("模板下载失败: " + e.message); }
  }

  WB.pages.imports = {
    template: `
    <div class="page">
      <h4>① 单位 / 协议 / 结算单元 <span style="font-weight:400;font-size:12px;color:#909399">(东软弹窗公共参数, 透传批次行; 费用开始年月自动填下月)</span></h4>
      <div v-if="st.baseErr" class="bad">{{ st.baseErr }}
        <el-button size="small" type="warning" plain @click="loadBase">重试</el-button></div>
      <el-form inline label-width="110px">
        <el-form-item label="单位">
          <el-select v-model="st.aab001" filterable placeholder="选单位" style="width:280px">
            <el-option v-for="u in units" :key="u.aab001" :value="u.aab001"
                       :label="u.aab001 + ' ' + u.aab004"/>
          </el-select>
        </el-form-item>
        <el-form-item label="派遣协议">
          <el-select v-model="st.atb900" filterable placeholder="选协议" style="width:200px" @change="onAgreementChange">
            <el-option v-for="a in agreements" :key="a.atb900" :value="a.atb900"
                       :label="a.atb900 + ' · ' + (a.aab004 || '')"/>
          </el-select>
        </el-form-item>
        <el-form-item label="结算单元">
          <el-select v-model="st.atb930" filterable placeholder="选结算单元" style="width:220px" @change="onSettleChange">
            <el-option v-for="s in settleOptions" :key="s.atb930" :value="s.atb930"
                       :label="s.atb930 + ' ' + s.atb931"/>
          </el-select>
        </el-form-item>
      </el-form>

      <template v-if="st.tab === 'bp'">
        <h4 style="margin-top:2px">② 新签弹窗参数 <span style="font-weight:400;font-size:12px;color:#909399">(东软新签弹窗字段, 模板 54 列之外由此透传; 联系电话/身份证有效期必填)</span></h4>
        <el-form inline label-width="140px">
          <el-form-item label="人员类别">
            <el-select v-model="bp.aae00p" style="width:130px">
              <el-option v-for="c in ygCodes" :key="c" :value="c" :label="c"/>
            </el-select></el-form-item>
          <el-form-item label="民族"><el-input v-model="bp.aac005" style="width:130px"/></el-form-item>
          <el-form-item label="文化程度"><el-input v-model="bp.aac011" placeholder="可空" style="width:130px"/></el-form-item>
          <el-form-item label="人员类别"><el-input v-model="bp.aac012" style="width:150px"/></el-form-item>
          <el-form-item label="用工性质"><el-input v-model="bp.aac009" style="width:150px"/></el-form-item>
          <el-form-item label="用工形态"><el-input v-model="bp.aac013" style="width:150px"/></el-form-item>
          <el-form-item label="联系电话"><el-input v-model="bp.aae005" placeholder="必填" style="width:150px"/></el-form-item>
          <el-form-item label="身份证有效期">
            <el-date-picker v-model="bp.atc90ax" type="date" value-format="YYYY-MM-DD"
                            placeholder="必填" style="width:150px"/></el-form-item>
          <el-form-item label="户籍地址"><el-input v-model="bp.atc902" placeholder="可空" style="width:220px"/></el-form-item>
          <el-form-item label="单位服务费月标准">
            <el-input-number v-model="bp.atb90a" :min="0" :precision="2" :controls="false" style="width:130px"/></el-form-item>
          <el-form-item label="个人服务费月标准">
            <el-input-number v-model="bp.atb90b" :min="0" :precision="2" :controls="false" style="width:130px"/></el-form-item>
        </el-form>
      </template>

      <el-tabs v-model="st.tab">
        <el-tab-pane v-for="t in tabs" :key="t.key" :name="t.key"
                     :label="t.label + '(' + t.cols + '列)'">
          <div style="margin:6px 0">
            <wb-dsf-tip :menu="'派遣员工管理'" :page="t.page" area="导入弹窗" btn="模板下载"
              :note="'东软: 弹窗[模板下载]键下发静态 .xls; 新系统由 sys_import_mapping 配置反向生成 xlsx, 列头=映射中文名, 第 ' + t.head + ' 行为列头'">
              <el-button size="small" @click="downloadTemplate(t)">下载模板</el-button>
            </wb-dsf-tip>
            <wb-dsf-tip :menu="'派遣员工管理'" :page="t.page" area="导入弹窗" btn="上传导入"
              note="东软: 弹窗[导入]键 → 通用导入引擎(ria_pojo doImport); 新系统 POST /import/sys/{模板号}, 批次号 CE10.ACE100, 批次行入 BP 表">
              <input type="file" accept=".xls,.xlsx" style="display:none"
                     :id="'file_' + t.key" @change="ev => onFile(t, ev)"/>
              <el-button size="small" type="primary" plain @click="pickFile(t)">选择 Excel</el-button>
            </wb-dsf-tip>
            <el-button size="small" plain :disabled="!runs[t.key].file || runs[t.key].busy"
                       @click="upload(t)">上传导入</el-button>
            <wb-dsf-tip :menu="'派遣员工管理'" :page="t.page" area="导入弹窗" btn="校验"
              :note="t.check + ' — 逐行身份证解析/代码项比对/有效合同冲突等, 结果写回批次行 ace10a(3 过/2 拒+ace10b 原因)'">
              <el-button size="small" type="warning" plain :disabled="!runs[t.key].ace || runs[t.key].busy"
                         @click="check(t)">校验</el-button>
            </wb-dsf-tip>
            <wb-dsf-tip :menu="'派遣员工管理'" :page="t.page" area="导入弹窗" btn="提交"
              :note="t.commit + ' — ' + t.note">
              <el-button size="small" type="danger" plain :disabled="!runs[t.key].ace || runs[t.key].busy"
                         @click="commit(t)">提交</el-button>
            </wb-dsf-tip>
            <span style="margin-left:8px" v-if="runs[t.key].file">{{ runs[t.key].file.name }}</span>
          </div>
          <div class="kv">{{ t.note }}</div>
          <pre class="log" v-if="runs[t.key].log">{{ runs[t.key].log }}</pre>
          <template v-if="runs[t.key].rows.length">
            <h4>批次明细 ({{ runs[t.key].table }} · ace100={{ runs[t.key].ace }})</h4>
            <el-table :data="runs[t.key].rows" size="small" border max-height="300">
              <el-table-column prop="aac003" label="姓名" width="90"/>
              <el-table-column prop="aac002" label="身份证" width="170"/>
              <el-table-column v-if="t.key === 'bp'" prop="atc90a" label="合同类别" width="110"/>
              <el-table-column v-if="t.key === 'bp'" prop="atc90c" label="开始" width="100"/>
              <el-table-column v-if="t.key === 'bp' || t.key === 'bp3'" prop="atc90d" label="终止" width="100"/>
              <el-table-column v-if="t.key === 'bp'" prop="atc905" label="合同工资" width="90"/>
              <el-table-column v-if="t.key === 'bp2'" prop="aae005" label="联系电话" width="120"/>
              <el-table-column v-if="t.key === 'bp3'" prop="atc90a" label="新合同类别" width="110"/>
              <el-table-column label="校验" width="80">
                <template #default="{ row }">
                  <el-tag :type="api.batchType(row.ace10a)" size="small">{{ api.batchLabel(row.ace10a) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="ace10b" label="原因" min-width="200" show-overflow-tooltip/>
            </el-table>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>`,
    setup() {
      onMounted(loadBase);
      return { st, runs, tabs: TABS, bp, ygCodes, units, agreements, settleOptions,
        onAgreementChange, onSettleChange, loadBase,
        onFile, pickFile, upload, check, commit, downloadTemplate, api };
    },
  };
})();
