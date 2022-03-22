# License generator
Example of how to implement licensing for java applications

## Usage

1. Build license generator app
```bash
./gradlew generator:shadowJar
```

2. Generate key pair for signing license
```bash
cd generator/build/libs
java -jar generator-1.0-all.jar keypair output=/path/to/output/key/files
```

3. Put `private.key` into `<project dir>/generator/src/main/resources`

4. Put `public.key` into `<project dir>/reader/src/main/resources`

5. Build license generator app again (with private key now included)
```bash
./gradlew generator:shadowJar
```

6. Build license reader library
```bash
./gradlew reader:shadowJar
```

7. Put reader library into your app

8. In your app add code to read license file
```java
public static void main(String[] args) {
	String licensePath = args[0];
	LicenseInfo licenseInfo = LicenseReader.readLicense(new File(licensePath));
	long now = System.currentTimeMillis();
	if (licenseInfo.getStartDate() <= now && now <= licenseInfo.getEndDate()) {
		// Start app
	}
	else {
		// Show license error
	}
}
```

9. Generate licenses
```bash
java -jar generator-1.0-all.jar generate from=2022.01.01 to=2023.01.01 output=/path/to/output/license/file
```
