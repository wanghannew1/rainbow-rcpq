/* 险金测算: PRC_计算险金(TC90+AA97 基数+AA94 费率+TJ0 征缴覆盖) → TC01 结果展示 */
(() => {
  const { reactive } = Vue;
  const { api } = WB;

  const calc = reactive({ atc900: "", aae002: "202611", result: null, err: "" });
  async function run() {
    if (!calc.atc900) { ElementPlus.ElMessage.warning("填合同号 ATC900"); return; }
    calc.err = ""; calc.result = null;
    try {
      calc.result = await api.proc("PKG_人才派遣公共.PRC_计算险金",
        { atc900: calc.atc900, aae002: calc.aae002 || "202611" });
    } catch (e) { calc.err = e.message; }
  }

  WB.pages.calc = {
    template: `
    <div class="page">
      <el-form inline>
        <el-form-item label="合同号 ATC900"><el-input v-model="calc.atc900" style="width:140px"/></el-form-item>
        <el-form-item label="所属年月">
          <el-date-picker v-model="calc.aae002" type="month" value-format="YYYYMM"
                          placeholder="202611" style="width:120px"/></el-form-item>
        <el-form-item><el-button type="primary" @click="run">计算</el-button></el-form-item>
      </el-form>
      <div v-if="calc.err" class="bad">{{ calc.err }}</div>
      <el-descriptions v-if="calc.result" :column="4" border size="small">
        <el-descriptions-item v-for="k in keys" :key="k" :label="k">{{ calc.result[k] }}</el-descriptions-item>
      </el-descriptions>
      <div class="kv">等价旧链路: 险金计算 → TC01 结果; AC10 社保月数据由本过程机器补(atc9en='3')</div>
    </div>`,
    setup() {
      const keys = ["aaa301", "aaa302", "aaa303", "bba001", "bba002", "bba003", "bba004", "bba005",
        "baa001", "baa002", "baa003", "caa001", "caa002", "bba200", "baa200", "atc9eh"];
      return { calc, run, keys };
    },
  };
})();
