package com.racloop.util;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Copier {

    private String fileNamePattern;
    private String fileExtensionPattern;
    private File inputFolder;
    private File outputFolder;

    private String commandHelp;

    private Copier() {
        StringBuilder builder = new StringBuilder();
        builder.append("java -jar copier.jar -i c:/in -o d:/out -f namePrefix -e .docx -z true").append("\n");
        builder.append("Examples : ").append("\n");
        builder.append("1. To copy every thing : java -jar copier.jar -i c:/in -o d:/out").append("\n");
        builder.append("2. To copy every thing and zip final folder structure : java -jar copier.jar -i c:/in -o d:/out -z true").append("\n");
        builder.append("3. To copy only .docx files : java -jar copier.jar -i c:/in -o d:/out -e .docx").append("\n");
        builder.append("4. To copy only .docx files that starts with numbers : java -jar copier.jar -i c:/in -o d:/out -f '\\\\d+' -e .docx").append("\n");
        commandHelp = builder.toString();
    }

    public static void main(String[] args) throws IOException, ParseException {
        Copier copier = new Copier();
        String [] testargs = {"-i", "/Users/rajanpunchouty/MyDocs/tmp/in", "-o", "/Users/rajanpunchouty/MyDocs/tmp/out", "-e", ".docx"};
        boolean isValid = copier.processAndValidateArguments(testargs);
        if(isValid) {
            copier.copyFolder();
        }
        else {
            System.out.println("Invalid Arguments");
        }


//        File sourceFolder = new File("c:\\temp");
//
//        File destinationFolder = new File("c:\\tempNew");
//
//        copyFolder(sourceFolder, destinationFolder);
    }

    private boolean processAndValidateArguments(String[] args) throws ParseException, IOException {
        Options options = new Options();
        options.addOption("i", true, "Input Source Directory e.g. C:/data/in");
        options.addOption("o", true, "Output Directory e.g. C:/data/out");
        options.addOption("f", true, "File name regex pattern e.g.  '\\\\d+' for files name with numbers and '*' for all");
        options.addOption("e", true, "File Selector Pattern 'docx' for docx and '*' for all");
        options.addOption("z", true, "Zip the resultant folder");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);
        HelpFormatter formatter = new HelpFormatter();

        if(cmd.hasOption("i")) {
            String inputDirectory = cmd.getOptionValue("i");
            this.inputFolder = new File(inputDirectory);
        }
        else {
            System.out.println("Option i is required ");
            formatter.printHelp( commandHelp, options );
            return false;
        }

        if(cmd.hasOption("o")) {
            String outputDirectory = cmd.getOptionValue("o");
            this.outputFolder = new File(outputDirectory);
        }
        else {
            System.err.println("Option o is required ");
            formatter.printHelp( commandHelp, options );
            return false;
        }

        if(cmd.hasOption("f")) {
            String pattern = cmd.getOptionValue("f");
            if(pattern.equals("*")) {
                this.fileNamePattern = ".*";
            }
            else {
                this.fileNamePattern = pattern;
            }
        }
        else {
            System.out.println("Using defaults to copy all files ");
            this.fileNamePattern = ".*";
        }

        if(cmd.hasOption("e")) {
            String pattern = cmd.getOptionValue("e");
            this.fileExtensionPattern = pattern;
        }
        else {
            System.out.println("Using defaults to copy all extensions");
            this.fileExtensionPattern = "*";
        }
        boolean valid = validateFolders();
        return valid;
    }

    private boolean validateFolders() throws IOException {
        if(!this.inputFolder.exists()) {
            System.err.println("Input folder does not exist : " + inputFolder.getAbsolutePath());
            return false;
        }
        if(!this.outputFolder.exists()) {
            System.err.println("Output folder does not exist : " + outputFolder.getAbsolutePath());
            return false;
        }
        else {
            FileUtils.cleanDirectory(this.outputFolder);
        }
        return true;
    }

    private void copyFolder() throws IOException {
        copyFolder(inputFolder, outputFolder);
    }

    private void copyFolder(File inputFolder, File outputFolder) throws IOException {
        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then copy the file directly to new location
        if (inputFolder.isDirectory()) {
            //Verify if destinationFolder is already present; If not then create it
            if (!outputFolder.exists()) {
                outputFolder.mkdir();
                System.out.println("Directory created :: " + outputFolder);
            }

            //Get all files from source directory
            String files[] = inputFolder.list();

            //Iterate over all files and copy them to destinationFolder one by one
            for (String file : files) {
                File srcFile = new File(inputFolder, file);
                File destFile = new File(outputFolder, file);

                //Recursive function call
                copyFolder(srcFile, destFile);
            }
        } else {
            //Copy the file content from one place to another
            if(inputFolder.toPath().getFileName().startsWith(".") || inputFolder.toPath().getFileName().startsWith("~")) {
                //do nothing
                System.out.println("Skipping file : " + inputFolder.toPath().getFileName());
            }
            else {
                String fileName = inputFolder.toPath().getFileName().toString();
                if(fileNamePattern.equals("*") && fileExtensionPattern.equals("*")) {
                    Files.copy(inputFolder.toPath(), outputFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File copied :: " + outputFolder);
                } else if(fileNamePattern.equals("*") && fileName.endsWith(fileExtensionPattern)) {
                    Files.copy(inputFolder.toPath(), outputFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File copied :: " + outputFolder);
                } else if(fileName.matches(fileNamePattern) && fileName.endsWith(fileExtensionPattern)) {
                    Files.copy(inputFolder.toPath(), outputFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File copied :: " + outputFolder);
                } else {
                    System.out.println("Skipping file : " + inputFolder.toPath().getFileName());
                }
            }

        }
    }
}
