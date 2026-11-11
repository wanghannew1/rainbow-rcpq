-- wb_audit_log — 工作台操作留痕(2026-11-11 定调: 用户操作一律留痕, 删除一律逻辑删除)
-- 覆盖: /proc 过程网关、/interaction 交互网关、/wb 工作台写操作、/wb/roster-export 报盘导出。
-- detail 存请求 JSON(截断 2000 字); 不记录任何明文口令(系统无)。
CREATE TABLE IF NOT EXISTS wb_audit_log (
  id      bigserial PRIMARY KEY,
  aae036  timestamptz NOT NULL DEFAULT now(),
  aae011  varchar(32),          -- 经办人(请求体 aae011/oper, 可空)
  action  varchar(64) NOT NULL, -- proc:{过程名} | interaction:{REQ编号} | wb:{端点} | export:{导出}
  target  varchar(200),         -- 主对象标识(如 atc900/ace100/atc9w0/ym)
  detail  text,                 -- 请求摘要 JSON(截断)
  app_ip  varchar(64)
);
CREATE INDEX IF NOT EXISTS idx_wb_audit_036 ON wb_audit_log (aae036);
CREATE INDEX IF NOT EXISTS idx_wb_audit_action ON wb_audit_log (action);
