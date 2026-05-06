# AssetBoard

面向融资租赁行业的资产监控平台，参考 [ThingsBoard](https://thingsboard.io/) 架构简化实现，专注于船舶、飞机、工程机械等大型租赁资产的数据采集、遥测监控和告警管理。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17, Spring Boot 3.2.5, Spring Data JPA, Spring Security |
| 数据库 | H2 (嵌入式, 文件模式) |
| 认证 | JWT (HMAC-SHA384, 24h 过期) |
| 前端 | React 19, TypeScript, Vite 8, Tailwind CSS 4 |
| 图表 | ECharts 6 |
| 地图 | Leaflet + React-Leaflet (OpenStreetMap) |
| 图标 | Heroicons 2 |

## 项目结构

```
assetboard/
  pom.xml                          # Maven 父 POM (Spring Boot 3.2.5)
  assetboard-common/               # 共享数据模型和接口
    src/main/java/.../common/
      data/                         # 实体: Asset, Alarm, Customer, Contract, ...
      kv/                           # 键值模型: TsKvEntry, AttributeKvEntry, DataType
      query/                        # 分页: PageLink, PageData<T>
      relation/                     # 关系模型: EntityRelation
  assetboard-dao/                   # 数据访问层
    src/main/java/.../dao/
      sql/entity/                   # JPA 实体 (10 个)
      sql/repository/               # Spring Data JPA Repository (10 个)
      timeseries/                   # 时序 DAO (H2TimeseriesDao)
      attributes/                   # 属性 DAO (H2AttributesDao)
    src/main/resources/sql/
      schema-entities.sql           # 实体表 DDL
      schema-ts.sql                 # 时序表 DDL (ts_kv, ts_kv_latest, attribute_kv)
  assetboard-application/           # 主应用模块
    src/main/java/.../
      controller/                   # REST API (11 个 Controller, 50+ 端点)
      service/                      # 业务逻辑 (12 个 Service)
      security/                     # JWT 认证 (JwtTokenProvider, JwtAuthFilter)
      collector/                    # 数据采集器 (7 个 DataCollector 实现)
      client/                       # 外部 API 客户端 (ShipXy, Clarksons)
    src/main/resources/
      application.yml               # 应用配置
      static/                       # 前端构建产物
  assetboard-ui/                    # React 前端
    src/
      api/                          # Axios API 客户端 (9 个)
      components/                   # 通用组件 (Layout, DataTable, StatusBadge, ...)
      hooks/                        # 自定义 Hook (useAuth, usePagination)
      pages/                        # 页面组件 (14 个)
      constants/                    # 数据源定义
      types/                        # TypeScript 类型
```

## 核心架构

### 实体模型

```
Tenant (租户)
  └── Customer (客户)          # 融资租赁客户
       └── Project (项目)       # 租赁项目
            └── Contract (合同)  # 租赁合同
                 └── Asset (资产) # 船舶/飞机/工程机械
```

所有实体继承 `BaseData`（UUID 主键 + createdTime），通过 `EntityRelation` 建立关系图（五元组合键: fromId, fromType, toId, toType, relationType）。

`EntityType` 枚举：TENANT(0), CUSTOMER(1), PROJECT(2), CONTRACT(3), ASSET(4), ASSET_PROFILE(5), ALARM(6), USER(7)

### 遥测数据模型

```
时序数据 (ts_kv)          ── 带时间戳的指标值（lat, lon, roughValue, flightHours, ...）
最新值缓存 (ts_kv_latest) ── 每个 key 的最新值，用于快速查询
属性数据 (attribute_kv)    ── 静态元数据（船型、注册号、制造商、...）
键值字典 (key_dictionary)  ── 遥测 key 名称的 ID 映射，减少存储冗余
```

数据类型支持：BOOLEAN, LONG, DOUBLE, STRING, JSON

### 数据采集框架

采集器实现 `DataCollector` 接口（`getName()` + `collect()`），通过 Spring `@Scheduled` 定时执行。每个采集器在处理资产前检查 `DataSourceConfig` 的启用状态。

| 采集器 | ID | 数据来源 | 采集类型 | 产出 |
|--------|-----|---------|---------|------|
| ShipTrackCollector | ais | ShipXy API | 时序 | lat, lon, sog, cog |
| WeatherCollector | weather | ShipXy API | 时序 | temperature, humidity, pressure, ... |
| ShipArchiveCollector | archive | ShipXy API | 属性 | 船舶档案（船型、DWT、船级社、...） |
| ValuationCollector | valuation | Clarksons API | 时序 | roughValue, valuationCurrency |
| ShipPscCollector | psc | ShipXy API | 告警 | PSC_DEFICIENCY |
| ShipAlertCollector | alert | ShipXy API | 告警 | SHIP_ALERT_* |
| NavWarningCollector | nav-warning | ShipXy API | 告警 | NAV_WARNING（全局） |

**数据源与资产的关联**：通过 `data_source_config` 表显式绑定。资产创建时根据类型自动分配默认数据源，用户可在前端按资产粒度添加、移除或停用数据源。

### 告警系统

```
告警规则 (AlarmRule)
  ├── 目标类型: vessel / aircraft / equipment
  ├── 监控指标: telemetryKey
  ├── 触发条件: GT / GTE / LT / LTE / EQ + threshold
  ├── 告警级别: CRITICAL / MAJOR / MINOR / WARNING
  └── 告警类型: 自定义（如 VALUATION_DROP, EQUIPMENT_OVERUSE）

告警生命周期: 创建 → 确认(ack) → 清除(clear)
去重策略: 同一 originatorId + type + cleared=false 的告警只存在一条
```

`AlarmRuleEngine` 在每次遥测数据写入时被调用，对匹配的 enabled 规则进行评估。

### 安全架构

- JWT 无状态认证，Bearer Token 通过 `Authorization` 请求头传递
- 公开端点：`/api/auth/**`, `/h2-console/**`
- 其余 `/api/**` 端点需认证
- 密码使用 BCrypt 哈希存储
- 前端 Axios 拦截器自动附加 Token，401 时清除并跳转登录页

### 前端页面

| 路径 | 页面 | 功能 |
|------|------|------|
| `/` | 总览 | KPI 卡片、资产状态环形图、告警级别环形图、合同金额柱状图、船舶位置地图 |
| `/assets` | 资产列表 | 分页列表，按类型筛选 |
| `/assets/:id` | 资产详情 | 6 个 Tab：基本信息、时序数据(ECharts)、轨迹回放(Leaflet)、告警、合同、数据源 |
| `/alarms` | 告警中心 | 分页列表，支持确认/清除操作 |
| `/customers` | 客户管理 | CRUD |
| `/customers/:id` | 客户详情 | 关联项目、授信信息 |
| `/projects` | 项目管理 | CRUD，关联客户 |
| `/contracts` | 合同管理 | CRUD，关联项目 |
| `/settings/alarm-rules` | 告警规则 | 规则 CRUD |
| `/settings/datasources` | 数据源管理 | 按类型分组展示所有数据源，展开查看关联资产采集详情 |

## 快速启动

```bash
# 前置条件: JDK 17+, Maven 3.8+, Node.js 18+

# 1. 构建前端
cd assetboard-ui && npm install && npm run build && cd ..

# 2. 构建后端
mvn install -DskipTests

# 3. 启动
java -jar assetboard-application/target/assetboard-application-0.1.0-SNAPSHOT.jar

# 访问 http://localhost:8080
# 默认账号: admin@assetboard.com / admin123
```

启动时 `MockDataInitializer` 自动创建示例数据：1 个租户、5 个客户、8 个项目、10 份合同、12 个资产（8 船舶 + 2 飞机 + 2 工程机械）、30 条告警、4 条告警规则。

## 外部 API 集成

| 数据源 | 接口 | 配置方式 |
|--------|------|---------|
| ShipXy (船讯网) | AIS 轨迹、气象、船舶档案、PSC、预警 | 环境变量 `SHIPXY_API_KEY`, `SHIPXY_USER_ID` |
| Clarksons (克拉克森) | 船舶估值 | 环境变量 `CLARKSONS_USERNAME`, `CLARKSONS_PASSWORD` |

未配置 API Key 时使用 Mock 客户端，返回预置的模拟数据。

## 数据库

使用 H2 嵌入式数据库（文件模式），数据存储在 `./data/assetboard.mv.db`。

数据库表（13 张）：

| 表名 | 用途 |
|------|------|
| tenant | 租户 |
| tb_user | 用户 |
| customer | 客户 |
| project | 项目 |
| contract | 合同 |
| asset | 资产 |
| asset_profile | 资产档案模板 |
| alarm | 告警 |
| alarm_rule | 告警规则 |
| entity_relation | 实体关系 |
| data_source_config | 数据源配置 |
| ts_kv / ts_kv_latest / key_dictionary | 时序数据 |
| attribute_kv | 属性数据 |

开发时可通过 `http://localhost:8080/h2-console` 访问 H2 控制台（JDBC URL: `jdbc:h2:file:./data/assetboard`）。

---

## 与 ThingsBoard 的对比

### 已实现的核心能力

| 能力 | ThingsBoard | AssetBoard | 状态 |
|------|------------|------------|------|
| 实体管理 | Device/Asset CRUD | Asset + Customer/Project/Contract CRUD | 已实现 |
| 时序数据 | 多协议采集 + 存储 | HTTP 采集器 + H2 存储 | 已实现 |
| 属性管理 | Client/Server/Shared 三类 | 统一属性存储 | 已实现（简化） |
| 告警系统 | 规则引擎触发 | AlarmRule + AlarmRuleEngine | 已实现（简化） |
| 告警生命周期 | 创建/确认/清除 | 创建/确认/清除 | 已实现 |
| 实体关系 | 关系图 | EntityRelation（五元组合键） | 已实现 |
| 分页查询 | PageLink 分页 | PageLink 分页 | 已实现 |
| JWT 认证 | JWT + OAuth2 | JWT | 已实现 |
| REST API | 完整 REST | 50+ 端点 | 已实现 |
| 定时采集 | Rule Engine + Integration | @Scheduled 采集器 | 已实现（简化） |
| 数据源管理 | - | 按资产显式绑定数据源 | 已实现 |

### 缺失功能与未来规划

以下为 ThingsBoard 具备但 AssetBoard 尚未实现的能力，按优先级排列：

#### P0 — 基础能力补全

| 功能 | 说明 | 复杂度 |
|------|------|--------|
| **规则引擎 (Rule Engine)** | ThingsBoard 的核心：可视化规则链（Rule Chain），由消息节点组成有向图，支持数据过滤、转换、外部调用。当前 AssetBoard 仅有简单的阈值告警规则，无法表达复杂业务逻辑。 | 高 |
| **设备连接协议** | ThingsBoard 支持 MQTT, CoAP, HTTP, LwM2M, Modbus, OPC-UA 等。AssetBoard 仅通过 HTTP 轮询外部 API。需要至少实现 MQTT 接入以支持 IoT 设备直连。 | 高 |
| **实时数据推送 (WebSocket)** | ThingsBoard 通过 WebSocket 实现仪表盘实时刷新。AssetBoard 前端目前靠页面加载时一次性拉取，无实时更新能力。 | 中 |
| **多租户隔离** | ThingsBoard 有完整的多租户 Actor 模型。AssetBoard 虽有 tenantId 字段，但目前只有单租户 + 单用户，无租户级资源隔离和配额管理。 | 中 |
| **用户与权限 (RBAC)** | ThingsBoard PE 支持自定义角色和细粒度权限。AssetBoard 仅有 ADMIN/CUSTOMER 两个角色，无权限控制。 | 中 |

#### P1 — 平台能力增强

| 功能 | 说明 | 复杂度 |
|------|------|--------|
| **可视化仪表盘编辑器** | ThingsBoard 提供 600+ Widget 和拖拽式仪表盘编辑。AssetBoard 的总览页和详情页布局是硬编码的。 | 高 |
| **设备配置 (Device Profile)** | ThingsBoard 通过 Device Profile 定义传输协议、告警规则、设备配置。AssetBoard 的 AssetProfile 仅存名称和描述。 | 中 |
| **RPC 双向通信** | ThingsBoard 支持服务端 → 设备和设备 → 服务端的 RPC 调用。AssetBoard 无此能力。 | 中 |
| **数据持久化选择** | ThingsBoard 支持 PostgreSQL + Cassandra/TimescaleDB 组合以应对高吞吐。AssetBoard 使用 H2 嵌入式数据库，不适合生产环境。 | 中 |
| **资产/设备分组** | ThingsBoard 支持 Entity Group 和 Entity View。AssetBoard 通过 type 字段简单分类，无分组聚合能力。 | 低 |

#### P2 — 高级功能

| 功能 | 说明 | 复杂度 |
|------|------|--------|
| **边缘计算 (Edge)** | ThingsBoard Edge 支持本地处理 + 离线运行 + 自动同步。 | 高 |
| **OTA 固件升级** | ThingsBoard 支持设备固件/软件远程升级管理。 | 中 |
| **审计日志** | ThingsBoard 记录所有实体操作的审计日志。 | 低 |
| **数据导出** | ThingsBoard 支持 CSV/Excel 导出和定时报表。 | 低 |
| **API 限流** | ThingsBoard 支持租户级别的 API 限流和配额。 | 低 |
| **OAuth2 / LDAP** | ThingsBoard 支持 OAuth2、LDAP、双因素认证。 | 中 |
| **Kafka / RabbitMQ 集成** | ThingsBoard 规则引擎可将数据推送到消息队列。 | 中 |
| **国际化 (i18n)** | ThingsBoard 支持多语言。AssetBoard 硬编码中文。 | 低 |

### AssetBoard 的差异化设计

AssetBoard 并非 ThingsBoard 的简单翻版，以下设计是针对融资租赁场景的扩展：

| 特性 | 说明 |
|------|------|
| **客户-项目-合同层级** | ThingsBoard 的实体模型为 Tenant → Customer → Device/Asset。AssetBoard 扩展为 Tenant → Customer → Project → Contract → Asset，更贴合金融租赁业务。 |
| **外部数据采集器** | ThingsBoard 以设备直连为主。AssetBoard 设计了采集器框架，主动从第三方 API（船讯网、克拉克森）拉取数据，适合监控非 IoT 的大型资产。 |
| **数据源显式绑定** | 按资产粒度配置数据源（添加/移除/启停），支持跨类型分配。 |
| **融资租赁属性** | 合同金额、币种、出租人/承租人、授信额度、剩余本金等融资租赁专属字段。 |
