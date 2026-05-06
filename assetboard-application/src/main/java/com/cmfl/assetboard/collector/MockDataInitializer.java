package com.cmfl.assetboard.collector;

import com.cmfl.assetboard.common.data.*;
import com.cmfl.assetboard.common.data.relation.EntityRelation;
import com.cmfl.assetboard.common.kv.*;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.dao.sql.entity.AssetProfileEntity;
import com.cmfl.assetboard.dao.sql.repository.AssetProfileRepository;
import com.cmfl.assetboard.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("!prod")
public class MockDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MockDataInitializer.class);
    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final ObjectMapper mapper = new ObjectMapper();

    private final AssetProfileService assetProfileService;
    private final AssetProfileRepository assetProfileRepository;
    private final AssetService assetService;
    private final CustomerService customerService;
    private final ProjectService projectService;
    private final ContractService contractService;
    private final UserService userService;
    private final RelationService relationService;
    private final DataSourceConfigService dataSourceConfigService;
    private final TelemetryService telemetryService;
    private final AlarmService alarmService;
    private final AlarmRuleService alarmRuleService;
    private final List<DataCollector> collectors;

    public MockDataInitializer(AssetProfileService assetProfileService,
                               AssetProfileRepository assetProfileRepository,
                               AssetService assetService,
                               CustomerService customerService,
                               ProjectService projectService,
                               ContractService contractService,
                               UserService userService,
                               RelationService relationService,
                               DataSourceConfigService dataSourceConfigService,
                               TelemetryService telemetryService,
                               AlarmService alarmService,
                               AlarmRuleService alarmRuleService,
                               List<DataCollector> collectors) {
        this.assetProfileService = assetProfileService;
        this.assetProfileRepository = assetProfileRepository;
        this.assetService = assetService;
        this.customerService = customerService;
        this.projectService = projectService;
        this.contractService = contractService;
        this.userService = userService;
        this.relationService = relationService;
        this.dataSourceConfigService = dataSourceConfigService;
        this.telemetryService = telemetryService;
        this.alarmService = alarmService;
        this.alarmRuleService = alarmRuleService;
        this.collectors = collectors;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== MockDataInitializer: Starting mock data initialization ===");

        if (!userService.existsByEmail("admin@assetboard.com")) {
            User admin = new User();
            admin.setTenantId(TENANT_ID);
            admin.setEmail("admin@assetboard.com");
            admin.setAuthority(Authority.SYS_ADMIN);
            admin.setName("Admin");
            userService.createUser(admin, "admin123");
            log.info("Default admin user created: admin@assetboard.com / admin123");
        }

        var existing = assetService.findByTenantIdAndType(TENANT_ID, "vessel", new PageLink(1, 0));
        if (existing.getTotalElements() > 0) {
            log.info("Mock data already exists, skipping initialization. Running collectors only.");
            runCollectors();
            return;
        }

        // --- Asset Profiles ---
        AssetProfile vesselProfile = findOrCreateProfile("船舶", "船舶租赁物模板");
        AssetProfile aircraftProfile = findOrCreateProfile("飞机", "飞机租赁物模板");
        AssetProfile equipmentProfile = findOrCreateProfile("工程机械", "工程机械租赁物模板");
        log.info("Asset profiles created: 船舶, 飞机, 工程机械");

        // --- Customers ---
        Customer c1 = createCustomer("招商轮船股份有限公司", "5000000000", "3200000000", "深圳市南山区蛇口港湾大道8号");
        Customer c2 = createCustomer("中远海运集团有限公司", "8000000000", "5500000000", "上海市浦东新区东方路1228号");
        Customer c3 = createCustomer("国航租赁有限公司", "12000000000", "7800000000", "北京市朝阳区霄云路36号");
        Customer c4 = createCustomer("三一重工股份有限公司", "3000000000", "1800000000", "长沙市经济开发区三一路21号");
        Customer c5 = createCustomer("山东海运股份有限公司", "1500000000", "950000000", "青岛市市南区香港中路18号");
        log.info("Created 5 customers");

        // --- Projects ---
        Project p1 = createProject(c1, "招商轮船散货船融资租赁项目", "PRJ-2024-001", "直租", "融资租赁");
        Project p2 = createProject(c1, "招商轮船油轮经营租赁项目", "PRJ-2024-002", "经营性租赁", "经营租赁");
        Project p3 = createProject(c2, "中远海运集装箱船融资项目", "PRJ-2024-003", "直租", "融资租赁");
        Project p4 = createProject(c2, "中远海运散货船售后回租项目", "PRJ-2024-004", "售后回租", "融资租赁");
        Project p5 = createProject(c3, "国航窄体机融资租赁项目", "PRJ-2024-005", "直租", "融资租赁");
        Project p6 = createProject(c3, "国航宽体机经营租赁项目", "PRJ-2024-006", "经营性租赁", "经营租赁");
        Project p7 = createProject(c4, "三一重工挖掘机融资租赁项目", "PRJ-2024-007", "直租", "融资租赁");
        Project p8 = createProject(c5, "山东海运散货船售后回租项目", "PRJ-2024-008", "售后回租", "融资租赁");
        log.info("Created 8 projects");

        // --- Contracts ---
        long now = System.currentTimeMillis();
        long sixMonthsAgo = now - 180L * 86400000;
        long oneYearAgo = now - 365L * 86400000;
        long twoYearsAgo = now - 730L * 86400000;

        Contract ctr1 = createContract(p1, "CTR-2024-001", "350000000", "USD", "招商金融租赁有限公司", "招商轮船股份有限公司", "ACTIVE", oneYearAgo);
        Contract ctr2 = createContract(p2, "CTR-2024-002", "180000000", "USD", "招商金融租赁有限公司", "招商轮船股份有限公司", "ACTIVE", sixMonthsAgo);
        Contract ctr3 = createContract(p3, "CTR-2024-003", "520000000", "USD", "交银金融租赁有限公司", "中远海运集团有限公司", "ACTIVE", oneYearAgo);
        Contract ctr4 = createContract(p4, "CTR-2024-004", "280000000", "USD", "工银金融租赁有限公司", "中远海运集团有限公司", "ACTIVE", sixMonthsAgo);
        Contract ctr5 = createContract(p5, "CTR-2024-005", "850000000", "USD", "国银金融租赁有限公司", "国航租赁有限公司", "ACTIVE", oneYearAgo);
        Contract ctr6 = createContract(p6, "CTR-2024-006", "1200000000", "USD", "建信金融租赁有限公司", "国航租赁有限公司", "ACTIVE", sixMonthsAgo);
        Contract ctr7 = createContract(p7, "CTR-2024-007", "45000000", "CNY", "三一汽车金融有限公司", "三一重工股份有限公司", "ACTIVE", oneYearAgo);
        Contract ctr8 = createContract(p7, "CTR-2024-008", "32000000", "CNY", "三一汽车金融有限公司", "三一重工股份有限公司", "EXPIRED", twoYearsAgo);
        Contract ctr9 = createContract(p8, "CTR-2024-009", "120000000", "USD", "民生金融租赁股份有限公司", "山东海运股份有限公司", "ACTIVE", sixMonthsAgo);
        Contract ctr10 = createContract(p8, "CTR-2024-010", "80000000", "USD", "民生金融租赁股份有限公司", "山东海运股份有限公司", "TERMINATED", twoYearsAgo);
        log.info("Created 10 contracts");

        // --- Vessel Assets (8) ---
        Asset v1 = createVessel(vesselProfile, "TIAN EN", "9488918", "477245400", c1);
        Asset v2 = createVessel(vesselProfile, "TOYOKUNI MARU No.3", "9134414", "431500399", c1);
        Asset v3 = createVessel(vesselProfile, "ORIENT TARGET", "9838222", "477181700", c1);
        Asset v4 = createVessel(vesselProfile, "COSCO SHIPPING ARIES", "9757876", "477375200", c2);
        Asset v5 = createVessel(vesselProfile, "COSCO SHIPPING LEO", "9757888", "477375300", c2);
        Asset v6 = createVessel(vesselProfile, "XIN SHANGHAI", "9314222", "477195600", c2);
        Asset v7 = createVessel(vesselProfile, "SHAN DONG HAI YUN 01", "9283243", "413701400", c5);
        Asset v8 = createVesselWithStatus(vesselProfile, "SHAN DONG HAI YUN 05", "9348981", "413455000", c5, AssetStatus.DISPOSED);

        // Contract → Vessel relations
        createRelation(ctr1.getId(), EntityType.CONTRACT, v1.getId(), EntityType.ASSET);
        createRelation(ctr1.getId(), EntityType.CONTRACT, v2.getId(), EntityType.ASSET);
        createRelation(ctr2.getId(), EntityType.CONTRACT, v3.getId(), EntityType.ASSET);
        createRelation(ctr3.getId(), EntityType.CONTRACT, v4.getId(), EntityType.ASSET);
        createRelation(ctr3.getId(), EntityType.CONTRACT, v5.getId(), EntityType.ASSET);
        createRelation(ctr4.getId(), EntityType.CONTRACT, v6.getId(), EntityType.ASSET);
        createRelation(ctr9.getId(), EntityType.CONTRACT, v7.getId(), EntityType.ASSET);
        createRelation(ctr10.getId(), EntityType.CONTRACT, v8.getId(), EntityType.ASSET);

        // DataSourceConfig for all vessels
        List<Asset> vessels = List.of(v1, v2, v3, v4, v5, v6, v7, v8);
        vessels.forEach(v -> createDefaultDataSourceConfigs(v.getId(), "vessel"));
        log.info("Created 8 vessel assets with relations and data source configs");

        // --- Aircraft Assets (2) ---
        Asset a1 = createAircraft(aircraftProfile, "B-308A (A320neo)", "B-308A", "A320neo", c3);
        Asset a2 = createAircraft(aircraftProfile, "B-209C (B787-9)", "B-209C", "B787-9", c3);
        createRelation(ctr5.getId(), EntityType.CONTRACT, a1.getId(), EntityType.ASSET);
        createRelation(ctr6.getId(), EntityType.CONTRACT, a2.getId(), EntityType.ASSET);
        createDefaultDataSourceConfigs(a1.getId(), "aircraft");
        createDefaultDataSourceConfigs(a2.getId(), "aircraft");
        log.info("Created 2 aircraft assets");

        // --- Equipment Assets (2) ---
        Asset e1 = createEquipment(equipmentProfile, "SY490H-10 大型挖掘机", "EQ-2024-001", "SY490H", c4);
        Asset e2 = createEquipmentWithStatus(equipmentProfile, "SRT95C 矿用卡车", "EQ-2024-002", "SRT95C", c4, AssetStatus.DISPOSED);
        createRelation(ctr7.getId(), EntityType.CONTRACT, e1.getId(), EntityType.ASSET);
        createRelation(ctr8.getId(), EntityType.CONTRACT, e2.getId(), EntityType.ASSET);
        createDefaultDataSourceConfigs(e1.getId(), "equipment");
        createDefaultDataSourceConfigs(e2.getId(), "equipment");
        log.info("Created 2 equipment assets");

        // --- Alarm Rules ---
        createAlarmRule("船舶估值低于50万", "vessel", "roughValue", "LT", 50.0, AlarmSeverity.MAJOR, "VALUATION_DROP");
        createAlarmRule("高温预警", "vessel", "temperature", "GT", 40.0, AlarmSeverity.WARNING, "HIGH_TEMPERATURE");
        createAlarmRule("飞机飞行小时超限", "aircraft", "flightHours", "GT", 8000.0, AlarmSeverity.MAJOR, "FLIGHT_HOURS_EXCEED");
        createAlarmRule("设备运行超限", "equipment", "operatingHours", "GT", 5000.0, AlarmSeverity.WARNING, "EQUIPMENT_OVERUSE");
        log.info("Created 4 alarm rules");

        // --- Synthetic data for aircraft ---
        generateAircraftAttributes(a1, "空客", "A320neo", "B-308A", 186, 6300, oneYearAgo, now - 30L * 86400000);
        generateAircraftAttributes(a2, "波音", "B787-9", "B-209C", 290, 14140, sixMonthsAgo, now - 15L * 86400000);
        generateAircraftTimeseries(a1, 4200, 2800, 52.0, 2650, now);
        generateAircraftTimeseries(a2, 3100, 1950, 135.0, 5800, now);
        generateAircraftAlarms(a1, now);
        generateAircraftAlarms(a2, now);
        log.info("Generated synthetic data for aircraft");

        // --- Synthetic data for equipment ---
        generateEquipmentAttributes(e1, "三一重工", "SY490H", "SY490H2024001", 270, 49.0, oneYearAgo);
        generateEquipmentAttributes(e2, "三一重工", "SRT95C", "SRT95C2024002", 783, 95.0, twoYearsAgo);
        generateEquipmentTimeseries(e1, 2800, 380, 850, 28.112, 112.45, now);
        generateEquipmentTimeseries(e2, 5200, 520, 420, 27.985, 112.52, now);
        generateEquipmentAlarms(e1, now);
        generateEquipmentAlarms(e2, now);
        log.info("Generated synthetic data for equipment");

        // --- Run collectors for vessels ---
        runCollectors();

        // --- Fallback: generate synthetic data for vessels without collector data ---
        generateVesselFallbackData(vessels, now);

        log.info("=== MockDataInitializer: Initialization complete ===");
    }

    // ========== Helper: Profiles ==========

    private AssetProfile findOrCreateProfile(String name, String description) {
        return assetProfileRepository.findByTenantIdAndName(TENANT_ID, name)
                .map(AssetProfileEntity::toData)
                .orElseGet(() -> {
                    AssetProfile p = new AssetProfile();
                    p.setTenantId(TENANT_ID);
                    p.setName(name);
                    p.setDescription(description);
                    return assetProfileService.save(p);
                });
    }

    // ========== Helper: Customers ==========

    private Customer createCustomer(String name, String credit, String remaining, String address) {
        Customer c = new Customer();
        c.setTenantId(TENANT_ID);
        c.setName(name);
        c.setCreditAmount(new BigDecimal(credit));
        c.setRemainingPrincipal(new BigDecimal(remaining));
        c.setAddress(address);
        return customerService.save(c);
    }

    // ========== Helper: Projects ==========

    private Project createProject(Customer customer, String name, String projectNo, String businessType, String leaseType) {
        Project p = new Project();
        p.setTenantId(TENANT_ID);
        p.setCustomerId(customer.getId());
        p.setName(name);
        p.setProjectNo(projectNo);
        p.setBusinessType(businessType);
        p.setLeaseType(leaseType);
        return projectService.save(p);
    }

    // ========== Helper: Contracts ==========

    private Contract createContract(Project project, String contractNo, String amount, String currency,
                                    String lessor, String lessee, String status, long signDate) {
        Contract c = new Contract();
        c.setProjectId(project.getId());
        c.setContractNo(contractNo);
        c.setAmount(new BigDecimal(amount));
        c.setCurrency(currency);
        c.setLessor(lessor);
        c.setLessee(lessee);
        c.setStatus(status);
        c.setSignDate(signDate);
        return contractService.save(c);
    }

    // ========== Helper: Assets ==========

    private Asset createVessel(AssetProfile profile, String name, String imo, String mmsi, Customer customer) {
        return createVesselWithStatus(profile, name, imo, mmsi, customer, AssetStatus.IN_LEASE);
    }

    private Asset createVesselWithStatus(AssetProfile profile, String name, String imo, String mmsi, Customer customer, AssetStatus status) {
        Asset asset = new Asset();
        asset.setTenantId(TENANT_ID);
        asset.setCustomerId(customer.getId());
        asset.setAssetProfileId(profile.getId());
        asset.setName(name);
        asset.setType("vessel");
        asset.setLabel(name);
        asset.setStatus(status);
        ObjectNode info = mapper.createObjectNode();
        info.put("imo", imo);
        info.put("mmsi", mmsi);
        asset.setAdditionalInfo(info);
        return assetService.save(asset);
    }

    private Asset createAircraft(AssetProfile profile, String name, String regNo, String model, Customer customer) {
        Asset asset = new Asset();
        asset.setTenantId(TENANT_ID);
        asset.setCustomerId(customer.getId());
        asset.setAssetProfileId(profile.getId());
        asset.setName(name);
        asset.setType("aircraft");
        asset.setLabel(name);
        asset.setStatus(AssetStatus.IN_LEASE);
        ObjectNode info = mapper.createObjectNode();
        info.put("registrationNo", regNo);
        info.put("model", model);
        asset.setAdditionalInfo(info);
        return assetService.save(asset);
    }

    private Asset createEquipment(AssetProfile profile, String name, String serialNo, String model, Customer customer) {
        return createEquipmentWithStatus(profile, name, serialNo, model, customer, AssetStatus.IN_LEASE);
    }

    private Asset createEquipmentWithStatus(AssetProfile profile, String name, String serialNo, String model, Customer customer, AssetStatus status) {
        Asset asset = new Asset();
        asset.setTenantId(TENANT_ID);
        asset.setCustomerId(customer.getId());
        asset.setAssetProfileId(profile.getId());
        asset.setName(name);
        asset.setType("equipment");
        asset.setLabel(name);
        asset.setStatus(status);
        ObjectNode info = mapper.createObjectNode();
        info.put("serialNo", serialNo);
        info.put("model", model);
        asset.setAdditionalInfo(info);
        return assetService.save(asset);
    }

    // ========== Helper: Relations ==========

    private void createRelation(UUID fromId, EntityType fromType, UUID toId, EntityType toType) {
        EntityRelation rel = new EntityRelation();
        rel.setFromId(fromId);
        rel.setFromType(fromType);
        rel.setToId(toId);
        rel.setToType(toType);
        rel.setRelationType("CONTAINS");
        relationService.save(rel);
    }

    // ========== Helper: DataSourceConfig ==========

    private void createDefaultDataSourceConfigs(UUID assetId, String assetType) {
        String[] collectorIds;
        switch (assetType) {
            case "aircraft" -> collectorIds = new String[]{"aircraft-flight", "aircraft-valuation", "aircraft-alarm"};
            case "equipment" -> collectorIds = new String[]{"equipment-iot", "equipment-valuation", "equipment-alarm"};
            default -> collectorIds = new String[]{"ais", "weather", "archive", "valuation", "psc", "alert"};
        }
        for (String cid : collectorIds) {
            DataSourceConfig cfg = new DataSourceConfig();
            cfg.setAssetId(assetId);
            cfg.setCollectorId(cid);
            cfg.setEnabled(true);
            dataSourceConfigService.save(cfg);
        }
    }

    // ========== Helper: Alarm Rules ==========

    private void createAlarmRule(String name, String targetType, String telemetryKey, String condition,
                                 double threshold, AlarmSeverity severity, String alarmType) {
        AlarmRule rule = new AlarmRule();
        rule.setTenantId(TENANT_ID);
        rule.setName(name);
        rule.setTargetType(targetType);
        rule.setTelemetryKey(telemetryKey);
        rule.setCondition(condition);
        rule.setThreshold(threshold);
        rule.setSeverity(severity);
        rule.setAlarmType(alarmType);
        rule.setEnabled(true);
        alarmRuleService.save(rule);
    }

    // ========== Synthetic: Aircraft Attributes ==========

    private void generateAircraftAttributes(Asset aircraft, String manufacturer, String model,
                                            String regNo, int seatCount, int maxRange,
                                            long deliveryDate, long lastMaintenance) {
        long now = System.currentTimeMillis();
        saveAttr(aircraft.getId(), now, "manufacturer", manufacturer);
        saveAttr(aircraft.getId(), now, "model", model);
        saveAttr(aircraft.getId(), now, "registrationNo", regNo);
        saveAttr(aircraft.getId(), now, "seatCount", String.valueOf(seatCount));
        saveAttr(aircraft.getId(), now, "maxRange", maxRange + " km");
        saveAttr(aircraft.getId(), now, "deliveryDate", formatDate(deliveryDate));
        saveAttr(aircraft.getId(), now, "lastMaintenance", formatDate(lastMaintenance));
    }

    // ========== Synthetic: Aircraft Timeseries ==========

    private void generateAircraftTimeseries(Asset aircraft, double baseFlightHours, double baseCycles,
                                            double baseValue, double baseFuelEff, long now) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        long dayMs = 86400000L;

        for (int d = 365; d >= 0; d--) {
            long ts = now - d * dayMs + rng.nextLong(0, 3600000);
            double dayFraction = (365 - d) / 365.0;

            double flightHours = baseFlightHours + dayFraction * 1800 + rng.nextDouble(-5, 5);
            double cycles = baseCycles + dayFraction * 1200 + rng.nextDouble(-3, 3);
            double value = baseValue - dayFraction * baseValue * 0.08 + rng.nextDouble(-0.5, 0.5);
            double fuelEff = baseFuelEff + rng.nextDouble(-80, 80);

            List<TsKvEntry> entries = List.of(
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("flightHours", round2(flightHours))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("cycleCount", round2(cycles))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("roughValue", round2(value))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("fuelEfficiency", round2(fuelEff)))
            );
            telemetryService.saveTimeseriesBatch(aircraft.getId(), entries);
        }
    }

    // ========== Synthetic: Aircraft Alarms ==========

    private void generateAircraftAlarms(Asset aircraft, long now) {
        long dayMs = 86400000L;

        createAlarm(aircraft, "MAINTENANCE_DUE", AlarmSeverity.MINOR,
                now - 60 * dayMs, "定期维护到期提醒", true, true);
        createAlarm(aircraft, "MAINTENANCE_DUE", AlarmSeverity.WARNING,
                now - 20 * dayMs, "发动机A检到期", true, false);
        createAlarm(aircraft, "FLIGHT_HOURS_EXCEED", AlarmSeverity.MAJOR,
                now - 5 * dayMs, "累计飞行小时接近限制", false, false);
    }

    // ========== Synthetic: Equipment Attributes ==========

    private void generateEquipmentAttributes(Asset equipment, String manufacturer, String model,
                                             String serialNo, int enginePower, double weight, long deliveryDate) {
        long now = System.currentTimeMillis();
        saveAttr(equipment.getId(), now, "manufacturer", manufacturer);
        saveAttr(equipment.getId(), now, "model", model);
        saveAttr(equipment.getId(), now, "serialNo", serialNo);
        saveAttr(equipment.getId(), now, "enginePower", enginePower + " kW");
        saveAttr(equipment.getId(), now, "weight", weight + " t");
        saveAttr(equipment.getId(), now, "deliveryDate", formatDate(deliveryDate));
    }

    // ========== Synthetic: Equipment Timeseries ==========

    private void generateEquipmentTimeseries(Asset equipment, double baseHours, double baseFuel,
                                             double baseValue, double baseLat, double baseLon, long now) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        long dayMs = 86400000L;

        for (int d = 365; d >= 0; d--) {
            long ts = now - d * dayMs + rng.nextLong(0, 3600000);
            double dayFraction = (365 - d) / 365.0;

            double hours = baseHours + dayFraction * 2000 + rng.nextDouble(-8, 8);
            double fuel = baseFuel + rng.nextDouble(-60, 60);
            double value = baseValue - dayFraction * baseValue * 0.15 + rng.nextDouble(-5, 5);
            double lat = baseLat + rng.nextDouble(-0.005, 0.005);
            double lon = baseLon + rng.nextDouble(-0.005, 0.005);

            List<TsKvEntry> entries = List.of(
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("operatingHours", round2(hours))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("fuelConsumption", round2(fuel))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("roughValue", round2(value))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("lat", round6(lat))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("lon", round6(lon)))
            );
            telemetryService.saveTimeseriesBatch(equipment.getId(), entries);
        }
    }

    // ========== Synthetic: Equipment Alarms ==========

    private void generateEquipmentAlarms(Asset equipment, long now) {
        long dayMs = 86400000L;

        createAlarm(equipment, "EQUIPMENT_OVERUSE", AlarmSeverity.WARNING,
                now - 45 * dayMs, "设备连续作业超过规定时长", true, true);
        createAlarm(equipment, "MAINTENANCE_DUE", AlarmSeverity.MINOR,
                now - 15 * dayMs, "液压系统保养到期", true, false);
        createAlarm(equipment, "EQUIPMENT_OVERUSE", AlarmSeverity.MAJOR,
                now - 3 * dayMs, "累计运行小时接近上限", false, false);
    }

    // ========== Synthetic: Vessel Fallback ==========

    private void generateVesselFallbackData(List<Asset> vessels, long now) {
        // Predefined positions for vessels without real AIS data (major shipping routes)
        double[][] defaultPositions = {
            {22.3, 114.2},   // 南海 - 香港附近
            {31.2, 121.5},   // 东海 - 上海附近
            {1.3, 103.8},    // 新加坡海峡
            {35.6, 139.7},   // 东京湾
            {36.0, 120.4},   // 黄海 - 青岛附近
            {25.0, 121.5},   // 台湾海峡
        };

        String[] vesselTypes = {"散货船", "散货船", "油轮", "集装箱船", "集装箱船", "散货船", "散货船", "散货船"};
        String[] flags = {"香港", "日本", "香港", "香港", "香港", "香港", "中国", "中国"};
        int[] dwts = {180000, 76000, 115000, 141000, 141000, 99000, 57000, 75000};

        int posIdx = 0;
        for (int i = 0; i < vessels.size(); i++) {
            Asset v = vessels.get(i);
            var existing = telemetryService.findLatestTimeseries(v.getId(), "lat");
            if (existing != null) {
                log.info("Vessel {} already has collector data, skipping fallback", v.getName());
                continue;
            }

            log.info("Generating fallback synthetic data for vessel: {}", v.getName());
            double[] pos = defaultPositions[posIdx % defaultPositions.length];
            posIdx++;

            // Attributes
            long ts = System.currentTimeMillis();
            String imo = "";
            if (v.getAdditionalInfo() != null && v.getAdditionalInfo().has("imo")) {
                imo = v.getAdditionalInfo().get("imo").asText();
            }
            saveAttr(v.getId(), ts, "shipType", vesselTypes[i]);
            saveAttr(v.getId(), ts, "flag", flags[i]);
            saveAttr(v.getId(), ts, "imo", imo);
            saveAttr(v.getId(), ts, "dwt", dwts[i] + " t");
            saveAttr(v.getId(), ts, "yearBuilt", String.valueOf(2010 + i % 8));
            saveAttr(v.getId(), ts, "classificationSociety", i % 2 == 0 ? "DNV" : "Lloyd's Register");

            // Timeseries: lat, lon, sog, cog, roughValue, temperature
            generateVesselTimeseries(v, pos[0], pos[1], dwts[i] * 0.0003, now);

            // Alarms
            generateVesselAlarms(v, now);
        }
    }

    private void generateVesselTimeseries(Asset vessel, double baseLat, double baseLon, double baseValue, long now) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        long dayMs = 86400000L;

        for (int d = 365; d >= 0; d--) {
            long ts = now - d * dayMs + rng.nextLong(0, 3600000);
            double dayFraction = (365 - d) / 365.0;

            double lat = baseLat + dayFraction * 5 * Math.sin(dayFraction * Math.PI * 4) + rng.nextDouble(-0.2, 0.2);
            double lon = baseLon + dayFraction * 8 * Math.cos(dayFraction * Math.PI * 3) + rng.nextDouble(-0.2, 0.2);
            double sog = 10 + rng.nextDouble(-3, 3);
            double cog = rng.nextDouble(0, 360);
            double value = baseValue - dayFraction * baseValue * 0.06 + rng.nextDouble(-0.3, 0.3);
            double temp = 20 + 10 * Math.sin((dayFraction - 0.25) * Math.PI * 2) + rng.nextDouble(-2, 2);

            List<TsKvEntry> entries = List.of(
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("lat", round6(lat))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("lon", round6(lon))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("sog", round2(sog))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("cog", round2(cog))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("roughValue", round2(value))),
                    new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("temperature", round2(temp)))
            );
            telemetryService.saveTimeseriesBatch(vessel.getId(), entries);
        }
    }

    private void generateVesselAlarms(Asset vessel, long now) {
        long dayMs = 86400000L;
        createAlarm(vessel, "PSC_DEFICIENCY", AlarmSeverity.MINOR,
                now - 90 * dayMs, "PSC检查发现轻微缺陷", true, true);
        createAlarm(vessel, "VALUATION_DROP", AlarmSeverity.MAJOR,
                now - 7 * dayMs, "船舶估值低于阈值", false, false);
    }

    // ========== Common Helpers ==========

    private void saveAttr(UUID entityId, long ts, String key, String value) {
        telemetryService.saveAttribute(entityId, new BaseAttributeKvEntry(ts, BasicKvEntry.ofString(key, value)));
    }

    private void createAlarm(Asset asset, String type, AlarmSeverity severity, long startTs,
                             String detailMsg, boolean acknowledged, boolean cleared) {
        Alarm alarm = new Alarm();
        alarm.setTenantId(TENANT_ID);
        alarm.setOriginatorId(asset.getId());
        alarm.setOriginatorType(EntityType.ASSET);
        alarm.setType(type);
        alarm.setSeverity(severity);
        alarm.setStartTs(startTs);
        alarm.setAcknowledged(acknowledged);
        alarm.setCleared(cleared);
        if (acknowledged) alarm.setAckTs(startTs + 3600000L);
        if (cleared) alarm.setClearTs(startTs + 86400000L);
        ObjectNode details = mapper.createObjectNode();
        details.put("message", detailMsg);
        alarm.setDetails(details);
        alarmService.createOrUpdate(alarm);
    }

    private void runCollectors() {
        log.info("Running all collectors...");
        for (DataCollector collector : collectors) {
            try {
                collector.collect();
            } catch (Exception e) {
                log.error("Collector {} failed", collector.getName(), e);
            }
        }
    }

    private String formatDate(long ts) {
        return java.time.Instant.ofEpochMilli(ts).atZone(java.time.ZoneId.of("Asia/Shanghai"))
                .toLocalDate().toString();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static double round6(double v) {
        return Math.round(v * 1000000.0) / 1000000.0;
    }
}
