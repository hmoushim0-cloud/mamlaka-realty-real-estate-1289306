# تطبيق مملكة العقار — مراجعة خبير لمهام التخطيط وا (`mamlaka-realty-real-estate-1289306`)
> منظومة real_estate المتكاملة مع ربط 360 LiDAR Spatial Scanner + Matterport Pro3

- **Project ID**: `#1289306`
- **Commercial Archetype**: `real_estate`
- **Bound Hardware Peripheral**: `360 LiDAR Spatial Scanner + Matterport Pro3`
- **Package Name**: `sa.factory.mamlakarealtyreale`
- **Zero-Emoji Invariant**: Strictly Enforced (100% Vector Icons)


## [Sidekick Autonomous Update]

### Overview of Completed Features
Sidekick has enhanced the Mamlaka Realty PropTech platform with a production-grade 360-degree LiDAR spatial point-cloud parser (Matterport Pro3 protocol compatibility) and an architectural floorplan vectorization engine using Gauss's Shoelace polygon area calculation.

### Implemented Modules & Features
1. **360 LiDAR Spatial Scanner Parser (`LidarSpatialScannerParser.kt`)**:
   - Decodes multi-sample binary packets with `0xA55A` synchronization preambles and 16-bit additive checksums.
   - Converts polar coordinates (azimuth angle $\theta$, distance $r$ in mm) to Cartesian coordinates $(x, y)$ in meters.
   - Implements signal quality filtering ($\ge 15$) and indoor distance validation ($150\text{mm} \le r \le 25,000\text{mm}$) to discard near-field noise and multi-path reflections.

2. **Architectural Floorplan Vectorization Engine (`SpatialFloorplanEngine.kt`)**:
   - Computes accurate polygon surface areas ($m^2$) using Gauss's Shoelace algorithm across arbitrary vertex boundaries.
   - Calculates total spatial boundary perimeters in meters.
   - Implements an architectural room space classifier tailored to Saudi luxury residential properties (`MAJLIS_RECEPTION`, `MASTER_SUITE`, `LIVING_SALON`, `DINING_HALL`, `CORRIDOR`).
   - Computes bounding box dimensions and aspect ratios from raw point clouds.

3. **Autonomous Sensor Driver Telemetry Stream (`HardwareSensorDriver.kt`)**:
   - Upgraded to simulate authentic 360-degree LiDAR sweeps of luxury spaces (Majlis, Master Suite, Living Salon, Corridor).
   - Emits live spatial telemetry displaying detected room category, computed floor area ($m^2$), perimeter, and point cloud confidence scores.

4. **JUnit 4 Feature Test Suite (`MamlakaRealtyFeatureTest.kt`)**:
   - 6 comprehensive tests validating binary frame decoding, Cartesian coordinate conversion, noise reflection rejection, Shoelace area calculation, space classification, and corrupted packet rejection.

### Verification and Test Execution
Run the standalone Kotlin feature tests:
```bash
kotlinc -cp "/usr/share/java/junit4-4.13.2.jar:/usr/share/java/hamcrest-2.2.jar" \
  app/src/main/java/sa/factory/mamlakarealtyreale/model/MamlakaRealtyModels.kt \
  app/src/main/java/sa/factory/mamlakarealtyreale/hardware/LidarSpatialScannerParser.kt \
  app/src/main/java/sa/factory/mamlakarealtyreale/spatial/SpatialFloorplanEngine.kt \
  app/src/main/java/test/java/sa/factory/mamlakarealtyreale/MamlakaRealtyFeatureTest.kt \
  -d /tmp/mamlaka_test_bin

java -cp "/tmp/mamlaka_test_bin:/usr/share/java/junit4-4.13.2.jar:/usr/share/java/hamcrest-2.2.jar:/usr/share/java/kotlin-stdlib.jar" \
  org.junit.runner.JUnitCore sa.factory.mamlakarealtyreale.MamlakaRealtyFeatureTest
```
Result: `OK (6 tests)` passed.
