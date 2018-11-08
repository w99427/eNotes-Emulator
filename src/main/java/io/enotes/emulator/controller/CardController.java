package io.enotes.emulator.controller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.enotes.emulator.ENotesEmulatorServerApplication;
import io.enotes.emulator.entity.ApduEntity;
import io.enotes.emulator.entity.Card;
import io.enotes.emulator.entity.EnotesResponse;
import io.enotes.emulator.repository.CardRepository;
import io.enotes.emulator.utils.ByteUtil;
import io.enotes.emulator.utils.TLVBox;

@RestController
@RequestMapping("/sdk/card/")
public class CardController {
	private static final X9ECParameters curve = CustomNamedCurves.getByName("secp256k1");
	private static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(),
			curve.getH());
	static final BigInteger HALF_CURVE_ORDER = domain.getN().shiftRight(1);

	@Autowired
	private CardRepository cardRepository;

	@GetMapping("/list")
	public EnotesResponse<List<Card>> getCardList() {
		List<Card> list = this.cardRepository.findAll();
		return new EnotesResponse<List<Card>>(0, "success", list);
	}

	@GetMapping("/reset")
	public EnotesResponse<Object> resetCard(@RequestParam(value = "id", defaultValue = "0") Long id) {
		Card card = cardRepository.findOne(id);
		card.setCount(0);
		cardRepository.save(card);
		return new EnotesResponse<Object>(0, "success", null);
	}

	@PostMapping("/transceive")
	public EnotesResponse<ApduEntity> transceiveApdu(@RequestParam(value = "id", defaultValue = "0") Long id,
			@RequestParam(value = "apdu", defaultValue = "0") String apdu) {
		System.out.println("apdu = "+ apdu);
		ApduEntity apduEntity = new ApduEntity("");
		Card card = cardRepository.findOne(id);
		if (apdu.equals("00CA0030")) {// read cert
			apduEntity.setResult(card.getCert());
		} else if (apdu.equals("00CA0055")) {// read publickey
			apduEntity.setResult("5541" + card.getPublickey() + "9000");
		} else if (apdu.equals("00CA0090")) {// read status
			int count = card.getCount();
			TLVBox tlvBox = new TLVBox();
			tlvBox.putBytesValue(TLVTag.Transaction_Signature_Counter, IntToByte(count));
			apduEntity.setResult(ByteUtil.toHexString(tlvBox.serialize()) + "9000");
		} else if (apdu.contains("0088520022")) {
			signBySalt(ENotesEmulatorServerApplication.devicePri, apduEntity, apdu.substring("0088520022".length()));
		} else if (apdu.contains("0088540022")) {
			signBySalt(card.getPrivatekey(), apduEntity, apdu.substring("0088540022".length()));
		} else if (apdu.contains("00A0540022")) {
			signForNoHash(apduEntity, card, apdu.substring("00A0540022".length()));
		}  else if (apdu.contains("00a0540022")) {
			signForNoHash(apduEntity, card, apdu.substring("00a0540022".length()));
		} else if (apdu.contains("00A40400")) {
			apduEntity.setResult("9000");
		}
		if (!apduEntity.getResult().equals(""))
			return new EnotesResponse<ApduEntity>(0, "success", apduEntity);
		else
			return new EnotesResponse<ApduEntity>(-1, "no data", null);
	}

	private void signBySalt(String privateKey, ApduEntity entity, String tlv) {
		byte[] rl = nextRandomBytes();
		String rlString = ByteUtil.toHexString(rl);
		byte[] bytes = ByteUtil.hexStringToBytes(tlv);
		try {
			TLVBox tlvBox = TLVBox.parse(bytes, 0, bytes.length);
			String random = ByteUtil.toHexString(tlvBox.getBytesValue(TLVTag.Challenge));
			signForTwiceHash(entity, rl, privateKey, ByteUtil.hexStringToBytes(random + rlString));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void signForTwiceHash(ApduEntity entity, byte[] rl, String privateKey, byte[] data) {
		SHA256Digest s_SHA256Digest = new SHA256Digest();
		s_SHA256Digest.update(data, 0, data.length);
		byte hash[] = new byte[32];
		s_SHA256Digest.doFinal(hash, 0);
		
		s_SHA256Digest.reset();
		s_SHA256Digest.update(hash, 0, hash.length);
		byte db_hash[] = new byte[32];
		s_SHA256Digest.doFinal(db_hash, 0);

		ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
		signer.init(true, new ECPrivateKeyParameters(new BigInteger(privateKey, 16), domain));
		
		BigInteger[] generateSignature = signer.generateSignature(db_hash);
		if (generateSignature != null && generateSignature.length == 2) {
			System.out.println("sign1 = "+generateSignature[0].toString(16).length()+"/n"+generateSignature[1].toString(16).length());
			BigInteger s =generateSignature[1];
			if(s.compareTo(HALF_CURVE_ORDER) <= 0) {
				s=domain.getN().subtract(generateSignature[1]);
			}
			String rHex = generateSignature[0].toString(16);
			rHex = rHex.length()==63?("0"+rHex):rHex;
			String sHex = s.toString(16);
			sHex = sHex.length()==63?("0"+sHex):sHex;
			String signString =rHex+sHex;

			TLVBox tlvBox = new TLVBox();
			tlvBox.putBytesValue(TLVTag.Verification_Signature, ByteUtil.hexStringToBytes(signString));
			TLVBox tlvBox1 = new TLVBox();
			tlvBox1.putBytesValue(TLVTag.Salt, rl);
			entity.setResult(
					ByteUtil.toHexString(tlvBox.serialize()) + ByteUtil.toHexString(tlvBox1.serialize()) + "9000");
		}

	}




	private void signForNoHash(ApduEntity entity, Card card, String tlv) {
		byte[] bytes = ByteUtil.hexStringToBytes(tlv);
		TLVBox tlvBox1;
		try {
			tlvBox1 = TLVBox.parse(bytes, 0, bytes.length);
			byte[] data = tlvBox1.getBytesValue(TLVTag.Transaction_Hash);
			ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
			signer.init(true, new ECPrivateKeyParameters(new BigInteger(card.getPrivatekey(), 16), domain));
			BigInteger[] generateSignature = signer.generateSignature(data);
			if (generateSignature != null && generateSignature.length == 2) {
				
				BigInteger s =generateSignature[1];
				if(s.compareTo(HALF_CURVE_ORDER) <= 0) {
					s=domain.getN().subtract(generateSignature[1]);
				}
				String rHex = generateSignature[0].toString(16);
				rHex = rHex.length()==63?("0"+rHex):rHex;
				String sHex = s.toString(16);
				sHex = sHex.length()==63?("0"+sHex):sHex;
				String signString =rHex+sHex;
				
				TLVBox tlvBox = new TLVBox();
				tlvBox.putBytesValue(TLVTag.Transaction_signature, ByteUtil.hexStringToBytes(signString));
				entity.setResult(ByteUtil.toHexString(tlvBox.serialize()) + "9000");

				card.setCount(card.getCount() + 1);
				cardRepository.save(card);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public byte[] IntToByte(int num) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) ((num >> 8) & 0xff);
		bytes[1] = (byte) (num & 0xff);
		return bytes;
	}

	private static SecureRandom sSecureRandom;

	public static byte[] nextRandomBytes() {
		if (sSecureRandom == null) {
			sSecureRandom = new SecureRandom();
		}
		return sSecureRandom.generateSeed(32);
	}

	public static class TLVTag {
		public static final int Software_Author = 0x10;
		public static final int Software_Version = 0x11;
		public static final int Apdu_Protocol_Version = 0x12;
		public static final int Secure_Channel_Protocol = 0x13;
		public static final int Device_Certificate = 0x30;
		public static final int BlockChain_PublicKey = 0x55;
		public static final int Challenge = 0x70;
		public static final int Salt = 0x71;
		public static final int Verification_Signature = 0x73;
		public static final int Transaction_Signature_Counter = 0x90;
		public static final int Transaction_Hash = 0x91;
		public static final int Transaction_signature = 0x92;
	}
}
