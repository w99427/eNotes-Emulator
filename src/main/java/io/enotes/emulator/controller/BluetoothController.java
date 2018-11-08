package io.enotes.emulator.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.enotes.emulator.entity.Bluetooth;
import io.enotes.emulator.entity.BluetoothEntity;
import io.enotes.emulator.entity.ConnectEntity;
import io.enotes.emulator.entity.EnotesResponse;
import io.enotes.emulator.repository.BluetoothRepository;

@RestController
@RequestMapping("/sdk/bluetooth/")
public class BluetoothController {
	@Autowired
	private BluetoothRepository bluetoothRepository;

	@GetMapping("/list")
	public EnotesResponse<List<BluetoothEntity>> getBluetoothDevicesList() {
		List<Bluetooth> list = this.bluetoothRepository.findAll();
		List<BluetoothEntity> l = new ArrayList<>();
		if (list == null || list.size() == 0) {
			return new EnotesResponse<List<BluetoothEntity>>(-1, "no data", null);
		}
		for (Bluetooth b : list) {
			BluetoothEntity entity = new BluetoothEntity();
			entity.setName(b.getName());
			entity.setAddress(b.getAddress());
			l.add(entity);
		}
		return new EnotesResponse<List<BluetoothEntity>>(0, "success", l);
	}

	@GetMapping("/connect")
	public EnotesResponse<ConnectEntity> connectBluetooth(
			@RequestParam(value = "address", required = true) String address) {
		List<Bluetooth> list = bluetoothRepository.getListByAddress(address);
		if (list != null && list.size() > 0) {
			return new EnotesResponse<ConnectEntity>(0, "success", new ConnectEntity(list.get(0).getCardid()));
		} else {
			return new EnotesResponse<ConnectEntity>(-1, "no data", null);
		}
	}
}
