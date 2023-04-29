/*
 * Copyright 2014 Amund Elstad. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.aelstad.keccakj.provider;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.github.aelstad.keccakj.cipher.CipherAdapter;
import com.github.aelstad.keccakj.cipher.CipherInterface;
import com.github.aelstad.keccakj.cipher.CipherProviderFactory;

public final class KeccakjProvider extends Provider implements CipherProviderFactory {
	final static String cipherPrefix = "Cipher.";
	final static String messageDigestPrefix = "MessageDigest.";
	final static String secureRandomPrefix = "SecureRandom.";
	
	public KeccakjProvider() {
		super("com.github.aelstad.keccakj", 1.0, "Implementation of digests, ciphers and random generated based on the Keccack permutation. Includes SHA3 digests");
				
		put(messageDigestPrefix + Constants.SHA3_224, "com.github.aelstad.keccakj.fips202.SHA3_224");
		put(messageDigestPrefix + Constants.SHA3_256, "com.github.aelstad.keccakj.fips202.SHA3_256");
		put(messageDigestPrefix + Constants.SHA3_384, "com.github.aelstad.keccakj.fips202.SHA3_384");
		put(messageDigestPrefix + Constants.SHA3_512, "com.github.aelstad.keccakj.fips202.SHA3_512");		
		
		put(secureRandomPrefix + Constants.KECCAK_RND128, "com.github.aelstad.keccakj.spi.KeccakRnd128");
		put(secureRandomPrefix + Constants.KECCAK_RND256, "com.github.aelstad.keccakj.spi.KeccakRnd256");
		
		put(cipherPrefix + Constants.SHAKE128_STREAM_CIPHER, "com.github.aelstad.keccakj.spi.Shake128StreamCipher");
		put(cipherPrefix + Constants.SHAKE256_STREAM_CIPHER, "com.github.aelstad.keccakj.spi.Shake256StreamCipher");
		
		put(cipherPrefix + Constants.LAKEKEYAK_AUTHENTICATING_STREAM_CIPHER, "com.github.aelstad.keccakj.spi.LakeKeyakCipher");
	}

	@Override
	public CipherInterface getInstance(String transformation) throws NoSuchAlgorithmException, NoSuchPaddingException {
		String clazzName = getProperty(cipherPrefix + transformation);
		if(clazzName == null) {
			return new CipherAdapter(Cipher.getInstance(transformation));
		} else {		
			try {
				return (CipherInterface) Class.forName(clazzName).newInstance();
			} catch(ClassNotFoundException ex) {
				throw new NoSuchAlgorithmException();
			} catch(ClassCastException ex) {
				throw new NoSuchAlgorithmException();
			} catch (InstantiationException e) {
				throw new NoSuchAlgorithmException();
			} catch (IllegalAccessException e) {
				throw new NoSuchAlgorithmException();
			}
		}
	}
}
