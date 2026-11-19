/* 异议处理(W6 前端): 报盘回盘异议全链——反馈/取消反馈(tdcf 44) · 转入/取消转入(tdcg 45) ·
   医保异议同步(tdcad) · 异议离职同步(旧系统 doQuery 隐式触发, 本页查询前包装调用)。
   对接 PKG_保险人员增减.PRC_异议处理/异议离职同步/医保异议同步 + /wb/yy 查询面。 */
(() => {
  const { reactive, ref, onMounted } = Vue;
  const PKG = "PKG_保险人员增减.";

  WB.pages.yy = {
    setup() {
      const api = window.WB.api;
      const rp = reactive({ busy: false, msg: "", rows: [], detail: null,
                            fBacksign: "0", fInsign: "", fCbsign: "2", fSafesign: "",
                            syncAae002: "202611", syncAaa030: null, syncAaa100: "220100000000",
                            heads: [], districts: [] });

      async function load() {
        // 旧系统 tdcf/tdcg doQuery 首步隐式跑异议离职同步——本页保持该行为
        try { await api.proc(PKG + "PRC_异议离职同步", {}); } catch (e) { /* 忠实: 同步失败不拦查询 */ }
        const q = new URLSearchParams();
        if (rp.fBacksign) q.set("backsign", rp.fBacksign);
        if (rp.fInsign) q.set("insign", rp.fInsign);
        if (rp.fCbsign) q.set("cbsign", rp.fCbsign);
        if (rp.fSafesign) q.set("safesign", rp.fSafesign);
        q.set("bae011", api.store.oper.aae011);
        rp.rows = (await api.wb("/wb/yy/list?" + q.toString())) || [];
      }

      async function deal(id, flag, label) {
        rp.busy = true;
        try {
          await api.proc(PKG + "PRC_异议处理",
            { id, aae011: api.store.oper.aae011, flag });
          ElementPlus.ElMessage.success(label + "完成");
          await load();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }

      async function ybSync() {
        if (!rp.syncAaa030) { ElementPlus.ElMessage.warning("先输户头 AAA030"); return; }
        rp.busy = true;
        try {
          const r = await api.proc(PKG + "PRC_医保异议同步",
            { aae002: rp.syncAae002, aaa030: rp.syncAaa030, aaa100: rp.syncAaa100 });
          rp.msg = `医保异议同步完成: 扫描 ${r.scanned || 0} 行(TC9W1 ABP016 0→转入/2→重录异议)`;
          await load();
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }

      function show(row) { rp.detail = row; }
      const SAFE = { "1": "社保", "2": "医保", "3": "工伤", "4": "公积金" };
      const BS = { "0": "未反馈", "1": "已反馈" };
      const INS = { "0": "未转入", "1": "已转入" };
      const CBS = { "2": "未参保", "9": "离职作废" };

      onMounted(async () => {
        try {
          rp.heads = (await api.wb("/aaa030s")) || [];
          rp.districts = (await api.wb("/districts")) || [];
        } catch (e) { /* 保底手输 */ }
      });
      return { api, rp, rows, load, deal, ybSync, show, SAFE, BS, INS, CBS };
    },
    template: `
    <div class="page">
      <el-alert type="info" :closable="false" style="margin-bottom:8px"
        title="报盘回盘异议全链: 反馈(取消)→转入(取消)→离职自动作废; 列表查询前自动跑一次异议离职同步(旧系统行为)"/>
      <el-form inline>
        <el-form-item label="反馈">
          <el-select v-model="rp.fBacksign" clearable style="width:100px">
            <el-option value="0" label="未反馈"/><el-option value="1" label="已反馈"/>
          </el-select>
        </el-form-item>
        <el-form-item label="转入">
          <el-select v-model="rp.fInsign" clearable style="width:100px">
            <el-option value="0" label="未转入"/><el-option value="1" label="已转入"/>
          </el-select>
        </el-form-item>
        <el-form-item label="参保标识">
          <el-select v-model="rp.fCbsign" clearable style="width:110px">
            <el-option value="2" label="未参保"/><el-option value="9" label="离职作废"/>
          </el-select>
        </el-form-item>
        <el-form-item label="险种">
          <el-select v-model="rp.fSafesign" clearable style="width:100px">
            <el-option value="1" label="社保"/><el-option value="2" label="医保"/>
            <el-option value="3" label="工伤"/><el-option value="4" label="公积金"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/f 异议处理 (44) · td/c/g (45)" area="异议列表" btn="查询"
            note="旧系统查询前隐式跑 PRC_异议离职同步(合同终止 ATB90R=5 的异议行 CBSIGN 置 9), 本页保持该行为">
            <el-button type="primary" :loading="rp.busy" @click="load">查询</el-button>
          </wb-dsf-tip>
        </el-form-item>
      </el-form>
      <div v-if="rp.msg" class="kv">{{ rp.msg }}</div>

      <el-table :data="rp.rows" size="small" border max-height="300" highlight-current-row @current-change="show">
        <el-table-column prop="tc90yyid" label="异议号" width="110"/>
        <el-table-column label="险种" width="80">
          <template #default="{ row }">{{ SAFE[row.safesign] || row.safesign }}</template>
        </el-table-column>
        <el-table-column prop="aae002" label="异动年月" width="95"/>
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aac002" label="身份证号" width="170"/>
        <el-table-column prop="atb931" label="结算单元" min-width="140"/>
        <el-table-column prop="aaa310" label="缴费基数" width="100"/>
        <el-table-column label="反馈" width="90">
          <template #default="{ row }">{{ BS[row.backsign] || row.backsign }}</template>
        </el-table-column>
        <el-table-column label="转入" width="90">
          <template #default="{ row }">{{ INS[row.insign] || row.insign }}</template>
        </el-table-column>
        <el-table-column label="参保" width="90">
          <template #default="{ row }">{{ CBS[row.cbsign] || row.cbsign }}</template>
        </el-table-column>
        <el-table-column prop="aae013" label="异议原因" min-width="140"/>
      </el-table>

      <div v-if="rp.detail" style="margin-top:10px">
        <h4>选中: {{ rp.detail.aac003 }} · {{ SAFE[rp.detail.safesign] }} · 异议号 {{ rp.detail.tc90yyid }}</h4>
        <el-form inline>
          <el-form-item>
            <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/f 异议处理 (44)" area="反馈板块" btn="反馈/取消反馈"
              note="flag=1 反馈(BACKSIGN 0→1, 市医保联动 TC9W1.ABP017); flag=2 取消(已转入拒)">
              <el-button type="primary" :loading="rp.busy" @click="deal(rp.detail.tc90yyid, '1', '反馈')">反馈</el-button>
              <el-button :loading="rp.busy" @click="deal(rp.detail.tc90yyid, '2', '取消反馈')">取消反馈</el-button>
            </wb-dsf-tip>
          </el-form-item>
          <el-form-item>
            <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/g 异议处理-转入 (45)" area="转入板块" btn="转入/取消转入"
              note="转入: BACKSIGN=1 前置, 状态列置 NULL(SAFESIGN 线)或 TC9W1.ABP016='0'; 取消转入: 状态列置'2'+ABP016='2'">
              <el-button type="success" :loading="rp.busy" @click="deal(rp.detail.tc90yyid, '3', '转入')">转入</el-button>
              <el-button type="warning" :loading="rp.busy" @click="deal(rp.detail.tc90yyid, '4', '取消转入')">取消转入</el-button>
            </wb-dsf-tip>
          </el-form-item>
        </el-form>
      </div>

      <h4>医保异议同步(tdcad: TC9W1 回盘结果 → 异议行/参保状态)</h4>
      <el-form inline>
        <el-form-item label="年月"><el-input v-model="rp.syncAae002" style="width:100px"/></el-form-item>
        <el-form-item label="户头">
          <el-select v-model="rp.syncAaa030" filterable clearable style="width:220px" placeholder="AAA030">
            <el-option v-for="h in rp.heads" :key="h.aaa030" :value="h.aaa030"
                       :label="h.aaa030 + ' ' + (h.aaa031 || '')"/>
          </el-select>
        </el-form-item>
        <el-form-item label="属地">
          <el-select v-model="rp.syncAaa100" filterable style="width:180px">
            <el-option v-for="d in rp.districts" :key="d.id" :value="d.id" :label="(d.label || '') + ' ' + d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/ad 医保异议 (an:46)" area="同步" btn="执行同步"
            note="TC9W1 扫描(ATC9W1<>3): ABP016=0→异议行 INSIGN=1+TC90.ATC90PA=NULL; =2→重录异议+ATC90PA='2'">
            <el-button type="primary" :loading="rp.busy" @click="ybSync">执行同步</el-button>
          </wb-dsf-tip>
        </el-form-item>
      </el-form>
    </div>`,
  };
})();
