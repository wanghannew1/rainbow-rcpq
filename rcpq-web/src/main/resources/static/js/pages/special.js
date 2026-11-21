/* 专项登记导入(东软"专项(按需)"五弹窗工作台化): 模板下载 → Excel 上传(/import/sys 落批次)
   → 校验(/proc) → 批次预览 → 提交。五路: 生育补贴 tc04_bp(7列) / 意外险 tc05_bp(9列) /
   特殊情况 tc06_bp(22列) / 个人补退 tc07_bp(4列) / 经济补偿金 td04_bp(10列)。
   与 imports 页(合同批量导入)同构但无单位/协议参数——专项匹配键在 Excel 列内
   (生育按医保编号/意外险按结算单元+保单号/补退与补偿金按身份证+姓名)。
   过程契约: 校验=ace100(+特殊情况另需 aae002 异动年月; 补退/意外险另需 aae011),
   提交=ace100+aae011。 */
(() => {
  const { reactive, ref, onMounted } = Vue;
  const { api } = WB;
  const PKG = "PKG_派遣批量导入.";

  const TABS = [
    { key: "tc04", id: "tc04_bp", label: "生育补贴", sheet: "生育津贴回盘",
      cols: 7, head: 5, check: "PRC_生育补贴校验", commit: "PRC_生育补贴提交",
      page: "td/c/bf 生育补贴(22)",
      note: "按医保个人编号匹配合同+费款所属期查重; 列头在第 5 行" },
    { key: "tc05", id: "tc05_bp", label: "意外险", sheet: "意外险",
      cols: 9, head: 1, check: "PRC_意外险导入校验", commit: "PRC_意外险导入提交",
      page: "td/c/bi 意外险(23)",
      note: "按结算单元名称+保单号(TB9B)匹配, 已结算月份拒绝; 列头在第 1 行" },
    { key: "tc06", id: "tc06_bp", label: "特殊情况", sheet: "特殊情况说明报盘",
      cols: 22, head: 1, check: "PRC_特殊情况导入校验", commit: "PRC_特殊情况导入提交",
      page: "td/c/aw 特殊情况(21)",
      note: "六险种异动情况登记(异动原因/截止年月/调整后基数); 当月社保/医保/公积金已回盘确认则冻结",
      needYm: true, uploadYm: true },
    { key: "tc07", id: "tc07_bp", label: "个人补退", sheet: "个人补退",
      cols: 4, head: 1, check: "PRC_保险补贴导入校验", commit: "PRC_保险补贴导入提交",
      page: "td/d/af 个人补退(28)",
      note: "单位/个人险金合计两列; 同人已有手补行则整行覆盖(补充数据源 datasource='10')",
      uploadYm: true },
    { key: "td04", id: "td04_bp", label: "补偿金", sheet: "经济补偿金回盘",
      cols: 10, head: 1, check: "PRC_补偿金导入校验", commit: "PRC_补偿金导入提交",
      page: "td/d/x 经济补偿金(27)",
      note: "近12个月收入×补偿月数, 合计自动生成中文大写; 含待通知金; 结算单元必选(落行必填)",
      needSettle: true },
  ];

  const st = reactive({ tab: "tc04", baseErr: "", ym: "", atb930: null });   // ym=异动/补退年月; atb930=补偿金结算单元
  const settleUnits = ref([]);
  const runs = reactive({});
  TABS.forEach(t => { runs[t.key] = reactive({ file: null, ace: null, log: "", rows: [], table: "", busy: false }); });
  const cur = () => TABS.find(x => x.key === st.tab) || TABS[0];

  function onFile(t, ev) { runs[t.key].file = ev.target.files[0] || null; }
  function pickFile(t) { document.getElementById("sfile_" + t.key)?.click(); }

  function summary(rows) {
    const bad = rows.filter(r => r.ace10a === "2").length;
    return `共 ${rows.length} 行` + (bad ? `, 拒绝 ${bad} 行` : ", 全部通过");
  }

  async function loadBatch(t) {
    const r = runs[t.key];
    if (!r.ace) return;
    try {
      const b = await api.wb("/batch/" + r.ace);
      r.rows = b.rows || []; r.table = b.table || "";
    } catch (e) { r.log += "批次查询失败: " + e.message + "\n"; }
  }

  async function upload(t) {
    const r = runs[t.key];
    if (!r.file) { ElementPlus.ElMessage.warning("先选择模板 Excel"); return; }
    if ((t.uploadYm || t.needYm) && !/^\d{6}$/.test(st.ym)) {
      ElementPlus.ElMessage.warning("先填年月(6 位, 如 202611)"); return;
    }
    if (t.needSettle && !st.atb930) { ElementPlus.ElMessage.warning("补偿金: 先选结算单元(落行必填)"); return; }
    r.busy = true; r.log = ""; r.rows = [];
    try {
      // 弹窗透传参数(模板列之外): 年月块 tc06/tc07(落行 NOT NULL), 结算单元 td04(atb931 NOT NULL)
      const params = { aae011: api.store.oper.aae011 };
      if (t.uploadYm || t.needYm) params.aae002 = st.ym;
      if (t.needSettle) {
        const s = settleUnits.value.find(x => x.atb930 === st.atb930);
        Object.assign(params, { atb930: st.atb930, atb931: s ? s.atb931 : "结算单元",
          aab001: s ? s.aab001 : null, atb900: s ? s.atb900 : null });
      }
      const fd = new FormData();
      fd.append("file", r.file);
      fd.append("parameters", JSON.stringify(params));
      const resp = await fetch("/import/sys/" + t.id, { method: "POST", body: fd });
      const j = await resp.json();
      if (!j.succeed) {
        r.log = "导入失败: " + (j.msg || resp.status);
        ElementPlus.ElMessage.error(j.msg || "导入失败");
      } else {
        r.ace = (j.ce10 || [])[0];
        r.log = `导入成功: 批次 ${r.ace}` +
          ((j.errors || []).length ? `, 超长列 ${j.errors.length} 处(前 3: ${j.errors.slice(0, 3).join(" / ")})` : "") +
          "\n" + summary(r.rows.length ? r.rows : []);
        await loadBatch(t);
        r.log = `导入成功: 批次 ${r.ace} — ${summary(r.rows)}; 先【校验】再【提交】`;
      }
    } catch (e) { r.log = "导入异常: " + e.message; }
    r.busy = false;
  }

  async function check(t) {
    const r = runs[t.key];
    if (!r.ace) { ElementPlus.ElMessage.warning("先上传 Excel 生成批次"); return; }
    if (t.needYm && !/^\d{6}$/.test(st.ym)) { ElementPlus.ElMessage.warning("先填异动年月(6 位, 如 202611)"); return; }
    r.busy = true;
    try {
      const body = { ace100: String(r.ace), aae011: api.store.oper.aae011 };
      if (t.needYm) body.aae002 = st.ym;
      await api.proc(PKG + t.check, body);
      await loadBatch(t);
      r.log = "校验完成: " + summary(r.rows) +
        (r.rows.some(x => x.ace10a === "2") ? " — 有拒绝行(见表原因列)" : " — 可提交");
    } catch (e) { r.log = "校验失败: " + e.message; }
    r.busy = false;
  }

  async function commit(t) {
    const r = runs[t.key];
    if (!r.ace) { ElementPlus.ElMessage.warning("先上传并校验"); return; }
    try {
      await ElMessageBox.confirm(
        `提交批次 ${r.ace}(${summary(r.rows)})?\n提交后直接落账(${t.label}), 可在对应查询面核对。`, "专项提交");
    } catch (e) { return; }
    r.busy = true;
    try {
      await api.proc(PKG + t.commit, { ace100: String(r.ace), aae011: api.store.oper.aae011 });
      await loadBatch(t);
      r.log = (r.log || "") + "\n提交完成: " + summary(r.rows);
      ElementPlus.ElMessage.success("提交完成");
    } catch (e) { r.log = (r.log || "") + "\n提交失败: " + e.message; }
    r.busy = false;
  }

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

  async function loadBase() {
    st.ym = new Date().getFullYear() + String(new Date().getMonth() + 1).padStart(2, "0");
    try { settleUnits.value = (await api.wb("/settle-units")) || []; } catch (e) { /* 保底手选 */ }
  }

  WB.pages.special = {
    template: `
    <div class="page">
      <h4>专项(按需)登记 · 五路批量导入 <span style="font-weight:400;font-size:12px;color:#909399">
        (东软 td/c·td/d 专项弹窗; 匹配键都在 Excel 列内, 无需先选单位; 校验→全过→提交)</span></h4>
      <el-tabs v-model="st.tab">
        <el-tab-pane v-for="t in tabs" :key="t.key" :name="t.key"
                     :label="t.label + '(' + t.cols + '列)'">
          <div style="margin:6px 0">
            <wb-dsf-tip :menu="'专项(按需)'" :page="t.page" area="导入弹窗" btn="模板下载"
              :note="'东软: 专项弹窗[模板下载]键; 新系统由 sys_import_mapping 反向生成 xlsx, 列头在第 ' + t.head + ' 行'">
              <el-button size="small" @click="downloadTemplate(t)">下载模板</el-button>
            </wb-dsf-tip>
            <wb-dsf-tip :menu="'专项(按需)'" :page="t.page" area="导入弹窗" btn="上传导入"
              note="东软: 弹窗[导入]键 → 通用导入引擎; 新系统 POST /import/sys/{模板号}, 批次行入 TC04/05/06/07_BP·TD04_BP">
              <input type="file" accept=".xls,.xlsx" style="display:none"
                     :id="'sfile_' + t.key" @change="ev => onFile(t, ev)"/>
              <el-button size="small" type="primary" plain @click="pickFile(t)">选择 Excel</el-button>
            </wb-dsf-tip>
            <el-button size="small" plain :disabled="!runs[t.key].file || runs[t.key].busy"
                       @click="upload(t)">上传导入</el-button>
            <wb-dsf-tip :menu="'专项(按需)'" :page="t.page" area="导入弹窗" btn="校验"
              :note="t.check + (t.needYm ? '(需异动年月 aae002)' : '') + ' — 逐行匹配/查重, 结果写回批次行 ace10a'">
              <el-button size="small" type="warning" plain :disabled="!runs[t.key].ace || runs[t.key].busy"
                         @click="check(t)">校验</el-button>
            </wb-dsf-tip>
            <wb-dsf-tip :menu="'专项(按需)'" :page="t.page" area="导入弹窗" btn="提交"
              :note="t.commit + ' — ' + t.note">
              <el-button size="small" type="danger" plain :disabled="!runs[t.key].ace || runs[t.key].busy"
                         @click="commit(t)">提交</el-button>
            </wb-dsf-tip>
            <span style="margin-left:8px" v-if="runs[t.key].file">{{ runs[t.key].file.name }}</span>
          </div>
          <el-form inline v-if="t.needYm || t.uploadYm || t.needSettle" label-width="110px">
            <el-form-item v-if="t.needYm || t.uploadYm" :label="t.key === 'tc06' ? '异动年月' : '补退年月'">
              <el-input v-model="st.ym" placeholder="6 位, 如 202611" style="width:130px"/>
              <span class="kv" v-if="t.needYm">  该月社保/医保/公积金已回盘确认时, 校验按冻结规则拒绝</span>
            </el-form-item>
            <el-form-item v-if="t.needSettle" label="结算单元">
              <el-select v-model="st.atb930" filterable placeholder="选结算单元" style="width:240px">
                <el-option v-for="s in settleUnits" :key="s.atb930" :value="s.atb930"
                           :label="s.atb930 + ' ' + s.atb931"/>
              </el-select>
            </el-form-item>
          </el-form>
          <div class="kv">{{ t.note }}</div>
          <pre class="log" v-if="runs[t.key].log">{{ runs[t.key].log }}</pre>
          <template v-if="runs[t.key].rows.length">
            <h4>批次明细 ({{ runs[t.key].table }} · ace100={{ runs[t.key].ace }})</h4>
            <el-table :data="runs[t.key].rows" size="small" border max-height="300">
              <el-table-column prop="aac003" label="姓名" width="90"/>
              <el-table-column prop="aac002" label="身份证" width="170"/>
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
      return { st, runs, tabs: TABS, cur, settleUnits,
        onFile, pickFile, upload, check, commit, downloadTemplate, api };
    },
  };
})();
