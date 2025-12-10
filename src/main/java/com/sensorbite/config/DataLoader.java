/* Copyright (c) 2024 Sensorbite. All rights reserved. */
package com.sensorbite.config;

import com.sensorbite.entity.*;
import com.sensorbite.repository.*;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

  private final BuildingRepository buildingRepository;
  private final FloorRepository floorRepository;
  private final RoomRepository roomRepository;
  private final SensorRepository sensorRepository;
  private final SensorReadingRepository sensorReadingRepository;

  @Override
  public void run(String... args) {
    if (buildingRepository.count() == 0) {
      log.info("Loading sample data...");
      loadSampleData();
      log.info("Sample data loaded successfully!");
    }
  }

  private void loadSampleData() {
    Random random = new Random();

    // Create buildings
    Building building1 = new Building();
    building1.setName("Main Office Building");
    building1.setAddress("123 Tech Street, Silicon Valley, CA 94025");
    buildingRepository.save(building1);

    Building building2 = new Building();
    building2.setName("Research & Development Center");
    building2.setAddress("456 Innovation Ave, San Francisco, CA 94102");
    buildingRepository.save(building2);

    // Create floors for building 1
    Floor floor1_1 = new Floor();
    floor1_1.setLevel(0);
    floor1_1.setBuilding(building1);
    floorRepository.save(floor1_1);

    Floor floor1_2 = new Floor();
    floor1_2.setLevel(1);
    floor1_2.setBuilding(building1);
    floorRepository.save(floor1_2);

    Floor floor1_3 = new Floor();
    floor1_3.setLevel(2);
    floor1_3.setBuilding(building1);
    floorRepository.save(floor1_3);

    // Create floors for building 2
    Floor floor2_1 = new Floor();
    floor2_1.setLevel(0);
    floor2_1.setBuilding(building2);
    floorRepository.save(floor2_1);

    Floor floor2_2 = new Floor();
    floor2_2.setLevel(1);
    floor2_2.setBuilding(building2);
    floorRepository.save(floor2_2);

    // Create rooms for floor 1_1
    Room room101 = new Room();
    room101.setRoomNumber("101");
    room101.setDescription("Main Lobby");
    room101.setFloor(floor1_1);
    roomRepository.save(room101);

    Room room102 = new Room();
    room102.setRoomNumber("102");
    room102.setDescription("Reception Area");
    room102.setFloor(floor1_1);
    roomRepository.save(room102);

    // Create rooms for floor 1_2
    Room room201 = new Room();
    room201.setRoomNumber("201");
    room201.setDescription("Conference Room A");
    room201.setFloor(floor1_2);
    roomRepository.save(room201);

    Room room202 = new Room();
    room202.setRoomNumber("202");
    room202.setDescription("Open Office Space");
    room202.setFloor(floor1_2);
    roomRepository.save(room202);

    // Create rooms for floor 1_3
    Room room301 = new Room();
    room301.setRoomNumber("301");
    room301.setDescription("Server Room");
    room301.setFloor(floor1_3);
    roomRepository.save(room301);

    // Create sensors for room101
    Sensor tempSensor101 = new Sensor();
    tempSensor101.setType("TEMPERATURE");
    tempSensor101.setDescription("Temperature sensor in lobby");
    tempSensor101.setRoom(room101);
    sensorRepository.save(tempSensor101);

    Sensor humiditySensor101 = new Sensor();
    humiditySensor101.setType("HUMIDITY");
    humiditySensor101.setDescription("Humidity sensor in lobby");
    humiditySensor101.setRoom(room101);
    sensorRepository.save(humiditySensor101);

    // Create sensors for room201
    Sensor tempSensor201 = new Sensor();
    tempSensor201.setType("TEMPERATURE");
    tempSensor201.setDescription("Temperature sensor in conference room");
    tempSensor201.setRoom(room201);
    sensorRepository.save(tempSensor201);

    Sensor co2Sensor201 = new Sensor();
    co2Sensor201.setType("CO2");
    co2Sensor201.setDescription("CO2 sensor in conference room");
    co2Sensor201.setRoom(room201);
    sensorRepository.save(co2Sensor201);

    // Create sensors for room301 (server room)
    Sensor tempSensor301 = new Sensor();
    tempSensor301.setType("TEMPERATURE");
    tempSensor301.setDescription("Temperature sensor in server room");
    tempSensor301.setRoom(room301);
    sensorRepository.save(tempSensor301);

    Sensor smokeSensor301 = new Sensor();
    smokeSensor301.setType("SMOKE");
    smokeSensor301.setDescription("Smoke detector in server room");
    smokeSensor301.setRoom(room301);
    sensorRepository.save(smokeSensor301);

    // Create sample readings for temperature sensors (last 24 hours)
    LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i < 24; i++) {
      // Temperature readings for lobby (comfortable range)
      SensorReading reading1 = new SensorReading();
      reading1.setValue(20.0 + random.nextDouble() * 3); // 20-23°C
      reading1.setTimestamp(now.minusHours(i));
      reading1.setSensor(tempSensor101);
      sensorReadingRepository.save(reading1);

      // Temperature readings for conference room
      SensorReading reading2 = new SensorReading();
      reading2.setValue(21.0 + random.nextDouble() * 2); // 21-23°C
      reading2.setTimestamp(now.minusHours(i));
      reading2.setSensor(tempSensor201);
      sensorReadingRepository.save(reading2);

      // Temperature readings for server room (higher and more critical)
      SensorReading reading3 = new SensorReading();
      reading3.setValue(18.0 + random.nextDouble() * 4); // 18-22°C
      reading3.setTimestamp(now.minusHours(i));
      reading3.setSensor(tempSensor301);
      sensorReadingRepository.save(reading3);

      // Humidity readings
      SensorReading reading4 = new SensorReading();
      reading4.setValue(40.0 + random.nextDouble() * 20); // 40-60%
      reading4.setTimestamp(now.minusHours(i));
      reading4.setSensor(humiditySensor101);
      sensorReadingRepository.save(reading4);

      // CO2 readings (in ppm)
      SensorReading reading5 = new SensorReading();
      reading5.setValue(400.0 + random.nextDouble() * 200); // 400-600 ppm
      reading5.setTimestamp(now.minusHours(i));
      reading5.setSensor(co2Sensor201);
      sensorReadingRepository.save(reading5);

      // Smoke readings (0 = no smoke, 1 = smoke detected)
      SensorReading reading6 = new SensorReading();
      reading6.setValue(0.0); // No smoke detected
      reading6.setTimestamp(now.minusHours(i));
      reading6.setSensor(smokeSensor301);
      sensorReadingRepository.save(reading6);
    }

    log.info("Created {} buildings", buildingRepository.count());
    log.info("Created {} floors", floorRepository.count());
    log.info("Created {} rooms", roomRepository.count());
    log.info("Created {} sensors", sensorRepository.count());
    log.info("Created {} sensor readings", sensorReadingRepository.count());
  }
}
