package io.enotes.emulator.utils;

import java.security.Provider;

import org.spongycastle.jce.provider.BouncyCastleProvider;

public final class SpongyCastleProvider {
	public SpongyCastleProvider() {
	}

	public static Provider getInstance() {
		return SpongyCastleProvider.Holder.INSTANCE;
	}

	private static class Holder {
		private static final Provider INSTANCE = new BouncyCastleProvider();

		private Holder() {
		}
	}


}
