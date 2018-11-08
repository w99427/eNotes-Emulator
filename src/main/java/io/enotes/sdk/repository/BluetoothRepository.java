package io.enotes.sdk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.enotes.sdk.entity.Bluetooth;

public interface BluetoothRepository extends JpaRepository<Bluetooth, Long> {
	List<Bluetooth> getListByAddress(String address);
}
