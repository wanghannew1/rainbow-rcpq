/* 参保变更(W6 前端): 退休停保×2(tdit/tdiv, 员工管理菜单) · 保险补收基数_个人(tdbr:48) ·
   变更参保地(tdcbg:30, 属地字典暂缓=忠实逗号串语义)。
   对接 PKG_保险人员增减 停保/变更参保地 + PKG_人才派遣公共.PRC_保险补收基数_个人。
   TC90 在同步集合: 停保/变更对存量行=「退役后启用」语义(变更另发新合同 1e7 段)。 */
(() => {
  const { reactive, ref, onMounted } = Vue;
  const ZR = "PKG_保险人员增减.";

  WB.pages.insure = {
    setup() {
      const api = window.WB.api;
      const rp = reactive({ busy: false, msg: "", q: "", contracts: [], cur: null, last: "",
                            districts: [], bgXz: "3", bgAaa020: "229900000000" });

      async function search() {
        if (!rp.q) { ElementPlus.ElMessage.warning("输入姓名/身份证/合同号"); return; }
        rp.contracts = (await api.wb("/contract-search?q=" + encodeURIComponent(rp.q))) || [];
        if (!rp.contracts.length) ElementPlus.ElMessage.info("无匹配合同");
      }
      function pick(row) { rp.cur = row; rp.last = ""; }
      onMounted(async () => {
        try { rp.districts = (await api.wb("/districts")) || []; } catch (e) { /* 保底手输 */ }
      });
      const ok = r => r.appCode === 1;

      async function sbStop() {
        if (!rp.cur) return;
        rp.busy = true;
        try {
          await api.proc(ZR + "PRC_社保退休停保",
            { atc900: rp.cur.atc900, aaa204: rp.ym204 });
          rp.last = `社保退休停保完成: ATC90P='1', AAA204/AAA209=${rp.ym204}`;
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function ybStop() {
        if (!rp.cur) return;
        rp.busy = true;
        try {
          await api.proc(ZR + "PRC_医保退休停保",
            { atc900: rp.cur.atc900, aaa604: rp.ym604 });
          rp.last = `医保退休停保完成: 市属 ATC90PA='1'/省 '0', AAA602/604/609=${rp.ym604}, AAC008 追加'80'`;
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function bushou() {
        if (!rp.cur) return;
        rp.busy = true;
        try {
          const r = await api.proc("PKG_人才派遣公共.PRC_保险补收基数_个人",
            { aae002: rp.bsAae002, atc900: rp.cur.atc900, aae011: api.store.oper.aae011 });
          rp.last = `补收基数完成: 生成 TC08 ${r.generated || 0} 条(按年分段; 重跑幂等)`;
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }
      async function biangeng() {
        if (!rp.cur) return;
        rp.busy = true;
        try {
          const r = await api.proc(ZR + "PRC_变更参保地",
            { atc900: rp.cur.atc900, xz: rp.bgXz, aaa020: rp.bgAaa020 });
          rp.last = `变更参保地完成: 新合同 ATC900=${r.newAtc900}(旧合同置 ATB90R='5', 跨险种状态列 '0'→'4')`;
        } catch (e) { ElementPlus.ElMessage.error(e.message); } finally { rp.busy = false; }
      }

      return { api, rp, search, pick, sbStop, ybStop, bushou, biangeng };
    },
    template: `
    <div class="page">
      <el-alert type="info" :closable="false" style="margin-bottom:8px"
        title="TC90 在同步集合: 停保/变更对存量行=「退役后启用」语义(变更另发新合同, 旧合同 ATB90R='5'); 补收基数按年分段写 TC08(重跑幂等)"/>
      <el-form inline>
        <el-form-item label="员工(姓名/身份证/合同号)">
          <el-input v-model="rp.q" style="width:220px" @keyup.enter="search"/>
        </el-form-item>
        <el-form-item><el-button @click="search">搜索合同</el-button></el-form-item>
      </el-form>
      <el-table :data="rp.contracts" size="small" border max-height="220" highlight-current-row @current-change="pick">
        <el-table-column prop="atc900" label="合同号" width="110"/>
        <el-table-column prop="aac003" label="姓名" width="90"/>
        <el-table-column prop="aac002" label="身份证号" width="170"/>
        <el-table-column label="合同状态" width="90">
          <template #default="{ row }">{{ { '1': '在途', '2': '待审', '3': '生效', '5': '终止' }[row.atb90r] || row.atb90r }}</template>
        </el-table-column>
        <el-table-column prop="atc905" label="工资基数" width="100"/>
        <el-table-column prop="aab004" label="单位" min-width="180"/>
        <el-table-column prop="atb930" label="结算单元" width="100"/>
      </el-table>

      <div v-if="rp.cur" style="margin-top:10px">
        <h4>选中: {{ rp.cur.aac003 }} · 合同 {{ rp.cur.atc900 }}</h4>
        <el-form inline>
          <el-form-item label="社保停保年月"><el-input v-model="rp.ym204" placeholder="如 202610" style="width:110px"/></el-form-item>
          <el-form-item>
            <wb-dsf-tip menu="员工管理" page="td/i/t 社保退休停保" area="停保" btn="执行"
              note="TC90.ATC90P='1'+AAA204/AAA209=停保年月; 停保年月 yyyymm 格式校验">
              <el-button type="primary" :loading="rp.busy" @click="sbStop">社保退休停保</el-button>
            </wb-dsf-tip>
          </el-form-item>
          <el-form-item label="医保停保年月"><el-input v-model="rp.ym604" placeholder="如 202610" style="width:110px"/></el-form-item>
          <el-form-item>
            <wb-dsf-tip menu="员工管理" page="td/i/v 医保退休停保" area="停保" btn="执行"
              note="市属 ATC90PA='1'/省 '0'+AAA602/604/609=AAC008 追加'80'(退休标识)">
              <el-button type="primary" :loading="rp.busy" @click="ybStop">医保退休停保</el-button>
            </wb-dsf-tip>
          </el-form-item>
        </el-form>
        <el-form inline>
          <el-form-item label="补收年月"><el-input v-model="rp.bsAae002" placeholder="如 202511" style="width:110px"/></el-form-item>
          <el-form-item>
            <wb-dsf-tip menu="员工管理 · 保险补收基数" page="td/b/r 补收基数 (48)" area="个人补收" btn="生成"
              note="三段(社保/医保仅省/公积金)按年分段写 TC08; 本年段不写=源语义; 重跑幂等(UK_TC08)">
              <el-button type="primary" :loading="rp.busy" @click="bushou">生成补收基数</el-button>
            </wb-dsf-tip>
          </el-form-item>
          <el-form-item label="变更线">
            <el-radio-group v-model="rp.bgXz">
              <el-radio-button value="1">社保</el-radio-button>
              <el-radio-button value="2">医保</el-radio-button>
              <el-radio-button value="3">公积金</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="新参保地">
            <el-select v-model="rp.bgAaa020" filterable style="width:200px">
              <el-option v-for="d in rp.districts" :key="d.id" :value="d.id" :label="(d.label || '') + ' ' + d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <wb-dsf-tip menu="员工异动/补退费管理" page="td/c/g 变更参保地 (30)" area="参保地变更" btn="执行"
              note="守卫链(属地未变/未选参保/未参保成功/当月已结算)+旧合同置'5'跨险种'4'+新合同发号; 属地字典暂缓=忠实逗号串语义">
              <el-button type="warning" :loading="rp.busy" @click="biangeng">变更参保地</el-button>
            </wb-dsf-tip>
          </el-form-item>
        </el-form>
        <div v-if="rp.last" class="kv">{{ rp.last }}</div>
      </div>
    </div>`,
  };
})();
