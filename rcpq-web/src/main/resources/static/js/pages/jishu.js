/* 基数申报工作台(W5 收尾波): 核对/确认×4 · 基数导入校验/提交 · 公积金上下限调整 · 未申报清单。
   对接 PKG_缴费基数 14 过程 + /wb/jishu 查询面。
   东软页码: tdcl(29) 导入 · tdcq(30) 社保核对 · tdcab(31) 医保 · tdcac(32) 公积金核对
   · tdch(33) 工伤核对 · tdcc(20) 上下限调整 · tdcd(21) 上下限审核 · 未申报弹层在核对页。 */
(() => {
  const { reactive, ref, computed, onMounted } = Vue;

  const LINES = [
    { key: "sb", label: "社保", hd: "社保缴费基数核对", qr: "社保缴费基数确认", page: "td/c/q 核对工作台 (30)", wsb: "社保未申报" },
    { key: "yb", label: "医保", hd: "医保缴费基数核对", qr: "医保缴费基数确认", page: "td/c/ab 核对工作台 (31)", wsb: "医保未申报" },
    { key: "gjj", label: "公积金", hd: "公积金缴费基数核对", qr: "公积金缴费基数确认", page: "td/c/ac 核对工作台 (32)", wsb: "公积金未申报" },
    { key: "gs", label: "工伤", hd: "工伤缴费基数核对", qr: "工伤缴费基数确认", page: "td/c/h 核对工作台 (33)", wsb: "工伤未申报" },
  ];
  const ST_LABEL = { "0": "未核对/被退回", "3": "已核对待确认", "1": "已确认" };

  WB.pages.jishu = {
    setup() {
      const api = window.WB.api;
      const tab = ref("hd");
      const rp = reactive({ aae001: "2026", aaa030: null, aaa020: null, busy: false,
                            aae002: "202607", msg: "" });
      const rows = ref([]);

      // ---- 核对/确认 ----
      async function hd() {
        if (!rp.aaa030 || !rp.aaa020) { ElementPlus.ElMessage.warning("先选户头与参保属地"); return; }
        const cur = LINES.find(l => l.key === rp.line);
        rp.busy = true;
        try {
          const r = await api.proc("PKG_缴费基数.PRC_" + cur.hd,
            { aae001: rp.aae001, aaa030: rp.aaa030, aaa020: rp.aaa020 });
          rp.msg = `核对完成: ${r.rows || 0} 行中标记 ${r.marked || 0} 行` + (r.skipped ? ` (${r.skipped})` : "");
          await load();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function qr() {
        const cur = LINES.find(l => l.key === rp.line);
        rp.busy = true;
        try {
          const r = await api.proc("PKG_缴费基数.PRC_" + cur.qr,
            { aae001: rp.aae001, aaa030: rp.aaa030, aaa020: rp.aaa020,
              aae002: rp.aae002, aae011: api.store.oper.aae011 });
          rp.msg = `确认完成: ${r.confirmed || 0} 行(基数落 TC90 + AA97 置'1')`;
          await load();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function load() {
        if (!rp.aaa030) return;
        rows.value = (await api.wb(`/wb/jishu/aa97?aae001=${rp.aae001}&aaa030=${rp.aaa030}` +
          `&aaa020=${encodeURIComponent(rp.aaa020)}&line=${rp.line}`)) || [];
      }

      // ---- 导入校验/提交 ----
      const imp = reactive({ ace100: null, rows: [] });
      async function loadBp() {
        if (!imp.ace100) { ElementPlus.ElMessage.warning("先选/输入批次号"); return; }
        imp.rows = (await api.wb("/wb/jishu/bp?ace100=" + imp.ace100)) || [];
      }
      async function impCheck() {
        rp.busy = true;
        try {
          const r = await api.proc("PKG_缴费基数.PRC_基数导入校验", { ace100: imp.ace100, aae001: rp.aae001 });
          rp.msg = r.errMsg; await loadBp();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function impCommit() {
        rp.busy = true;
        try {
          const r = await api.proc("PKG_缴费基数.PRC_基数导入提交",
            { ace100: imp.ace100, aae011: api.store.oper.aae011 });
          rp.msg = r.errMsg; await loadBp();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function loadBatches() {
        try { imp.batches = (await api.wb("/wb/jishu/batches")) || []; } catch (e) { imp.batches = []; }
      }

      // ---- 上下限 ----
      const sx = reactive({ atb930: null, ace100: null, rows: [], audits: [], aae114: "", rejectReason: "", sel: [] });
      async function sxTemplate() {
        rp.busy = true;
        try {
          const r = await api.proc("PKG_缴费基数.PRC_公积金基数上下限模板", { atb930: sx.atb930 });
          rp.msg = `模板生成: ${r.generated || 0} 行(TC0A_BP01, 导出弹窗属旧系统 Excel 面)`; await loadSxBp();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function loadSxBp() {
        if (sx.ace100) sx.rows = (await api.wb("/wb/jishu/tc0a_bp?ace100=" + sx.ace100)) || [];
      }
      async function sxCheck() {
        rp.busy = true;
        try {
          const r = await api.proc("PKG_缴费基数.PRC_公积金基数上下限调整校验", { ace100: sx.ace100 });
          rp.msg = r.errMsg; await loadSxBp();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function sxCommit() {
        rp.busy = true;
        try {
          const r = await api.proc("PKG_缴费基数.PRC_公积金基数上下限调整提交",
            { ace100: sx.ace100, aae011: api.store.oper.aae011 });
          rp.msg = r.errMsg; await loadSxBp(); await loadAudit();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function loadAudit() {
        const q = new URLSearchParams();
        if (sx.aae002) q.set("aae002", sx.aae002);
        if (sx.aae114) q.set("aae114", sx.aae114);
        sx.audits = (await api.wb("/wb/jishu/tc0a?" + q.toString())) || [];
      }
      async function audit(flag) {
        if (!sx.sel.length) { ElementPlus.ElMessage.warning("先勾选待审行"); return; }
        const ids = sx.sel.map(r => r.atc0a0).join(",");
        rp.busy = true;
        try {
          const r = await api.proc("PKG_缴费基数.PRC_公积金基数上下限调整审核",
            { atc0a0: ids, flag, aae011: api.store.oper.aae011, aae013: sx.rejectReason });
          rp.msg = r.errMsg; await loadAudit();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }

      // ---- 未申报 ----
      const wsb = reactive({ line: "gjj", rows: [], detail: [], cur: null });
      async function wsbGen() {
        const cur = LINES.find(l => l.key === wsb.line);
        rp.busy = true;
        try {
          await api.proc("PKG_缴费基数.PRC_" + cur.wsb,
            { aae001: rp.aae001, aaa030: rp.aaa030, aaa020: rp.aaa020 });
          await wsbLoad();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function wsbLoad() {
        wsb.rows = (await api.wb(`/wb/jishu/dw?aae001=${rp.aae001}&aaa030=${rp.aaa030}` +
          `&aaa020=${encodeURIComponent(rp.aaa020)}&line=${wsb.line}`)) || [];
      }
      async function wsbDetail(row) {
        wsb.cur = row;
        wsb.detail = (await api.wb("/wb/jishu/gr?fid=" + row.fid)) || [];
      }

      onMounted(() => { loadBatches(); });

      const stLabel = s => ST_LABEL[s] || (s || "未校验");
      return { api, tab, rp, rows, LINES, hd, qr, load, stLabel,
               imp, loadBp, impCheck, impCommit, loadBatches,
               sx, sxTemplate, loadSxBp, sxCheck, sxCommit, loadAudit, audit,
               wsb, wsbGen, wsbLoad, wsbDetail };
    },
    template: `
    <div class="page">
      <el-form inline>
        <el-form-item label="调整年度"><el-input v-model="rp.aae001" style="width:90px"/></el-form-item>
        <el-form-item label="户头"><el-input v-model.number="rp.aaa030" placeholder="AAA030" style="width:110px"/></el-form-item>
        <el-form-item label="参保属地(区划)"><el-input v-model="rp.aaa020" placeholder="如 220100000000" style="width:150px"/></el-form-item>
      </el-form>
      <el-tabs v-model="tab">
        <el-tab-pane label="核对 / 确认×4" name="hd">
          <el-form inline>
            <el-form-item label="险种">
              <el-radio-group v-model="rp.line">
                <el-radio-button v-for="l in LINES" :key="l.key" :value="l.key">{{ l.label }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="确认生效年月"><el-input v-model="rp.aae002" style="width:100px"/></el-form-item>
            <el-form-item>
              <wb-dsf-tip :menu="'员工异动/补退费管理'" :page="LINES.find(l => l.key === rp.line).page"
                area="缴费基数核对" btn="核对" note="资格判定(SYSDATE 年度窗口)+申报基数核对, AAE214 置 3/0">
                <el-button type="primary" :loading="rp.busy" @click="hd">核对</el-button>
              </wb-dsf-tip>
              <wb-dsf-tip :menu="'员工异动/补退费管理'" :page="LINES.find(l => l.key === rp.line).page"
                area="缴费基数核对" btn="确认" note="基数落 TC90 + AA97 置'1'+经办件套; 仅对 AAE214='3' 行生效">
                <el-button type="success" :loading="rp.busy" @click="qr">确认</el-button>
              </wb-dsf-tip>
              <el-button @click="load">查询</el-button>
            </el-form-item>
          </el-form>
          <div v-if="rp.msg" class="kv">{{ rp.msg }}</div>
          <el-table :data="rows" size="small" border max-height="420">
            <el-table-column prop="aac003" label="姓名" width="90"/>
            <el-table-column prop="aac002" label="身份证号" width="170"/>
            <el-table-column label="核对状态" width="120">
              <template #default="{ row }">{{ stLabel(row.aae214_1 || row.aae214_2 || row.aae214_3 || row.aae214_4) }}</template>
            </el-table-column>
            <el-table-column label="申报基数" width="110">
              <template #default="{ row }">{{ row.aaa301_1 ?? row.aaa302_1 ?? row.aaa303_1 ?? row.aaa305_1 ?? '—' }}</template>
            </el-table-column>
            <el-table-column prop="aaa300" label="上年月均工资" width="120"/>
            <el-table-column prop="atb930" label="结算单元" width="100"/>
            <el-table-column prop="aab004" label="单位" min-width="180"/>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="基数导入" name="imp">
          <el-form inline>
            <el-form-item label="批次">
              <el-select v-model="imp.ace100" filterable clearable style="width:200px" @change="loadBp">
                <el-option v-for="b in (imp.batches || [])" :key="b.ace100" :value="b.ace100"
                  :label="b.ace100 + ' (' + (b.n || 0) + '行 过' + (b.ok3 || 0) + ' 败' + (b.ng2 || 0) + ' 提' + (b.ok1 || 0) + ')'"/>
              </el-select>
              <el-input v-model="imp.ace100" placeholder="或手输批次号" style="width:140px;margin-left:8px"/>
            </el-form-item>
            <el-form-item>
              <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/l 基数导入工作台 (29)" area="导入批次" btn="校验"
                note="Excel 导入面(lemis.imp)属旧系统; 本页消费其产物 AA97_BP(ACE100 批次)">
                <el-button type="primary" :loading="rp.busy" @click="impCheck">校验</el-button>
              </wb-dsf-tip>
              <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/l 基数导入工作台 (29)" area="导入批次" btn="提交"
                note="AA97 落库(属地择一)+AA97_BP 置'1'; UK 命中走 UPDATE(已确认线 AAE214=1 不覆写)">
                <el-button type="success" :loading="rp.busy" @click="impCommit">提交</el-button>
              </wb-dsf-tip>
              <el-button @click="loadBp">查询批次行</el-button>
            </el-form-item>
          </el-form>
          <div v-if="rp.msg" class="kv">{{ rp.msg }}</div>
          <el-table :data="imp.rows" size="small" border max-height="420">
            <el-table-column prop="id" label="行号" width="70"/>
            <el-table-column prop="aac003" label="姓名" width="90"/>
            <el-table-column prop="aac002" label="身份证号" width="170"/>
            <el-table-column prop="aae001" label="调整年度" width="90"/>
            <el-table-column prop="aaa301_1" label="社保申报" width="90"/>
            <el-table-column prop="aaa302_1" label="省医保" width="80"/>
            <el-table-column prop="aaa302_2" label="市医保" width="80"/>
            <el-table-column prop="aaa303_1" label="省公积金" width="85"/>
            <el-table-column prop="aaa303_2" label="市公积金" width="85"/>
            <el-table-column prop="aaa305_1" label="省工伤" width="80"/>
            <el-table-column prop="aaa305_2" label="市工伤" width="80"/>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">{{ { '3': '校验过', '2': '校验失败', '1': '已提交', '0': '提交失败' }[row.ace10a] || '未校验' }}</template>
            </el-table-column>
            <el-table-column prop="ace10b" label="原因" min-width="220"/>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="公积金上下限" name="sx">
          <el-form inline>
            <el-form-item label="结算单元 ATB930"><el-input v-model.number="sx.atb930" style="width:120px"/></el-form-item>
            <el-form-item>
              <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/c 上下限调整 (20)" area="模板" btn="生成模板"
                note="AA98 省市现窗×TC90 候选出窗员工→TC0A_BP01(导出弹窗属旧系统 Excel 面)">
                <el-button @click="sxTemplate">生成模板</el-button>
              </wb-dsf-tip>
            </el-form-item>
            <el-form-item label="批次"><el-input v-model.number="sx.ace100" style="width:130px"/></el-form-item>
            <el-form-item>
              <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/c 上下限调整 (20)" area="导入批次" btn="校验/提交"
                note="校验: 比例{12,7}整数段+补缴三查+只查省限出窗; 提交: TC0A 落库+UK 命中 UPDATE(已审不覆写)">
                <el-button type="primary" :loading="rp.busy" @click="sxCheck">校验</el-button>
                <el-button type="success" :loading="rp.busy" @click="sxCommit">提交</el-button>
                <el-button @click="loadSxBp">查询批次行</el-button>
              </wb-dsf-tip>
            </el-form-item>
          </el-form>
          <div v-if="rp.msg" class="kv">{{ rp.msg }}</div>
          <el-table :data="sx.rows" size="small" border max-height="260">
            <el-table-column prop="id" label="行号" width="70"/>
            <el-table-column prop="aac003" label="姓名" width="90"/>
            <el-table-column prop="aaa303_1" label="原基数" width="90"/>
            <el-table-column prop="aaa303_2" label="调整后" width="90"/>
            <el-table-column prop="atc0a1" label="补缴" width="70"/>
            <el-table-column prop="atc0a2" label="起" width="85"/>
            <el-table-column prop="atc0a3" label="止" width="85"/>
            <el-table-column prop="abp00d" label="单位月缴" width="90"/>
            <el-table-column prop="abp00e" label="个人月缴" width="90"/>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">{{ { '3': '校验过', '2': '失败', '1': '已提交' }[row.ace10a] || '未校验' }}</template>
            </el-table-column>
            <el-table-column prop="ace10b" label="原因" min-width="200"/>
          </el-table>

          <h4>审核台(TC0A)</h4>
          <el-form inline>
            <el-form-item label="调整年月"><el-input v-model.number="sx.aae002" style="width:110px" placeholder="如 202611"/></el-form-item>
            <el-form-item label="审核标识">
              <el-select v-model="sx.aae114" clearable style="width:110px">
                <el-option value="0" label="0 未审"/><el-option value="1" label="1 通过"/><el-option value="2" label="2 驳回"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button @click="loadAudit">查询</el-button>
            </el-form-item>
            <el-form-item>
              <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/d 上下限审核 (21)" area="审核列表" btn="通过/驳回/取消"
                note="通过: TC90.AAA303←AAA303_1+AA97 同步+AA97_BG 台账; 驳/取消已审态回滚; 转款已确认(TJ0D1/TJ0E1)拦截">
                <el-button type="success" @click="audit('1')">通过</el-button>
                <el-button type="danger" @click="audit('2')">驳回</el-button>
                <el-button @click="audit('3')">取消审核</el-button>
                <el-input v-model="sx.rejectReason" placeholder="驳回原因" style="width:140px;margin-left:8px"/>
              </wb-dsf-tip>
            </el-form-item>
          </el-form>
          <el-table :data="sx.audits" size="small" border max-height="300"
                    @selection-change="s => sx.sel = s" ref="auditTable">
            <el-table-column type="selection" width="40"/>
            <el-table-column prop="atc0a0" label="主键" width="110"/>
            <el-table-column prop="aae002" label="调整年月" width="95"/>
            <el-table-column prop="aac003" label="姓名" width="90"/>
            <el-table-column prop="aaa303" label="原基数" width="90"/>
            <el-table-column prop="aaa303_1" label="调整后" width="90"/>
            <el-table-column prop="atc0a1" label="补缴" width="70"/>
            <el-table-column prop="abp00d" label="单位月缴" width="90"/>
            <el-table-column prop="abp00e" label="个人月缴" width="90"/>
            <el-table-column label="审核" width="90">
              <template #default="{ row }">{{ { '0': '未审', '1': '通过', '2': '驳回' }[row.aae114] || '-' }}</template>
            </el-table-column>
            <el-table-column prop="aae113" label="驳回原因" min-width="140"/>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="未申报名单" name="wsb">
          <el-form inline>
            <el-form-item label="险种">
              <el-radio-group v-model="wsb.line">
                <el-radio-button v-for="l in LINES" :key="l.key" :value="l.key">{{ l.label }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <wb-dsf-tip menu="员工异动/补退费管理" :page="LINES.find(l => l.key === wsb.line).page"
                area="未申报名单(弹层)" btn="生成清单"
                note="按单元生成 AA97_DW 清单+AA97_GR 名册(资格判定+已申报排除); 名册弹窗属旧系统 Excel 面">
                <el-button type="primary" :loading="rp.busy" @click="wsbGen">生成清单</el-button>
                <el-button @click="wsbLoad">查询清单</el-button>
              </wb-dsf-tip>
            </el-form-item>
          </el-form>
          <el-table :data="wsb.rows" size="small" border max-height="300" highlight-current-row
                    @current-change="wsbDetail">
            <el-table-column prop="atb930" label="结算单元" width="100"/>
            <el-table-column prop="atb931" label="单元名称" min-width="180"/>
            <el-table-column prop="aab004" label="单位" min-width="180"/>
            <el-table-column prop="psum" label="未申报人数" width="110"/>
            <el-table-column prop="xz" label="险种" width="70"/>
          </el-table>
          <div v-if="wsb.cur" class="kv">单元 {{ wsb.cur.atb931 }} 名册:</div>
          <el-table v-if="wsb.cur" :data="wsb.detail" size="small" border max-height="260">
            <el-table-column prop="aac003" label="姓名" width="100"/>
            <el-table-column prop="aac002" label="身份证号" width="180"/>
            <el-table-column prop="aac001" label="个人编号" width="110"/>
            <el-table-column prop="bxzt" label="保险状态" width="100"/>
            <el-table-column prop="aaa000" label="保险编号" width="150"/>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>`,
  };
})();
