package io.mosip.registration.cipher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import io.mosip.kernel.crypto.jce.constant.SecurityMethod;
import io.mosip.kernel.crypto.jce.processor.SymmetricProcessor;
import io.mosip.registration.config.RegistrationUpdate;

/**
 * Decryption the Client Jar with Symmetric Key
 * 
 * @author Omsai Eswar M.
 *
 */
public class CilentJarDecryption {

	private static final String SLASH = "/";
	private static final String AES_ALGORITHM = "AES";
	private static final String REGISTRATION = "registration";
	private static final String MOSIP_CLIENT = "mosip-client.jar";
	private static final String MOSIP_SERVICES = "mosip-services.jar";
	private static String libFolder = "lib/";
	private static String binFolder = "bin/";

	static {
		String tempPath = System.getProperty("java.io.tmpdir");
		System.setProperty("java.ext.dirs",
				"C:\\Users\\M1046564\\Desktop\\mosip-sw-0.10.0\\lib;" + tempPath + "/mosip/");

		System.out.println(System.getProperty("java.ext.dirs"));
	}

	/**
	 * Decrypt the bytes
	 * 
	 * @param Jar
	 *            bytes
	 * @throws UnsupportedEncodingException
	 */
	public byte[] decrypt(byte[] data, byte[] encodedString) {
		// Generate AES Session Key
		SecretKey symmetricKey = new SecretKeySpec(encodedString, AES_ALGORITHM);

		return SymmetricProcessor.process(SecurityMethod.AES_WITH_CBC_AND_PKCS5PADDING, symmetricKey, data,
				Cipher.DECRYPT_MODE);
	}

	/**
	 * Decrypt and save the file in temp directory
	 * 
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		CilentJarDecryption aesDecrypt = new CilentJarDecryption();
		RegistrationUpdate registrationUpdate = new RegistrationUpdate();

		// TODO Check Internet Connectivity
		try {

			checkForJars();
		} catch (ParserConfigurationException | SAXException | io.mosip.kernel.core.exception.IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File encryptedClientJar = new File(binFolder + MOSIP_CLIENT);

		File encryptedServicesJar = new File(binFolder + MOSIP_SERVICES);

		String tempPath = FileUtils.getTempDirectoryPath();

		System.out.println(tempPath);

		System.out.println("Decrypt File Name====>" + encryptedClientJar.getName());
		byte[] decryptedRegFileBytes = aesDecrypt.decrypt(FileUtils.readFileToByteArray(encryptedClientJar),
				Base64.getDecoder().decode("bBQX230Wskq6XpoZ1c+Ep1D+znxfT89NxLQ7P4KFkc4="));

		FileUtils.writeByteArrayToFile(new File(tempPath + "/mosip/" + encryptedClientJar.getName()),
				decryptedRegFileBytes);

		System.out.println("Decrypt File Name====>" + encryptedServicesJar.getName());
		byte[] decryptedRegServiceBytes = aesDecrypt.decrypt(FileUtils.readFileToByteArray(encryptedServicesJar),
				Base64.getDecoder().decode("bBQX230Wskq6XpoZ1c+Ep1D+znxfT89NxLQ7P4KFkc4="));

		FileUtils.writeByteArrayToFile(new File(tempPath + "/mosip/" + encryptedServicesJar.getName()),
				decryptedRegServiceBytes);
		try {
			
			String libPath = new File("lib").getAbsolutePath();
			Process process = Runtime.getRuntime().exec(
					"java -Dspring.profiles.active=qa -Djava.ext.dirs="+libPath+" -jar "
							+ tempPath + "/mosip/mosip-client.jar");
			System.out.println("the output stream is " + process.getOutputStream().getClass());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s;
			while ((s = bufferedReader.readLine()) != null) {
				System.out.println("The stream is : " + s);
			}

			if (0 == process.waitFor()) {

				process.destroy();

			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		/*
		 * ProcessBuilder clientBuilder = new ProcessBuilder("java", "-jar", tempPath +
		 * "/mosip/mosip-client.jar");
		 * 
		 * Process process = clientBuilder.start();
		 * 
		 * System.out.println("Invoked suuceessfully");
		 * 
		 * int status = process.waitFor(); if (status == 0) {
		 * System.out.println("Registration Client stopped with the status: " + status);
		 * process.destroy(); FileUtils.deleteDirectory(new File(tempPath + "mosip\\"));
		 * }
		 */
	}

	private static void checkForJars()
			throws IOException, ParserConfigurationException, SAXException, io.mosip.kernel.core.exception.IOException {
		RegistrationUpdate registrationUpdate = new RegistrationUpdate();

		if (registrationUpdate.hasUpdate()) {

			// Generate alert to update or to continue with existing
			boolean update = true;

			if (update) {
				try {
					registrationUpdate.getWithLatestJars();
				} catch (io.mosip.kernel.core.exception.IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				registrationUpdate.getJars();
			}
		} else {
			registrationUpdate.getJars();
		}
	}

	private void setProperties() throws IOException {

		String propsFilePath = new File(System.getProperty("user.dir")).getParentFile()
				+ "/props/mosip-application.properties";

		FileInputStream fileInputStream = new FileInputStream(propsFilePath);
		Properties properties = new Properties();
		properties.load(fileInputStream);

		System.setProperty("reg.db.path", properties.getProperty("mosip.dbpath"));

	}
}