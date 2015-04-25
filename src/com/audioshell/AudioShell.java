package com.audioshell;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by cornelius on 3/11/15.
 */
public class AudioShell {

    private File currentDirectory;
    private FilenameFilter filter;
    private List<File> myFileList;

    public AudioShell() {
        this.currentDirectory = new File("/home/cornelius/data/Music");
        this.filter = new FilenameFilter()//anonim class
        {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".wma") || fileName.endsWith(".flac");
            }
        };
        this.myFileList=new ArrayList<File>();
    }

    public void start() {
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Start shell");
        while (keyboard.hasNextLine()) {
            List<String> command = new ArrayList<String>();
            String stringCommand = keyboard.nextLine();
            if (Pattern.matches("exit.*", stringCommand)) break;
            for (String s : stringCommand.split(" ",2)) command.add(s);
            try {
                parseCommand(command);
            } catch (InvalidDataException ide) {
                System.out.println(ide.getMessage());
            }
        }
        System.out.println("Exiting...");
    }

    private void parseCommand(List command) throws InvalidDataException {
        if (Pattern.matches("list", command.get(0).toString())) list(command);
        else if (Pattern.matches("play", command.get(0).toString())) play(command);
        else if (Pattern.matches("search", command.get(0).toString())) search(command);
        else if (Pattern.matches("edit", command.get(0).toString())) edit(command);
        else if (Pattern.matches("setdir",command.get(0).toString())) setCurrentDirectory(command.get(1).toString());
        else throw new InvalidDataException("Error:Invalid command");
    }

    private void list(List command) throws InvalidDataException {
        System.out.println("Listing...");
        String pathPattern = "[/\\w]+";
        if (command.size() == 1) {
            listAudioFiles();
        }
        else if (command.size() ==2 && Pattern.matches(pathPattern, command.get(1).toString()))
        {
            listAudioFiles(command.get(1).toString());
        }
        else throw new InvalidDataException("Error:Invalid argument");
    }

    private void listAudioFiles() {
        File[] audioFiles = this.getListAudioFiles();
        for (int i = 0; i < audioFiles.length; i++) {
            myFileList.add(audioFiles[i]);
            System.out.println("File" + i + ":" + audioFiles[i].getName());
        }
    }

    private void listAudioFiles(String path) {
        try {
            File[] audioFiles=getListAudioFiles(path);
            for (int i = 0; i < audioFiles.length; i++)
                System.out.println("File"+ i + ":" + audioFiles[i].getName());
        } catch (InvalidDataException ide) {
            System.out.println(ide.getMessage());
        }
    }

    private File[] getListAudioFiles() {
        File[] audioFiles = currentDirectory.listFiles(filter);
        return audioFiles;
    }

    private File[] getListAudioFiles(String path)throws InvalidDataException{
        File dir = new File(path);
        if (dir.isDirectory()) {
            File[] audioFiles = dir.listFiles(filter);
            return audioFiles;
        } else throw new InvalidDataException("Error:The path is incorrect");
    }

    private void play(List command)throws InvalidDataException{
        System.out.println("Playing...");
        if(command.size()==1)throw new InvalidDataException("Error:No file argument found");
        if(isNumeric(command.get(1).toString())){
            play(Integer.parseInt(command.get(1).toString()));
        }
        else {
            Pattern filePattern = Pattern.compile("([0-9]{2}(\\.))?[a-zA-Z \\.0-9]+(-)[a-zA-Z\\.() \\[\\]0-9]+(\\.)((mp3)|(wav)|(wma)|(flac))");
            if(Pattern.matches(filePattern.pattern(),command.get(1).toString()) ){
              File file = new File(currentDirectory.toString() + "/" + command.get(1).toString());
              play(file);}
            else{
                File file = new File(command.get(1).toString());
                play(file);
            }
        }
    }

    private void play(int fileNumber)throws InvalidDataException{
        if(fileNumber<0||fileNumber>myFileList.size())throw new InvalidDataException("Error:Wrong number");
        try {
            Runtime.getRuntime().exec("vlc " + myFileList.get(fileNumber).toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void play(File file) throws InvalidDataException {

        if (!file.exists()) throw new InvalidDataException("Error:File does not exist");
        else if (!file.isFile()) throw new InvalidDataException("Error:Is not a file");
        else {
            try {
                Runtime.getRuntime().exec("vlc " + file.toPath());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void search(List command) throws InvalidDataException {
        System.out.println("Searching...");
        if(command.size()==1)throw new InvalidDataException("Error:Argument not found");
        Pattern filePattern = Pattern.compile("([0-9]{2}(\\.))?[a-zA-Z \\.0-9]+(-)[a-zA-Z\\.() \\[\\]0-9]+(\\.)((mp3)|(wav)|(wma)|(flac))");
        if(Pattern.matches(filePattern.pattern(), command.get(1).toString()) )
            search(this.currentDirectory,command.get(1).toString());
        else throw new InvalidDataException("Error: Pattern not matched");
    }

    private void search(File file,String fileNameToSearch)throws InvalidDataException {
        for (File temp : file.listFiles()) {
            if (temp.isDirectory()) {
                search(temp,fileNameToSearch);
            } else {
                if ( fileNameToSearch.equals(temp.getAbsoluteFile().getName().toString())) {
                    System.out.print(temp.getAbsolutePath().toString());
                    return;
                }
            }
        }
        throw new InvalidDataException("Error:File not found");
    }

    private static boolean isNumeric(String str)
    {
        try
        {
            int d = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private void rename(List args)throws InvalidDataException{
        File file= new File(this.currentDirectory.toString()+"/"+args.get(0).toString());
        File newFile=new File(this.currentDirectory.toString()+"/"+args.get(2).toString());
        if(!file.exists())throw new InvalidDataException("Error:File does not exist");
        if(newFile.exists())throw new InvalidDataException("Error:File exists,can't rename ");
        boolean ok=file.renameTo(newFile);
        if(!ok){
            throw new InvalidDataException("Error:Rename failed");
        }
    }

    private void addPrefix(List args)throws InvalidDataException{
        args.set(2,this.currentDirectory.toString()+"/"+args.get(2).toString()+args.get(0).toString());
        rename(args);
    }

    private void edit(List command) throws InvalidDataException {
        System.out.println("Editing...");
        List<String> args = new ArrayList<String>();
        for (String s : command.get(1).toString().split(" ")) args.add(s);
        if(args.size()!=3)throw new InvalidDataException("Error:Insuficient arguments");
        if(args.get(1).equals("-r")) {
            rename(args);
        }
        else if(args.get(1).equals("-p")){
            addPrefix(args);
        }
        else throw new InvalidDataException("Error: Wrong parameter");
    }

    private void setCurrentDirectory(String currentDirectory) throws InvalidDataException {
        if (new File(currentDirectory).exists())
            if (new File(currentDirectory).isDirectory())
                this.currentDirectory = new File(currentDirectory);
            else throw new InvalidDataException("Error:Path is not a directory");
        else throw new InvalidDataException("Error:Path does not exist");
    }




}
