package io.enotes.emulator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.enotes.emulator.entity.Bluetooth;

public interface BluetoothRepository extends JpaRepository<Bluetooth, Long> {
	List<Bluetooth> getListByAddress(String address);
}
