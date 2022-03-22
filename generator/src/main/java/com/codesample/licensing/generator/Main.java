package com.codesample.licensing.generator;

import com.codesample.licensing.entity.LicenseInfo;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.out;

public class Main {

    private static final String ACTION_GENERATE = "generate";
    private static final String ACTION_KEYPAIR = "keypair";
    private static final String ACTION_HELP = "help";

    private static final String ARG_OUTPUT = "output";
    private static final String ARG_FROM = "from";
    private static final String ARG_TO = "to";

    private static final String DEFAULT_FILENAME = "license.lic";
    private static final String DATE_FORMAT = "yyyy.MM.dd";

    private static final List<String> knownArgs = Arrays.asList(ARG_OUTPUT, ARG_FROM, ARG_TO);

    private static boolean generateKeypair = false;
    private static File output = null;
    private static LicenseInfo licenseInfo = null;

    public static void main(String[] args) {
        try {
            parseArgs(args);
        } catch (IllegalArgumentException e)  {
            out.println(e.getMessage());
            System.exit(-1);
        }

        try {
            doActions();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-2);
        }
    }

    private static void parseArgs(String[] args) {

        final String action = args.length == 0 ? ACTION_HELP : args[0];

        final Map<String, String> map = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            final String[] split = args[i].split("=", -1);
            final String key, value;
            if (split.length == 2) {
                key = split[0];
                value = split[1];
            } else {
                key = args[i];
                value = null;
            }
            if (map.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate argument " + key);
            }
            map.put(key, value);
        }

        switch (action) {
            case ACTION_GENERATE:
                parseGenerateArgs(map);
                break;
            case ACTION_KEYPAIR:
                parseKeypairArgs(map);
                break;
            case ACTION_HELP:
                showUsage();
                System.exit(0);
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }

        for (String knownArg: knownArgs) {
            if (map.containsKey(knownArg)) {
                throw new IllegalArgumentException(String.format("'%s' argument is not allowed for '%s' action", knownArg, action));
            }
        }

        if (!map.isEmpty()) {
            throw new IllegalArgumentException("Unknown arguments: " + String.join(", ", map.keySet()));
        }
    }

    private static void parseGenerateArgs(Map<String, String> args) {
        if (args.containsKey(ARG_OUTPUT)) {
            final String outputPath = args.remove(ARG_OUTPUT);
            checkArgNotEmpty(ARG_OUTPUT, outputPath);
            File outputFile = new File(outputPath);
            if (outputFile.isDirectory()) {
                if (!outputFile.canWrite()) {
                    throw new IllegalArgumentException("Cannot write to " + outputFile.getAbsolutePath());
                }
                outputFile = new File(outputFile, DEFAULT_FILENAME);
            }
            if (outputFile.exists() && !outputFile.canWrite()) {
                throw new IllegalArgumentException("Cannot override file " + outputFile.getAbsolutePath());
            }
            output = outputFile;
        }
        else {
            output = new File(DEFAULT_FILENAME);
        }

        long from, to;
        if (args.containsKey(ARG_FROM)) {
            from = parseDate(args.remove(ARG_FROM));
        }
        else {
            throw new IllegalArgumentException(String.format("%s action require '%s' parameter", ACTION_GENERATE, ARG_FROM));
        }

        if (args.containsKey(ARG_TO)) {
            to = parseDate(args.remove(ARG_TO));
        }
        else {
            throw new IllegalArgumentException(String.format("%s action require '%s' parameter", ACTION_GENERATE, ARG_TO));
        }

        licenseInfo = new LicenseInfo(from, to);
    }

    private static void parseKeypairArgs(Map<String, String> args) {
        if (args.containsKey(ARG_OUTPUT)) {
            final String outputPath = args.remove(ARG_OUTPUT);
            checkArgNotEmpty(ARG_OUTPUT, outputPath);
            final File outputFile = new File(outputPath);
            if (!outputFile.isDirectory()) {
                throw new IllegalArgumentException(
                        String.format("'%s' action require '%s' parameter to point at directory", ACTION_KEYPAIR, ARG_OUTPUT));
            }
            if (!outputFile.canWrite()) {
                throw new IllegalArgumentException("Cannot write to " + outputFile.getAbsolutePath());
            }
            output = outputFile;
        }
        else {
            output = new File("");
        }

        generateKeypair = true;
    }

    private static void checkArgNotEmpty(String key, String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(String.format("'%s' argument has no value specified", key));
        }
    }

    private static long parseDate(String dateString) {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            return sdf.parse(dateString).getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Invalid date. Date must be in %s format", DATE_FORMAT));
        }
    }

    private static void showUsage() {
        out.printf(
                "Usage:\n" +
                "licGen <action> [options...]\n\n" +

                "licGen generate from=yyyy.MM.dd to=yyyy.MM.dd [output=/path/to/output/license]\n" +
                "generate new license with specified lifetime\n\n" +

                "licGen keypair [output=/path/to/output/dir]\n" +
                "generate new keypair for license signing\n\n" +

                "licGen help\n" +
                "print this message\n"
        );
    }

    private static void doActions() throws Exception {
        if (generateKeypair) {
            new Generator().generateKeypair(output);
        }
        else {
            final String json = new Gson().toJson(licenseInfo);
            final Encryptor encryptor = new Encryptor();
            final String encryptedKey = encodeBase64(encryptor.getEncryptedKey());
            final String encryptedJson = encodeBase64(encryptor.getEncryptedText(json));
            FileUtils.write(output, encryptedKey + "|" +  encryptedJson, StandardCharsets.UTF_8);
            out.println("Generate license in " + output.getAbsolutePath());
        }

        out.println("Done!");
    }

    private static String encodeBase64(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }
}



