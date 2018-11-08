package io.enotes.emulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Stack;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.enotes.emulator.entity.Bluetooth;
import io.enotes.emulator.entity.Card;
import io.enotes.emulator.repository.BluetoothRepository;
import io.enotes.emulator.repository.CardRepository;
import io.enotes.emulator.utils.ECKeyPairGenerator;
import io.enotes.emulator.utils.SpongyCastleProvider;

@SpringBootApplication
public class ENotesEmulatorServerApplication implements CommandLineRunner {
	public static final String TEMP_UPLOAD_DIR=System.getProperty("user.dir")+"/temp/";
	public static final String devicePri = "ef12e6b12e39fba849b0f9985641e72fac5734031d647887cc9e45381fbbd6c7";
	private static final String devicePub = "04a6c9b8116ef8781fe9d9f1ff24a4a0b49794f61cc547a87dc7ec955a8c8c54b56c0ad70e189981843c9e7ac9e5024565668f032dd2a516cd3806f73ff781a5a5";
	private static final String[] certArr = {
			"30ce3081cb30818202030f42401309654e6f7465732e696f180f32303138313033313039333731315a300f020227100404800000000201010400042103a6c9b8116ef8781fe9d9f1ff24a4a0b49794f61cc547a87dc7ec955a8c8c54b5302b1610746573742d30303030303030303035371606323031385133180f32303138313033313039333731315a3044022015672d9bedfb656f396151caa319cf11252197368670063cbe468a4db3a6d4d002203bc378ce4ad106fce3698ac7818c11a50d9b12af384c2f66f46be6ec11dfbc309000",
			"30ce3081cb30818202030f42401309654e6f7465732e696f180f32303138313033313130303133375a300f0202271004048000003c0201030400042103a6c9b8116ef8781fe9d9f1ff24a4a0b49794f61cc547a87dc7ec955a8c8c54b5302b1610746573742d30303030303030303035381606323031385133180f32303138313033313130303133375a3044022071ca5ca66845e4fabda83da10909e811ec76fef320a06166498a5d0d155f3d4e02206ce614c1b2517cb2e492708eaceba50fce09ed9cb7320e7eb99b1b85fe1a742d9000",
			"30cf3081cc30818202030f42401309654e6f7465732e696f180f32303138313033313130303235365a300f0202271004048000003c0201040400042103a6c9b8116ef8781fe9d9f1ff24a4a0b49794f61cc547a87dc7ec955a8c8c54b5302b1610746573742d30303030303030303035391606323031385133180f32303138313033313130303235365a3045022100dd419ae1edec44b1638eb2513dfd63bd1abcc0040112ea892ba2c9f9549dbd3502201340a00acd004e2ad2575778730cda667e2dc0db3343146c0a2580a0f4f281759000",
			"30cf3081cc30818202030f42401309654e6f7465732e696f180f32303138313033313130303631355a300f0202271004048000003c02012a0400042103a6c9b8116ef8781fe9d9f1ff24a4a0b49794f61cc547a87dc7ec955a8c8c54b5302b1610746573742d30303030303030303036301606323031385133180f32303138313033313130303631355a30450221008bedc41a840b81722a107c843d61c5b5eaea642d285c7fa5a5428066a9dba93a0220106e074cfa7c2a46bf292e2c55325e578c70b15d1de1d18571dca396c6ec48419000" };
	private StringBuffer sb = new StringBuffer();
	private Stack<String> priStack = new Stack<>();

	@Autowired
	private BluetoothRepository bluetoothRepository;

	@Autowired
	private CardRepository cardRepository;

	public static void main(String[] args) {
		SpringApplication.run(ENotesEmulatorServerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		createBluetoothList();
	}
	
	/**
	 * add bluetooth list and card list to db
	 */
	private void createBluetoothList() {
		if (bluetoothRepository.count() == 0) {
			readPri();
			Bluetooth b1 = new Bluetooth();
			b1.setName("ACR-eNotes-255");
			b1.setAddress("192.168.10.31");
			b1.setCardid(createCard(0));
			bluetoothRepository.save(b1);

			Bluetooth b2 = new Bluetooth();
			b2.setName("ACR-eNotes-257");
			b2.setAddress("192.168.10.33");
			b2.setCardid(createCard(1));
			bluetoothRepository.save(b2);

			Bluetooth b3 = new Bluetooth();
			b3.setName("ACR-eNotes-259");
			b3.setAddress("192.168.10.35");
			b3.setCardid(createCard(2));
			bluetoothRepository.save(b3);

			Bluetooth b4 = new Bluetooth();
			b4.setName("ACR-eNotes-261");
			b4.setAddress("192.168.10.37");
			b4.setCardid(createCard(3));
			bluetoothRepository.save(b4);
			resotrePri();
		}
	}

	private long createCard(int i) {
		String pubString = "";
		String priString = "";
		if (priStack.isEmpty()) {
			SecureRandom random = new SecureRandom();
			Provider provider = SpongyCastleProvider.getInstance();
			KeyPairGenerator keyPairGen = ECKeyPairGenerator.getInstance(provider, random);
			KeyPair keyPair = keyPairGen.generateKeyPair();
			PrivateKey privKey = keyPair.getPrivate();
			PublicKey pubKey = keyPair.getPublic();
			priString = ((BCECPrivateKey) privKey).getD().toString(16);
			sb.append(priString + "#");
			ECPoint pub = ((BCECPublicKey) pubKey).getQ();
			pubString = ByteUtils.toHexString(pub.getEncoded(false));

		} else {
			String priPop = priStack.pop();
			priString = priPop;
			ECPoint pub = publicPointFromPrivate(new BigInteger(priPop, 16));
			pubString = ByteUtils.toHexString(pub.getEncoded(false));
			System.out.println("publickey_compressed = " + ByteUtils.toHexString(pub.getEncoded(true)));
		}

//		System.out.println("pubString = " + pubString);
//		System.out.println("priString = " + priString);
		Card card = new Card();
		card.setCert(certArr[i]);
		card.setCount(0);
		card.setPrivatekey(priString);
		card.setPublickey(pubString);

		return cardRepository.save(card).getId();
	}

	private static final X9ECParameters curve = SECNamedCurves.getByName("secp256k1");
	private static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(),
			curve.getN(), curve.getH());

	/**
	 * privateKey to PublicKey
	 * @param privKey
	 * @return
	 */
	public static ECPoint publicPointFromPrivate(BigInteger privKey) {
		if (privKey.bitLength() > domain.getN().bitLength()) {
			privKey = privKey.mod(domain.getN());
		}
		return new FixedPointCombMultiplier().multiply(domain.getG(), privKey);
	}

	/**
	 * resotre privateKeys to temp file 
	 */
	private void resotrePri() {
		if (sb.toString() == null || sb.toString().equals(""))
			return;
		try {
			File path = new File(TEMP_UPLOAD_DIR);
			if(!path.exists())path.mkdirs();
			File f = new File(path.getAbsolutePath() + File.separator + "pri.txt");
			FileWriter fw = null;
			BufferedWriter bw = null;
			try {
				if (!f.exists()) {
					f.createNewFile();
				}
				fw = new FileWriter(f.getAbsoluteFile(), false); 
				bw = new BufferedWriter(fw);
				bw.write(sb.toString());
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * read privateKeys from temp file
	 */
	private void readPri() {
		try {
			priStack.clear();
			String s = "";
			File path = new File(TEMP_UPLOAD_DIR);
			if(!path.exists())path.mkdirs();
			File f = new File(path.getAbsolutePath() + File.separator + "pri.txt");
			if(!f.exists())return;
			BufferedReader br = new BufferedReader(new FileReader(f));
			String temp;
			while ((temp = br.readLine()) != null) {
				s += temp;
			}
			if (s != null) {
				String[] arr = s.split("#");
				if (arr.length > 2) {
					if (arr != null && arr.length > 0) {
						for (int i = arr.length - 1; i >= 0; i--) {
							priStack.push(arr[i]);
						}
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
